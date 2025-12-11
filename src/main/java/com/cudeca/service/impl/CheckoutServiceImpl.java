package com.cudeca.service.impl;

import com.cudeca.dto.CheckoutRequest;
import com.cudeca.dto.CheckoutResponse;
import com.cudeca.dto.TicketDTO;
import com.cudeca.exception.AsientoNoDisponibleException;
import com.cudeca.model.enums.*;
import com.cudeca.model.evento.Asiento;
import com.cudeca.model.evento.TipoEntrada;
import com.cudeca.model.negocio.*;
import com.cudeca.model.usuario.Invitado;
import com.cudeca.model.usuario.Usuario;
import com.cudeca.repository.*;
import com.cudeca.service.CheckoutService;
import com.cudeca.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Implementación del servicio de Checkout.
 * Maneja la lógica de negocio para el proceso de compra, pago y emisión de entradas.
 */
@Service
@Transactional
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "ObjectMapper is thread-safe and commonly injected in Spring")
public class CheckoutServiceImpl implements CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutServiceImpl.class);
    private static final String COMPRA_NO_ENCONTRADA = "Compra no encontrada: ";

    // --- REPOSITORIOS (Inyección de Dependencias) ---
    private final CompraRepository compraRepository;
    private final UsuarioRepository usuarioRepository;
    private final InvitadoRepository invitadoRepository;
    private final TipoEntradaRepository tipoEntradaRepository;
    private final AsientoRepository asientoRepository;
    private final CertificadoFiscalRepository certificadoRepository;
    private final MonederoRepository monederoRepository;
    private final MovimientoMonederoRepository movimientoRepository;
    private final PagoRepository pagoRepository;
    private final EntradaEmitidaRepository entradaEmitidaRepository; // Variable de instancia

    // --- SERVICIOS Y UTILIDADES ---
    private final ObjectMapper objectMapper;
    private final TicketService ticketService;

    @Override
    public CheckoutResponse procesarCheckout(CheckoutRequest request) {
        if (log.isInfoEnabled()) {
            log.info("Procesando checkout. Usuario: {}, Método: {}", request.getUsuarioId(), request.getMetodoPago());
        }

        // 1. Validaciones y Bloqueo de Asientos
        validarCheckoutRequest(request);
        if (request.getAsientoIds() != null && !request.getAsientoIds().isEmpty()) {
            bloquearAsientos(request.getAsientoIds());
        }

        // 2. Crear estructura de la Compra
        Compra compra = crearCompra(request);
        procesarItems(request, compra);
        BigDecimal total = calcularTotal(compra, request.getDonacionExtra());

        // 3. Lógica de Pago
        MetodoPago metodo = MetodoPago.valueOf(request.getMetodoPago());

        if (metodo == MetodoPago.MONEDERO) {
            // Pago inmediato con saldo (descuenta dinero y marca completada)
            procesarPagoMonedero(compra, total, request.getUsuarioId());
            compra.setEstado(EstadoCompra.COMPLETADA);
        } else {
            // Pago diferido (Pasarela Externa)
            compra.setEstado(EstadoCompra.PENDIENTE);
        }

        // 4. Guardar Compra (Genera ID en base de datos)
        compra = compraRepository.save(compra);

        // 5. Generar Snapshot Fiscal (Si el usuario pidió factura)
        if (request.getDatosFiscales() != null) {
            generarCertificadoFiscal(compra, request);
        }

        // 6. POST-PROCESADO: Si está pagada, generar entradas y enviar email
        if (compra.getEstado() == EstadoCompra.COMPLETADA) {
            generarEntradasEmitidas(compra); // Crea los QRs en BD
            enviarTicketsPorEmail(compra);   // Envía los PDFs al usuario
        }

        if (log.isInfoEnabled()) {
            log.info("Compra procesada ID: {}, Estado: {}", compra.getId(), compra.getEstado());
        }
        return construirRespuesta(compra, total);
    }

    /**
     * Genera los registros de entradas en la base de datos con QRs únicos.
     * Sin esto, el validador del portero no encontrará nada.
     */
    private void generarEntradasEmitidas(Compra compra) {
        log.info("Generando entradas emitidas para compra ID: {}", compra.getId());

        for (ArticuloCompra articulo : compra.getArticulos()) {
            if (articulo instanceof ArticuloEntrada articuloEntrada) {
                // Generar tantas entradas como cantidad indique el artículo
                for (int i = 0; i < articuloEntrada.getCantidad(); i++) {
                    EntradaEmitida entrada = new EntradaEmitida();
                    entrada.setArticuloEntrada(articuloEntrada);
                    entrada.setEstado(EstadoEntrada.VALIDA);

                    // Generar QR único seguro (UUID o Hash)
                    String codigoQR = "TICKET-" + compra.getId() + "-" + articulo.getId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
                    entrada.setCodigoQR(codigoQR);

                    // Guardar en BD usando la variable inyectada (CORREGIDO)
                    entradaEmitidaRepository.save(entrada);

                    // Añadir a la lista en memoria para usarla luego en el email
                    if (articuloEntrada.getEntradasEmitidas() == null) {
                        articuloEntrada.setEntradasEmitidas(new ArrayList<>());
                    }
                    articuloEntrada.getEntradasEmitidas().add(entrada);
                }
            }
        }
    }

    private void enviarTicketsPorEmail(Compra compra) {
        log.info("Iniciando envío de tickets para compra ID: {}", compra.getId());
        try {
            String nombreUsuario = compra.getUsuario() != null ? compra.getUsuario().getNombre() : "Invitado";
            String emailUsuario = compra.getEmailContacto();

            for (ArticuloCompra articulo : compra.getArticulos()) {
                if (articulo instanceof ArticuloEntrada articuloEntrada) {
                    var evento = articuloEntrada.getTipoEntrada().getEvento();

                    // IMPORTANTE: Iteramos sobre las entradas que acabamos de guardar
                    if (articuloEntrada.getEntradasEmitidas() != null) {
                        for (EntradaEmitida entradaEmitida : articuloEntrada.getEntradasEmitidas()) {

                            TicketDTO ticketDTO = TicketDTO.builder()
                                    .nombreEvento(evento.getNombre())
                                    .lugarEvento(evento.getLugar())
                                    .fechaEventoFormato(evento.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                                    .descripcionEvento(evento.getDescripcion())
                                    .nombreUsuario(nombreUsuario)
                                    .emailUsuario(emailUsuario)
                                    .tipoEntrada(articuloEntrada.getTipoEntrada().getNombre())
                                    .precio(articuloEntrada.getPrecioUnitario() + "€")
                                    .zonaRecinto(articuloEntrada.getAsiento() != null ? articuloEntrada.getAsiento().getZona().getNombre() : "General")
                                    .codigoAsiento(articuloEntrada.getAsiento() != null ? articuloEntrada.getAsiento().getCodigoEtiqueta() : "Sin Asiento")
                                    .fila(articuloEntrada.getAsiento() != null ? articuloEntrada.getAsiento().getFila() : null)
                                    .columna(articuloEntrada.getAsiento() != null ? articuloEntrada.getAsiento().getColumna() : null)
                                    .codigoQR(entradaEmitida.getCodigoQR()) // Usamos el QR real guardado en BD
                                    .build();

                            // Llamada al servicio del Grupo C
                            ticketService.generarYEnviarTicket(ticketDTO);
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            // No lanzamos excepción para no revertir la transacción de compra si falla el email
            log.error("Error enviando tickets por email (La compra SÍ se guardó): {}", e.getMessage());
        }
    }

    private void procesarPagoMonedero(Compra compra, BigDecimal total, Long usuarioId) {
        if (usuarioId == null) throw new IllegalArgumentException("Los invitados no pueden pagar con Monedero");

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
            // Snapshot JSON
            String snapshot = objectMapper.writeValueAsString(request.getDatosFiscales());
            certificado.setDatosSnapshotJson(snapshot);

            BigDecimal baseDonacion = calcularBaseDonacion(compra);
            certificado.setImporteDonado(baseDonacion);
            certificado.setNumeroSerie("CERT-" + System.currentTimeMillis());

            certificadoRepository.save(certificado);
        } catch (Exception e) {
            log.error("Error generando certificado fiscal", e);
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

        // Al confirmar pago externo, también generamos y enviamos
        generarEntradasEmitidas(compra);
        enviarTicketsPorEmail(compra);

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

        if (request.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            builder.usuario(usuario);
        } else {
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
                case ENTRADA -> crearArticuloEntrada(itemDTO);
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

    private ArticuloEntrada crearArticuloEntrada(CheckoutRequest.ItemDTO itemDTO) {
        TipoEntrada te = tipoEntradaRepository.findById(itemDTO.getReferenciaId())
                .orElseThrow(() -> new IllegalArgumentException("TipoEntrada no encontrado con ID: " + itemDTO.getReferenciaId()));

        return ArticuloEntrada.builder()
                .tipoEntrada(te)
                .cantidad(itemDTO.getCantidad())
                .precioUnitario(BigDecimal.valueOf(itemDTO.getPrecio()))
                .entradasEmitidas(new ArrayList<>())
                .build();
    }

    private BigDecimal calcularTotal(Compra compra, Double donacionExtra) {
        BigDecimal total = calcularTotalCompra(compra);
        if (donacionExtra != null && donacionExtra > 0) {
            total = total.add(BigDecimal.valueOf(donacionExtra));
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

    private void bloquearAsientos(List<Long> asientoIds) {
        if (log.isInfoEnabled()) {
            log.info("Bloqueando {} asientos: {}", asientoIds.size(), asientoIds);
        }

        List<Asiento> asientos = asientoRepository.findAllByIdWithLock(asientoIds);

        if (asientos.size() != asientoIds.size()) {
            throw new IllegalArgumentException("Asientos no encontrados");
        }

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

        asientos.forEach(asiento -> asiento.setEstado(EstadoAsiento.BLOQUEADO));
        asientoRepository.saveAll(asientos);
    }
}