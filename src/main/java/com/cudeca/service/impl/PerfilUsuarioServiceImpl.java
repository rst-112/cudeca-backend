package com.cudeca.service.impl;

import com.cudeca.dto.EntradaUsuarioDTO;
import com.cudeca.dto.TicketDTO;
import com.cudeca.dto.UserProfileDTO;
import com.cudeca.model.evento.Asiento;
import com.cudeca.model.negocio.*;
import com.cudeca.model.usuario.Rol;
import com.cudeca.model.usuario.Usuario;
import com.cudeca.repository.CompraRepository;
import com.cudeca.repository.EntradaEmitidaRepository;
import com.cudeca.repository.MonederoRepository;
import com.cudeca.repository.UsuarioRepository;
import com.cudeca.service.PdfService;
import com.cudeca.service.PerfilUsuarioService;
import com.cudeca.service.QrCodeService;
import com.cudeca.service.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Implementación del servicio de gestión de perfil de usuario.
 */
@Service
@Transactional
public class PerfilUsuarioServiceImpl implements PerfilUsuarioService {

    private static final Logger log = LoggerFactory.getLogger(PerfilUsuarioServiceImpl.class);
    private static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado: ";

    private final UsuarioRepository usuarioRepository;
    private final MonederoRepository monederoRepository;
    private final CompraRepository compraRepository;
    private final EntradaEmitidaRepository entradaEmitidaRepository;
    private final TicketService ticketService;
    private final PdfService pdfService;
    private final QrCodeService qrCodeService;

    public PerfilUsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            MonederoRepository monederoRepository,
            CompraRepository compraRepository,
            EntradaEmitidaRepository entradaEmitidaRepository,
            TicketService ticketService,
            PdfService pdfService,
            QrCodeService qrCodeService) {
        this.usuarioRepository = usuarioRepository;
        this.monederoRepository = monederoRepository;
        this.compraRepository = compraRepository;
        this.entradaEmitidaRepository = entradaEmitidaRepository;
        this.ticketService = ticketService;
        this.pdfService = pdfService;
        this.qrCodeService = qrCodeService;
    }

    // --- MÉTODOS DE PERFIL ---

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO obtenerPerfilPorId(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(USUARIO_NO_ENCONTRADO + usuarioId));
        return convertirAPerfilDTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfileDTO> obtenerPerfilPorEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return usuarioRepository.findByEmail(email).map(this::convertirAPerfilDTO);
    }

    @Override
    public UserProfileDTO actualizarPerfil(Long usuarioId, String nombre, String direccion) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(USUARIO_NO_ENCONTRADO + usuarioId));

        if (nombre != null && !nombre.isBlank()) {
            if (nombre.length() > 100) throw new IllegalArgumentException("El nombre no puede exceder 100 caracteres");
            usuario.setNombre(nombre);
        }
        if (direccion != null) {
            usuario.setDireccion(direccion);
        }

        return convertirAPerfilDTO(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerUsuarioPorId(Long usuarioId) {
        return usuarioRepository.findById(usuarioId);
    }

    @Override
    public boolean existeUsuario(Long usuarioId) {
        return usuarioRepository.existsById(usuarioId);
    }

    // --- CONVERSORES Y DTOs ---

    @Override
    public UserProfileDTO convertirAPerfilDTO(Usuario usuario) {
        if (usuario == null) return null;
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setDireccion(usuario.getDireccion());

        String rol = usuario.getRoles().stream().findFirst().map(Rol::getNombre).orElse("COMPRADOR");
        dto.setRol(rol);

        dto.setSaldoMonedero(obtenerSaldoMonedero(usuario));
        return dto;
    }

    private BigDecimal obtenerSaldoMonedero(Usuario usuario) {
        return monederoRepository.findByUsuario_Id(usuario.getId())
                .map(Monedero::getSaldo)
                .orElse(BigDecimal.ZERO);
    }

    // --- HISTORIAL DE COMPRAS ---

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerHistorialCompras(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new IllegalArgumentException(USUARIO_NO_ENCONTRADO + usuarioId);
        }

        List<Compra> compras = compraRepository.findByUsuario_Id(usuarioId);

        return compras.stream().sorted(Comparator.comparing(Compra::getFecha).reversed()).map(compra -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", compra.getId().toString());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM, yyyy", new Locale("es", "ES"));
            dto.put("date", compra.getFecha().atZoneSameInstant(ZoneId.systemDefault()).format(formatter));
            dto.put("status", compra.getEstado().name());

            BigDecimal total = compra.getArticulos().stream()
                    .map(a -> a.getPrecioUnitario().multiply(new BigDecimal(a.getCantidad())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.put("total", total + "€");

            String titulo = "Compra General";
            int numEntradas = 0;
            for (ArticuloCompra art : compra.getArticulos()) {
                if (art instanceof ArticuloEntrada ent) {
                    numEntradas += ent.getCantidad();
                    if (ent.getTipoEntrada() != null && ent.getTipoEntrada().getEvento() != null) {
                        titulo = ent.getTipoEntrada().getEvento().getNombre();
                    }
                }
            }
            dto.put("title", titulo);
            dto.put("tickets", numEntradas + " entradas");
            return dto;
        }).toList();
    }

    // --- GESTIÓN DE ENTRADAS (Fixed) ---

    @Override
    @Transactional(readOnly = true)
    public List<EntradaUsuarioDTO> obtenerEntradasUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new IllegalArgumentException(USUARIO_NO_ENCONTRADO + usuarioId);
        }

        List<Compra> compras = compraRepository.findByUsuario_Id(usuarioId);

        return compras.stream()
                .flatMap(compra -> compra.getArticulos().stream())
                .filter(articulo -> articulo instanceof ArticuloEntrada)
                .map(articulo -> (ArticuloEntrada) articulo)
                .flatMap(articuloEntrada -> articuloEntrada.getEntradasEmitidas().stream())
                .sorted((e1, e2) -> e2.getId().compareTo(e1.getId()))
                .map(this::convertirAEntradaDTO)
                .toList();
    }

    private EntradaUsuarioDTO convertirAEntradaDTO(EntradaEmitida entrada) {
        var articulo = entrada.getArticuloEntrada();
        var evento = articulo.getTipoEntrada().getEvento();
        Asiento asiento = articulo.getAsiento();

        String asientoTexto;
        if (asiento != null) {
            asientoTexto = "Fila " + asiento.getFila() + " - " + asiento.getCodigoEtiqueta();
        } else {
            asientoTexto = articulo.getTipoEntrada().getNombre();
        }

        return EntradaUsuarioDTO.builder()
                .id(entrada.getId())
                .codigoQR(entrada.getCodigoQR())
                .estadoEntrada(entrada.getEstado().name())
                .eventoNombre(evento.getNombre())
                .fechaEvento(evento.getFechaInicio().toString())
                .fechaEmision(articulo.getCompra().getFecha().toString())
                .asientoNumero(asientoTexto)
                .build();
    }

    // --- GENERACIÓN DE PDF (Fixed) ---

    @Override
    @Transactional(readOnly = true)
    public byte[] generarPDFEntrada(Long entradaId, Long usuarioId) {
        log.info("Generando PDF REAL para entrada ID: {} del usuario ID: {}", entradaId, usuarioId);

        EntradaEmitida entrada = entradaEmitidaRepository.findById(entradaId)
                .orElseThrow(() -> new IllegalArgumentException("Entrada no encontrada: " + entradaId));

        Long compradorId = entrada.getArticuloEntrada().getCompra().getUsuario().getId();
        if (!compradorId.equals(usuarioId)) {
            throw new SecurityException("Acceso denegado: Esta entrada no pertenece al usuario.");
        }

        // Mapear a TicketDTO
        TicketDTO ticketDTO = mapearEntradaADTO(entrada);

        // Usar el servicio de tickets real dentro de un try-catch
        try {
            return ticketService.generarTicketPdf(ticketDTO);
        } catch (Exception e) {
            log.error("Error generando PDF para entrada {}", entradaId, e);
            throw new RuntimeException("Error interno al generar el PDF del ticket", e);
        }
    }

    private TicketDTO mapearEntradaADTO(EntradaEmitida entrada) {
        var articulo = entrada.getArticuloEntrada();
        var compra = articulo.getCompra();
        var evento = articulo.getTipoEntrada().getEvento();
        var usuario = compra.getUsuario();

        String nombreUser = (usuario != null) ? usuario.getNombre() : "Invitado";
        String emailUser = (usuario != null) ? usuario.getEmail() : compra.getEmailContacto();

        return TicketDTO.builder()
                .nombreEvento(evento.getNombre())
                .lugarEvento(evento.getLugar())
                .fechaEventoFormato(evento.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .descripcionEvento(evento.getDescripcion())
                .nombreUsuario(nombreUser)
                .emailUsuario(emailUser)
                .codigoAsiento(articulo.getAsiento() != null ? articulo.getAsiento().getCodigoEtiqueta() : "General")
                .fila(articulo.getAsiento() != null ? articulo.getAsiento().getFila() : null)
                .columna(articulo.getAsiento() != null ? articulo.getAsiento().getColumna() : null)
                .zonaRecinto(articulo.getAsiento() != null ? articulo.getAsiento().getZona().getNombre() : "General")
                .codigoQR(entrada.getCodigoQR())
                .tipoEntrada(articulo.getTipoEntrada().getNombre())
                .precio(articulo.getPrecioUnitario() + "€")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarResumenCompraPdf(Long compraId, Long usuarioId) {
        log.info("Generando PDF de resumen de compra ID: {} para usuario ID: {}", compraId, usuarioId);

        // 1. Validar Compra
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada: " + compraId));

        if (!compra.getUsuario().getId().equals(usuarioId)) {
            throw new SecurityException("No tienes permiso para ver esta compra");
        }

        // 2. Obtener todas las entradas asociadas a esta compra usando el nuevo método del repositorio
        List<EntradaEmitida> entradasEntidad = entradaEmitidaRepository.findByCompraId(compraId);

        if (entradasEntidad.isEmpty()) {
            // Opcional: Si es una donación pura sin entradas, podrías querer manejarlo diferente,
            // pero el PdfService debería ser capaz de pintar solo la factura.
            log.info("La compra {} no tiene entradas asociadas (posiblemente solo donaciones).", compraId);
        }

        // 3. Preparar datos para el PDF
        List<TicketDTO> ticketsDTO = new ArrayList<>();
        List<byte[]> codigosQR = new ArrayList<>();

        for (EntradaEmitida entrada : entradasEntidad) {
            ticketsDTO.add(mapearEntradaADTO(entrada));
            try {
                // Generamos el QR en memoria para incrustarlo en el PDF
                codigosQR.add(qrCodeService.generarCodigoQR(entrada.getCodigoQR()));
            } catch (Exception e) {
                log.error("Error generando QR para entrada {}", entrada.getId(), e);
                throw new RuntimeException("Error interno generando códigos QR", e);
            }
        }

        // 4. Generar PDF Combinado (Factura + Entradas)
        try {
            return pdfService.generarPdfCompraCompleta(compra, ticketsDTO, codigosQR);
        } catch (Exception e) {
            log.error("Error generando PDF de compra completa", e);
            throw new RuntimeException("Error al generar el documento PDF", e);
        }
    }

    // --- MONEDERO ---

    @Override
    @Transactional(readOnly = true)
    public Monedero obtenerMonedero(Long usuarioId) {
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(USUARIO_NO_ENCONTRADO + usuarioId));
        return monederoRepository.findByUsuario_Id(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no tiene monedero configurado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoMonedero> obtenerMovimientosMonedero(Long usuarioId) {
        Monedero monedero = obtenerMonedero(usuarioId);
        List<MovimientoMonedero> movimientos = new ArrayList<>(monedero.getMovimientos());
        movimientos.sort(Comparator.comparing(MovimientoMonedero::getFecha).reversed());
        return movimientos;
    }
}