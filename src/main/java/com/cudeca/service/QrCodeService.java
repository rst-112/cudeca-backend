package com.cudeca.service;

/**
 * Interfaz para la generación de códigos QR.
 * Permite crear QR en formato de imagen (bytes) que pueden ser
 * incrustados en PDFs u otros documentos.
 */
public interface QrCodeService {

    /**
     * Genera un código QR a partir de un texto.
     *
     * @param contenido El contenido que se codificará en el QR
     * @param ancho Ancho de la imagen QR en píxeles
     * @param alto Alto de la imagen QR en píxeles
     * @return Array de bytes con la imagen QR en formato PNG
     * @throws Exception Si ocurre un error durante la generación
     */
    byte[] generarCodigoQR(String contenido, int ancho, int alto) throws Exception;

    /**
     * Genera un código QR a partir de un texto con tamaño por defecto (250x250).
     *
     * @param contenido El contenido que se codificará en el QR
     * @return Array de bytes con la imagen QR en formato PNG
     * @throws Exception Si ocurre un error durante la generación
     */
    byte[] generarCodigoQR(String contenido) throws Exception;
}

