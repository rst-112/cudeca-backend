package com.cudeca.repository;

import com.cudeca.model.usuario.VerificacionCuenta;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
