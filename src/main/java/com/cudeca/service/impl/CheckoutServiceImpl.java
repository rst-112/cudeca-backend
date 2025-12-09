package com.cudeca.service.impl;

import com.cudeca.dto.CheckoutRequest;
import com.cudeca.dto.CheckoutResponse;
import com.cudeca.model.enums.EstadoCompra;
import com.cudeca.model.enums.TipoItem;
import com.cudeca.model.negocio.ArticuloCompra;
import com.cudeca.model.negocio.ArticuloDonacion;
import com.cudeca.model.negocio.ArticuloEntrada;
import com.cudeca.model.negocio.Compra;
import com.cudeca.model.evento.TipoEntrada;
import com.cudeca.model.usuario.Invitado;
import com.cudeca.model.usuario.Usuario;
import com.cudeca.repository.CompraRepository;
import com.cudeca.repository.InvitadoRepository;
import com.cudeca.repository.TipoEntradaRepository;
import com.cudeca.repository.UsuarioRepository;
import com.cudeca.service.CheckoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio de Checkout.
 * Maneja la lógica de negocio para el proceso de compra.
 */
@Service
@Transactional
public class CheckoutServiceImpl implements CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutServiceImpl.class);

    private static final String COMPRA_NO_ENCONTRADA = "Compra no encontrada: ";

    private final CompraRepository compraRepository;
    private final UsuarioRepository usuarioRepository;
    private final InvitadoRepository invitadoRepository;
    private final TipoEntradaRepository tipoEntradaRepository;

    public CheckoutServiceImpl(
            CompraRepository compraRepository,
            UsuarioRepository usuarioRepository,
            InvitadoRepository invitadoRepository,
            TipoEntradaRepository tipoEntradaRepository) {
        this.compraRepository = compraRepository;
        this.usuarioRepository = usuarioRepository;
        this.invitadoRepository = invitadoRepository;
        this.tipoEntradaRepository = tipoEntradaRepository;
    }

    @Override
    public CheckoutResponse procesarCheckout(CheckoutRequest request) {
        log.info("Procesando checkout para usuario ID: {}", request.getUsuarioId());

        // 1. Validar datos básicos
        validarCheckoutRequest(request);

        // 2. Crear la compra
        Compra compra = crearCompra(request);

        // 3. Procesar items del carrito
        procesarItems(request, compra);

        // 4. Calcular total
        BigDecimal total = calcularTotal(compra, request.getDonacionExtra());

        // 5. Guardar compra
        compra = compraRepository.save(compra);

        log.info("Compra creada con ID: {} y total: {}", compra.getId(), total);

        // 6. Preparar respuesta
        return construirRespuesta(compra, total);
    }

    @Override
    public boolean confirmarPago(Long compraId) {
        log.info("Confirmando pago para compra ID: {}", compraId);

        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada: " + compraId));

        if (compra.getEstado() != EstadoCompra.PENDIENTE) {
            log.warn("Intento de confirmar compra en estado: {}", compra.getEstado());
            return false;
        }

        compra.setEstado(EstadoCompra.COMPLETADA);
        compraRepository.save(compra);

        log.info("Pago confirmado para compra ID: {}", compraId);
        return true;
    }

    @Override
    public boolean cancelarCompra(Long compraId, String motivo) {
        log.info("Cancelando compra ID: {} - Motivo: {}", compraId, motivo);

        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada: " + compraId));

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
                .fecha(Instant.now())
                .estado(EstadoCompra.PENDIENTE)
                .emailContacto(request.getEmailContacto())
                .articulos(new ArrayList<>());

        // Asociar usuario o invitado
        if (request.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            builder.usuario(usuario);
        } else {
            // Buscar o crear invitado por email
            Invitado invitado = invitadoRepository.findByEmail(request.getEmailContacto())
                    .orElseGet(() -> {
                        Invitado nuevoInvitado = new Invitado();
                        nuevoInvitado.setEmail(request.getEmailContacto());
                        return invitadoRepository.save(nuevoInvitado);
                    });
            builder.invitado(invitado);
        }

        return builder.build();
    }

    private void procesarItems(CheckoutRequest request, Compra compra) {
        for (CheckoutRequest.ItemDTO itemDTO : request.getItems()) {
            ArticuloCompra articulo = crearArticulo(itemDTO, compra);
            compra.getArticulos().add(articulo);
        }
    }

    private ArticuloCompra crearArticulo(CheckoutRequest.ItemDTO itemDTO, Compra compra) {
        TipoItem tipoItem = TipoItem.valueOf(itemDTO.getTipo().toUpperCase());

        switch (tipoItem) {
            case ENTRADA:
                return crearArticuloEntrada(itemDTO, compra);
            case DONACION:
                return crearArticuloDonacion(itemDTO, compra);
            default:
                throw new IllegalArgumentException("Tipo de item no soportado: " + itemDTO.getTipo());
        }
    }

    private ArticuloEntrada crearArticuloEntrada(CheckoutRequest.ItemDTO itemDTO, Compra compra) {
        TipoEntrada tipoEntrada = tipoEntradaRepository.findById(itemDTO.getReferenciaId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "TipoEntrada no encontrado: " + itemDTO.getReferenciaId()));

        ArticuloEntrada articulo = ArticuloEntrada.builder()
                .tipoEntrada(tipoEntrada)
                .entradasEmitidas(new ArrayList<>())
                .build();
        
        articulo.setCompra(compra);
        // articulo.setTipo(TipoItem.ENTRADA);  <-- BORRADO (JPA lo hace automático)
        
        articulo.setCantidad(itemDTO.getCantidad());
        articulo.setSolicitaCertificado(false);
        
        BigDecimal precio = itemDTO.getPrecio() != null ?
                BigDecimal.valueOf(itemDTO.getPrecio()) : tipoEntrada.getCosteBase();
        articulo.setPrecioUnitario(precio);
        
        return articulo;
    }

    private ArticuloDonacion crearArticuloDonacion(CheckoutRequest.ItemDTO itemDTO, Compra compra) {
        return ArticuloDonacion.builder()
                //.compra(compra)
                .cantidad(itemDTO.getCantidad())
                .precioUnitario(BigDecimal.valueOf(itemDTO.getPrecio()))
                //.tipo(TipoItem.DONACION)
                .solicitaCertificado(false)
                .destino("General") // Valor por defecto
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
                .map(articulo -> articulo.getPrecioUnitario()
                        .multiply(BigDecimal.valueOf(articulo.getCantidad())))
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
        switch (estado) {
            case PENDIENTE:
                return "Compra creada. Pendiente de pago.";
            case COMPLETADA:
                return "Compra completada exitosamente.";
            case CANCELADA:
                return "Compra cancelada.";
            default:
                return "Estado: " + estado.name();
        }
    }
}

