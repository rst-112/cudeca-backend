package com.cudeca.exception;

import lombok.Getter;
import java.io.Serial;

/**
 * Excepción lanzada cuando se intenta operar sobre un asiento que no está disponible.
 * Se utiliza principalmente en operaciones de compra y bloqueo de asientos.
 */
@Getter
public class AsientoNoDisponibleException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    private final String codigoError;

    /**
     * Constructor con mensaje de error.
     *
     * @param mensaje Descripción del error
     */
    public AsientoNoDisponibleException(String mensaje) {
        super(mensaje);
        this.codigoError = "ASIENTO_NO_DISPONIBLE";
    }

    /**
     * Constructor con mensaje de error y causa.
     *
     * @param mensaje Descripción del error
     * @param causa   Excepción que causó este error
     */
    public AsientoNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigoError = "ASIENTO_NO_DISPONIBLE";
    }

    /**
     * Constructor con mensaje de error y código de error personalizado.
     *
     * @param mensaje      Descripción del error
     * @param codigoError  Código de error personalizado
     */
    public AsientoNoDisponibleException(String mensaje, String codigoError) {
        super(mensaje);
        this.codigoError = codigoError;
    }

    /**
     * Constructor con mensaje de error, código de error y causa.
     *
     * @param mensaje      Descripción del error
     * @param codigoError  Código de error personalizado
     * @param causa        Excepción que causó este error
     */
    public AsientoNoDisponibleException(String mensaje, String codigoError, Throwable causa) {
        super(mensaje, causa);
        this.codigoError = codigoError;
    }
}
