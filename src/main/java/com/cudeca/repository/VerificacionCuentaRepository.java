package com.cudeca.repository;

import com.cudeca.model.usuario.VerificacionCuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar los Tokens de verificación (OTP/Enlaces).
 * Se usa para:
 * 1. Recuperación de contraseñas.
 * 2. Fusión de cuentas (Invitado -> Usuario Registrado).
 */
@Repository
public interface VerificacionCuentaRepository extends JpaRepository<VerificacionCuenta, Long> {

    /**
     * Busca una verificación por el token único (el string largo).
     * Fundamental: Es lo primero que se llama cuando el usuario hace clic en el enlace del email.
     *
     * @param token El código de verificación.
     * @return Optional con la verificación si existe.
     */
    Optional<VerificacionCuenta> findByToken(String token);

    /**
     * Encuentra todas las verificaciones asociadas a un email.
     * Útil para:
     * - Auditoría: "¿Cuántas veces ha pedido recuperar contraseña este usuario?"
     * - Limpieza: "Borrar tokens antiguos de este email".
     */
    List<VerificacionCuenta> findByEmail(String email);

    /**
     * Invalida (marca como usados) todos los tokens activos previos de un usuario.
     * Esto evita que si un usuario solicita restablecer contraseña 3 veces,
     * los 2 enlaces antiguos sigan funcionando. Solo el último (que se creará después de esta llamada) valdrá.
     *
     * @param usuarioId ID del usuario
     */
    @Modifying
    @Query("UPDATE VerificacionCuenta v SET v.usado = true WHERE v.usuario.id = :usuarioId AND v.usado = false")
    void anularTokensPrevios(@Param("usuarioId") Long usuarioId);
}
