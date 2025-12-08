package com.cudeca.service.impl;

import com.cudeca.dto.UserProfileDTO;
import com.cudeca.model.negocio.Monedero;
import com.cudeca.model.usuario.Comprador;
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

    private final UsuarioRepository usuarioRepository;
    private final MonederoRepository monederoRepository;

    public PerfilUsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            MonederoRepository monederoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.monederoRepository = monederoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO obtenerPerfilPorId(Long usuarioId) {
        log.debug("Obteniendo perfil para usuario ID: {}", usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));

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
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));

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
            // Si el usuario es un Comprador, intentar obtener su monedero
            if (usuario instanceof Comprador) {
                Optional<Monedero> monedero = monederoRepository.findByComprador_Id(usuario.getId());
                if (monedero.isPresent()) {
                    return monedero.get().getSaldo();
                }
            }
        } catch (Exception e) {
            log.warn("Error al obtener saldo del monedero para usuario ID: {}", usuario.getId(), e);
        }

        return BigDecimal.ZERO;
    }
}

