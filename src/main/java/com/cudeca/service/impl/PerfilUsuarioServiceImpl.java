package com.cudeca.service.impl;

import com.cudeca.dto.UserProfileDTO;
import com.cudeca.model.negocio.Monedero;
import com.cudeca.model.usuario.Rol;
import com.cudeca.model.usuario.Usuario;
import com.cudeca.repository.MonederoRepository;
import com.cudeca.repository.UsuarioRepository;
import com.cudeca.service.PerfilUsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Implementación del servicio de gestión de perfil de usuario.
 * Maneja consultas y actualizaciones del perfil del usuario.
 */
@Service
@Transactional
public class PerfilUsuarioServiceImpl implements PerfilUsuarioService {

    private static final Logger log = LoggerFactory.getLogger(PerfilUsuarioServiceImpl.class);
    private static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado: ";

    private final UsuarioRepository usuarioRepository;
    private final MonederoRepository monederoRepository;
    private final com.cudeca.repository.CompraRepository compraRepository;

    public PerfilUsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            MonederoRepository monederoRepository,
            com.cudeca.repository.CompraRepository compraRepository) {
        this.usuarioRepository = usuarioRepository;
        this.monederoRepository = monederoRepository;
        this.compraRepository = compraRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO obtenerPerfilPorId(Long usuarioId) {
        log.debug("Obteniendo perfil para usuario ID: {}", usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(USUARIO_NO_ENCONTRADO + usuarioId));

        return convertirAPerfilDTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfileDTO> obtenerPerfilPorEmail(String email) {
        log.debug("Obteniendo perfil para email: {}", email);

        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        return usuarioRepository.findByEmail(email)
                .map(this::convertirAPerfilDTO);
    }

    @Override
    public UserProfileDTO actualizarPerfil(Long usuarioId, String nombre, String direccion) {
        log.info("Actualizando perfil para usuario ID: {}", usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(USUARIO_NO_ENCONTRADO + usuarioId));

        // Validar y actualizar campos
        if (nombre != null && !nombre.isBlank()) {
            if (nombre.length() > 100) {
                throw new IllegalArgumentException("El nombre no puede exceder 100 caracteres");
            }
            usuario.setNombre(nombre);
            log.debug("Nombre actualizado a: {}", nombre);
        }

        if (direccion != null) {
            usuario.setDireccion(direccion);
            log.debug("Dirección actualizada");
        }

        // Guardar cambios
        Usuario actualizado = usuarioRepository.save(usuario);
        log.info("Perfil actualizado para usuario ID: {}", usuarioId);

        return convertirAPerfilDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerUsuarioPorId(Long usuarioId) {
        log.debug("Obteniendo usuario por ID: {}", usuarioId);
        return usuarioRepository.findById(usuarioId);
    }

    @Override
    public UserProfileDTO convertirAPerfilDTO(Usuario usuario) {
        return crearPerfilDTODesdeUsuario(usuario);
    }

    /**
     * Método privado para crear DTO desde Usuario (evita warnings de transacciones).
     */
    private UserProfileDTO crearPerfilDTODesdeUsuario(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setDireccion(usuario.getDireccion());

        // Obtener rol principal (el primero si tiene varios)
        String rol = usuario.getRoles().stream()
                .findFirst()
                .map(Rol::getNombre)
                .orElse("COMPRADOR");
        dto.setRol(rol);

        // Obtener saldo del monedero si el usuario es Comprador
        BigDecimal saldo = obtenerSaldoMonedero(usuario);
        dto.setSaldoMonedero(saldo);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeUsuario(Long usuarioId) {
        log.debug("Verificando existencia de usuario ID: {}", usuarioId);
        return usuarioRepository.existsById(usuarioId);
    }

    // --- MÉTODOS PRIVADOS DE APOYO ---

    private BigDecimal obtenerSaldoMonedero(Usuario usuario) {
        try {
            // Buscar el monedero del usuario por su ID
            Optional<Monedero> monedero = monederoRepository.findByUsuario_Id(usuario.getId());
            if (monedero.isPresent()) {
                return monedero.get().getSaldo();
            }
        } catch (Exception e) {
            log.warn("Error al obtener saldo del monedero para usuario ID: {}", usuario.getId(), e);
        }

        return BigDecimal.ZERO;
    }

    // --- MÉTODOS PARA ENTRADAS Y TICKETS ---

    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.cudeca.model.negocio.EntradaEmitida> obtenerEntradasUsuario(Long usuarioId) {
        log.debug("Obteniendo entradas para usuario ID: {}", usuarioId);

        // Verificar que el usuario existe
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new IllegalArgumentException(USUARIO_NO_ENCONTRADO + usuarioId);
        }

        // Obtener todas las compras del usuario usando el repositorio
        java.util.List<com.cudeca.model.negocio.Compra> compras = compraRepository.findByUsuario_Id(usuarioId);

        // Extraer las entradas de los artículos de tipo ArticuloEntrada
        return compras.stream()
                .flatMap(compra -> compra.getArticulos().stream())
                .filter(articulo -> articulo instanceof com.cudeca.model.negocio.ArticuloEntrada)
                .map(articulo -> (com.cudeca.model.negocio.ArticuloEntrada) articulo)
                .flatMap(articuloEntrada -> articuloEntrada.getEntradasEmitidas().stream())
                .sorted((e1, e2) -> e2.getId().compareTo(e1.getId())) // Más recientes primero
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarPDFEntrada(Long entradaId, Long usuarioId) {
        log.info("Generando PDF para entrada ID: {} del usuario ID: {}", entradaId, usuarioId);

        // Verificar que el usuario existe
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new IllegalArgumentException(USUARIO_NO_ENCONTRADO + usuarioId);
        }

        // Obtener las entradas del usuario
        java.util.List<com.cudeca.model.negocio.EntradaEmitida> entradas = obtenerEntradasUsuario(usuarioId);

        // Verificar que la entrada existe y pertenece al usuario
        com.cudeca.model.negocio.EntradaEmitida entrada = entradas.stream()
                .filter(e -> e.getId().equals(entradaId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Entrada no encontrada o no pertenece al usuario: " + entradaId));

        // TODO: Implementar generación real de PDF con iText o similar
        // Por ahora, retornamos un PDF simulado con información básica
        String contenidoPDF = generarContenidoPDFSimulado(entrada);

        log.info("PDF generado exitosamente para entrada ID: {}", entradaId);
        return contenidoPDF.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Genera contenido simulado de PDF (placeholder hasta implementar librería PDF real).
     */
    private String generarContenidoPDFSimulado(com.cudeca.model.negocio.EntradaEmitida entrada) {
        StringBuilder pdf = new StringBuilder();
        pdf.append("========================================\n");
        pdf.append("         ENTRADA - CUDECA EVENT          \n");
        pdf.append("========================================\n\n");
        pdf.append("ID Entrada: ").append(entrada.getId()).append("\n");
        pdf.append("Código QR: ").append(entrada.getCodigoQR()).append("\n");
        pdf.append("Estado: ").append(entrada.getEstado()).append("\n\n");

        // Información del asiento a través del artículo de entrada
        if (entrada.getArticuloEntrada() != null && entrada.getArticuloEntrada().getAsiento() != null) {
            com.cudeca.model.evento.Asiento asiento = entrada.getArticuloEntrada().getAsiento();
            pdf.append("--- INFORMACIÓN DEL ASIENTO ---\n");
            pdf.append("Código: ").append(asiento.getCodigoEtiqueta()).append("\n");
            if (asiento.getFila() != null) {
                pdf.append("Fila: ").append(asiento.getFila()).append("\n");
            }
            if (asiento.getColumna() != null) {
                pdf.append("Columna: ").append(asiento.getColumna()).append("\n");
            }

            // Información de la zona
            if (asiento.getZona() != null) {
                pdf.append("Zona: ").append(asiento.getZona().getNombre()).append("\n");

                // Información del evento
                if (asiento.getZona().getEvento() != null) {
                    com.cudeca.model.evento.Evento evento = asiento.getZona().getEvento();
                    pdf.append("\n--- INFORMACIÓN DEL EVENTO ---\n");
                    pdf.append("Evento: ").append(evento.getNombre()).append("\n");
                    if (evento.getDescripcion() != null) {
                        pdf.append("Descripción: ").append(evento.getDescripcion()).append("\n");
                    }
                    pdf.append("Fecha: ").append(evento.getFechaInicio()).append("\n");
                }
            }
        }

        pdf.append("\n========================================\n");
        pdf.append("   Conserve esta entrada para el evento  \n");
        pdf.append("========================================\n");

        return pdf.toString();
    }

    // --- MÉTODOS PARA MONEDERO ---

    @Override
    @Transactional(readOnly = true)
    public com.cudeca.model.negocio.Monedero obtenerMonedero(Long usuarioId) {
        log.debug("Obteniendo monedero para usuario ID: {}", usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(USUARIO_NO_ENCONTRADO + usuarioId));

        return monederoRepository.findByUsuario_Id(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario no tiene monedero configurado: " + usuarioId));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.cudeca.model.negocio.MovimientoMonedero> obtenerMovimientosMonedero(Long usuarioId) {
        log.debug("Obteniendo movimientos del monedero para usuario ID: {}", usuarioId);

        // Primero obtener el monedero (esto valida que el usuario existe y es comprador)
        com.cudeca.model.negocio.Monedero monedero = obtenerMonedero(usuarioId);

        // Obtener los movimientos ordenados por fecha descendente (más recientes primero)
        java.util.List<com.cudeca.model.negocio.MovimientoMonedero> movimientos =
                new java.util.ArrayList<>(monedero.getMovimientos());

        movimientos.sort((m1, m2) -> m2.getFecha().compareTo(m1.getFecha()));

        log.debug("Se encontraron {} movimientos para el monedero del usuario ID: {}",
                movimientos.size(), usuarioId);

        return movimientos;
    }
}

