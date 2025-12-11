package com.cudeca.service.impl;

import com.cudeca.dto.CheckoutRequest;
import com.cudeca.dto.CheckoutResponse;
import com.cudeca.exception.AsientoNoDisponibleException;
import com.cudeca.model.enums.*;
import com.cudeca.model.evento.Asiento;
import com.cudeca.model.evento.TipoEntrada;
import com.cudeca.model.negocio.*;
import com.cudeca.model.usuario.Invitado;
import com.cudeca.model.usuario.Usuario;
import com.cudeca.repository.*;
import com.cudeca.service.CheckoutService;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio de Checkout.
 * Maneja la lógica de negocio para el proceso de compra.
 */
@Service
@Transactional
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "ObjectMapper is thread-safe and commonly injected in Spring")
public class CheckoutServiceImpl implements CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutServiceImpl.class);
    private static final String COMPRA_NO_ENCONTRADA = "Compra no encontrada: ";

    private final CompraRepository compraRepository;
    private final UsuarioRepository usuarioRepository;
    private final InvitadoRepository invitadoRepository;
    private final TipoEntradaRepository tipoEntradaRepository;
    private final AsientoRepository asientoRepository;
    private final CertificadoFiscalRepository certificadoRepository;
    private final MonederoRepository monederoRepository;
    private final MovimientoMonederoRepository movimientoRepository;
    private final PagoRepository pagoRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CheckoutResponse procesarCheckout(CheckoutRequest request) {
        if (log.isInfoEnabled()) {
            log.info("Procesando checkout. Usuario: {}, Método: {}", request.getUsuarioId(), request.getMetodoPago());
        }

        validarCheckoutRequest(request);
        if (request.getAsientoIds() != null && !request.getAsientoIds().isEmpty()) {
            bloquearAsientos(request.getAsientoIds());
        }

        Compra compra = crearCompra(request);
        procesarItems(request, compra);
        BigDecimal total = calcularTotal(compra, request.getDonacionExtra());

        MetodoPago metodo = MetodoPago.valueOf(request.getMetodoPago());

        if (metodo == MetodoPago.MONEDERO) {
            procesarPagoMonedero(compra, total, request.getUsuarioId());
            compra.setEstado(EstadoCompra.COMPLETADA);
        } else {
            compra.setEstado(EstadoCompra.PENDIENTE);
        }

        compra = compraRepository.save(compra);

        if (request.getDatosFiscales() != null) {
            generarCertificadoFiscal(compra, request);
        }

        if (log.isInfoEnabled()) {
            log.info("Compra procesada ID: {}, Estado: {}", compra.getId(), compra.getEstado());
        }
        return construirRespuesta(compra, total);
    }

    private void procesarPagoMonedero(Compra compra, BigDecimal total, Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("Los invitados no pueden pagar con Monedero");
        }

        Monedero monedero = monederoRepository.findByUsuario_Id(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no tiene monedero activo"));

        if (monedero.getSaldo().compareTo(total) < 0) {
            throw new IllegalStateException("Saldo insuficiente en el monedero");
        }

        monedero.setSaldo(monedero.getSaldo().subtract(total));
        monederoRepository.save(monedero);

        MovimientoMonedero mov = MovimientoMonedero.builder()
                .monedero(monedero)
                .tipo(TipoMovimiento.CARGO)
                .importe(total)
                .referencia("Compra #" + (compra.getId() != null ? compra.getId() : "PENDIENTE"))
                .fecha(OffsetDateTime.now())
                .build();
        movimientoRepository.save(mov);

        Pago pago = Pago.builder()
                .compra(compra)
                .importe(total)
                .metodo(MetodoPago.MONEDERO)
                .estado(EstadoPago.APROBADO)
                .createdAt(OffsetDateTime.now())
                .build();

        compra.getPagos().add(pago);
    }

    private void generarCertificadoFiscal(Compra compra, CheckoutRequest request) {
        try {
            CertificadoFiscal certificado = new CertificadoFiscal();
            certificado.setCompra(compra);

            // Usamos un dummy DatosFiscales porque la entidad requiere relación,
            // aunque en el futuro debería ser independiente.
            // Aquí guardamos el JSON snapshot que es lo importante legalmente.
            String snapshot = objectMapper.writeValueAsString(request.getDatosFiscales());
            certificado.setDatosSnapshotJson(snapshot);

            // Cálculo simplificado
            BigDecimal baseDonacion = calcularBaseDonacion(compra);
            certificado.setImporteDonado(baseDonacion);
            certificado.setNumeroSerie("CERT-" + System.currentTimeMillis());

            certificadoRepository.save(certificado);

        } catch (Exception e) {
            log.error("Error generando certificado fiscal", e);
            // No fallamos la compra por esto, pero lo logueamos
        }
    }

    private BigDecimal calcularBaseDonacion(Compra compra) {
        return compra.getArticulos().stream()
                .filter(a -> a instanceof ArticuloDonacion)
                .map(a -> a.getPrecioUnitario().multiply(BigDecimal.valueOf(a.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public boolean confirmarPago(Long compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new IllegalArgumentException(COMPRA_NO_ENCONTRADA + compraId));

        if (compra.getEstado() != EstadoCompra.PENDIENTE) return false;

        compra.setEstado(EstadoCompra.COMPLETADA);
        compraRepository.save(compra);
        return true;
    }

    @Override
    public boolean cancelarCompra(Long compraId, String motivo) {
        log.info("Cancelando compra ID: {} - Motivo: {}", compraId, motivo);

        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new IllegalArgumentException(COMPRA_NO_ENCONTRADA + compraId));

        if (compra.getEstado() == EstadoCompra.COMPLETADA) {
            log.warn("No se puede cancelar una compra completada");
            return false;
        }

        compra.setEstado(EstadoCompra.CANCELADA);
        compraRepository.save(compra);

        log.info("Compra cancelada ID: {}", compraId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public CheckoutResponse obtenerDetallesCompra(Long compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new IllegalArgumentException(COMPRA_NO_ENCONTRADA + compraId));

        BigDecimal total = calcularTotalCompra(compra);
        return construirRespuesta(compra, total);
    }

    // --- MÉTODOS PRIVADOS DE APOYO ---

    private void validarCheckoutRequest(CheckoutRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        // Si no hay usuario, debe ser invitado y tener email
        if (request.getUsuarioId() == null &&
                (request.getEmailContacto() == null || request.getEmailContacto().isBlank())) {
            throw new IllegalArgumentException("Se requiere email de contacto para compras sin usuario");
        }
    }

    private Compra crearCompra(CheckoutRequest request) {
        Compra.CompraBuilder builder = Compra.builder()
                .fecha(OffsetDateTime.now())
                .estado(EstadoCompra.PENDIENTE)
                .emailContacto(request.getEmailContacto())
                .articulos(new ArrayList<>())
                .pagos(new ArrayList<>());

        // Asociar usuario o invitado
        if (request.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            builder.usuario(usuario);
        } else {
            // Buscar o crear invitado por email
            Invitado invitado = invitadoRepository.findByEmail(request.getEmailContacto())
                    .orElseGet(() -> invitadoRepository.save(Invitado.builder().email(request.getEmailContacto()).build()));
            builder.invitado(invitado);
        }
        return builder.build();
    }

    private void procesarItems(CheckoutRequest request, Compra compra) {
        for (CheckoutRequest.ItemDTO itemDTO : request.getItems()) {
            TipoItem tipo = TipoItem.valueOf(itemDTO.getTipo());
            ArticuloCompra articulo = switch (tipo) {
                case ENTRADA -> {
                    TipoEntrada te = tipoEntradaRepository.findById(itemDTO.getReferenciaId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "TipoEntrada no encontrado con ID: " + itemDTO.getReferenciaId()));
                    yield ArticuloEntrada.builder()
                            .tipoEntrada(te)
                            .cantidad(itemDTO.getCantidad())
                            .precioUnitario(BigDecimal.valueOf(itemDTO.getPrecio()))
                            .build();
                }
                case SORTEO -> ArticuloSorteo.builder()
                        .cantidad(itemDTO.getCantidad())
                        .precioUnitario(BigDecimal.valueOf(itemDTO.getPrecio()))
                        .build();
                case DONACION -> ArticuloDonacion.builder()
                        .cantidad(itemDTO.getCantidad())
                        .precioUnitario(BigDecimal.valueOf(itemDTO.getPrecio()))
                        .build();
            };
            articulo.setCompra(compra);
            compra.getArticulos().add(articulo);
        }
    }

    private BigDecimal calcularTotal(Compra compra, Double donacionExtra) {
        BigDecimal total = compra.getArticulos().stream()
                .map(a -> a.getPrecioUnitario().multiply(BigDecimal.valueOf(a.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (donacionExtra != null && donacionExtra > 0) {
            total = total.add(BigDecimal.valueOf(donacionExtra));
            // Deberías añadir un ArticuloDonacion extra aquí si quieres persistirlo
        }
        return total;
    }

    private BigDecimal calcularTotalCompra(Compra compra) {
        return compra.getArticulos().stream()
                .map(a -> a.getPrecioUnitario().multiply(BigDecimal.valueOf(a.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CheckoutResponse construirRespuesta(Compra compra, BigDecimal total) {
        CheckoutResponse response = new CheckoutResponse();
        response.setCompraId(compra.getId());
        response.setEstado(compra.getEstado().name());
        response.setTotal(total);
        response.setMensaje(obtenerMensajeEstado(compra.getEstado()));
        // La URL de pasarela se establecería aquí si se integra con un proveedor de pago
        response.setUrlPasarela(null);
        return response;
    }

    private String obtenerMensajeEstado(EstadoCompra estado) {
        return switch (estado) {
            case PENDIENTE -> "Compra creada. Pendiente de pago.";
            case COMPLETADA -> "Compra completada exitosamente.";
            case CANCELADA -> "Compra cancelada.";
            case PARCIAL_REEMBOLSADA -> "Compra parcialmente reembolsada.";
            case REEMBOLSADA -> "Compra completamente reembolsada.";
        };
    }

    /**
     * Bloquea asientos usando bloqueo pesimista (PESSIMISTIC_WRITE) y valida su disponibilidad.
     * Este método previene condiciones de carrera (race conditions) en compras concurrentes.
     *
     * @param asientoIds Lista de IDs de asientos a bloquear
     * @throws AsientoNoDisponibleException si algún asiento no está LIBRE
     */
    private void bloquearAsientos(List<Long> asientoIds) {
        if (log.isInfoEnabled()) {
            log.info("Bloqueando {} asientos: {}", asientoIds.size(), asientoIds);
        }

        // 1. Obtener asientos con bloqueo pesimista (evita lecturas concurrentes)
        List<Asiento> asientos = asientoRepository.findAllByIdWithLock(asientoIds);

        // 2. Validar que se encontraron todos los asientos
        if (asientos.size() != asientoIds.size()) {
            List<Long> encontrados = asientos.stream().map(Asiento::getId).toList();
            List<Long> noEncontrados = asientoIds.stream()
                    .filter(id -> !encontrados.contains(id))
                    .toList();
            throw new IllegalArgumentException(
                    "Asientos no encontrados: " + noEncontrados);
        }

        // 3. Validar que todos los asientos están LIBRES
        List<Long> asientosNoDisponibles = asientos.stream()
                .filter(asiento -> asiento.getEstado() != EstadoAsiento.LIBRE)
                .map(Asiento::getId)
                .toList();

        if (!asientosNoDisponibles.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("Intento de reservar asientos no disponibles: {}", asientosNoDisponibles);
            }
            throw new AsientoNoDisponibleException(
                    "Los siguientes asientos no están disponibles: " + asientosNoDisponibles);
        }

        // 4. Cambiar estado a BLOQUEADO
        asientos.forEach(asiento -> {
            asiento.setEstado(EstadoAsiento.BLOQUEADO);
            if (log.isDebugEnabled()) {
                log.debug("Asiento {} bloqueado exitosamente", asiento.getId());
            }
        });

        // 5. Persistir cambios (dentro de la misma transacción)
        asientoRepository.saveAll(asientos);

        if (log.isInfoEnabled()) {
            log.info("Todos los asientos bloqueados exitosamente");
        }
    }
}
