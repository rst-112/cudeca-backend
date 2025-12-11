package com.cudeca.service;

import com.cudeca.model.usuario.DatosFiscales;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar los datos fiscales (libreta de direcciones de facturación).
 * Permite a los usuarios guardar y gestionar múltiples perfiles fiscales (NIFs).
 */
public interface DatosFiscalesService {

    /**
     * Crea un nuevo perfil fiscal para un usuario.
     *
     * @param datosFiscales Datos fiscales a guardar
     * @param usuarioId ID del usuario propietario
     * @return El perfil fiscal guardado
     * @throws IllegalArgumentException si los datos son inválidos o el usuario no existe
     */
    DatosFiscales crearDatosFiscales(DatosFiscales datosFiscales, Long usuarioId);

    /**
     * Actualiza un perfil fiscal existente.
     *
     * @param id ID del perfil fiscal a actualizar
     * @param datosFiscales Nuevos datos fiscales
     * @param usuarioId ID del usuario (para validar propiedad)
     * @return El perfil fiscal actualizado
     * @throws IllegalArgumentException si el perfil no existe o no pertenece al usuario
     */
    DatosFiscales actualizarDatosFiscales(Long id, DatosFiscales datosFiscales, Long usuarioId);

    /**
     * Elimina un perfil fiscal.
     *
     * @param id ID del perfil fiscal
     * @param usuarioId ID del usuario (para validar propiedad)
     * @return true si se eliminó correctamente
     */
    boolean eliminarDatosFiscales(Long id, Long usuarioId);

    /**
     * Obtiene todos los perfiles fiscales de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Lista de perfiles fiscales del usuario
     */
    List<DatosFiscales> obtenerDatosFiscalesPorUsuario(Long usuarioId);

    /**
     * Obtiene un perfil fiscal por ID.
     *
     * @param id ID del perfil fiscal
     * @param usuarioId ID del usuario (para validar propiedad)
     * @return Optional con el perfil fiscal si existe y pertenece al usuario
     */
    Optional<DatosFiscales> obtenerDatosFiscalesPorId(Long id, Long usuarioId);

    /**
     * Valida que un NIF sea válido.
     *
     * @param nif NIF a validar
     * @return true si el NIF es válido
     */
    boolean validarNIF(String nif);
}

