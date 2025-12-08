package com.cudeca.service;

import com.cudeca.dto.UserProfileDTO;
import com.cudeca.model.usuario.Usuario;

import java.util.Optional;

/**
 * Servicio para gestionar el perfil de usuario.
 * Permite consultar y actualizar la información personal del usuario.
 */
public interface PerfilUsuarioService {

    /**
     * Obtiene el perfil completo de un usuario por su ID.
     *
     * @param usuarioId ID del usuario
     * @return DTO con la información del perfil
     * @throws IllegalArgumentException si el usuario no existe
     */
    UserProfileDTO obtenerPerfilPorId(Long usuarioId);

    /**
     * Obtiene el perfil de un usuario por su email.
     *
     * @param email Email del usuario
     * @return Optional con el DTO del perfil si existe
     */
    Optional<UserProfileDTO> obtenerPerfilPorEmail(String email);

    /**
     * Actualiza el perfil de un usuario.
     *
     * @param usuarioId ID del usuario a actualizar
     * @param nombre Nuevo nombre (opcional)
     * @param direccion Nueva dirección (opcional)
     * @return DTO con el perfil actualizado
     * @throws IllegalArgumentException si el usuario no existe
     */
    UserProfileDTO actualizarPerfil(Long usuarioId, String nombre, String direccion);

    /**
     * Obtiene la entidad Usuario completa por ID.
     *
     * @param usuarioId ID del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> obtenerUsuarioPorId(Long usuarioId);

    /**
     * Convierte una entidad Usuario a DTO de perfil.
     *
     * @param usuario Entidad Usuario
     * @return DTO con información del perfil
     */
    UserProfileDTO convertirAPerfilDTO(Usuario usuario);

    /**
     * Verifica si un usuario existe.
     *
     * @param usuarioId ID del usuario
     * @return true si el usuario existe
     */
    boolean existeUsuario(Long usuarioId);
}

