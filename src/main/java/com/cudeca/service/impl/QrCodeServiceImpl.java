package com.cudeca.service.impl;

import com.cudeca.service.QrCodeService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/**
 * Implementación de QrCodeService utilizando la librería ZXing.
 *
 * ZXing es una librería robusta, ampliamente utilizada y mantiene excelente balance
 * entre funcionalidad y simplicidad. Es la opción estándar para generación de códigos QR en Java.
 */
@Service
@Slf4j
public class QrCodeServiceImpl implements QrCodeService {

    private static final int TAMAÑO_DEFECTO_ANCHO = 250;
    private static final int TAMAÑO_DEFECTO_ALTO = 250;

    /**
     * Genera un código QR a partir de un contenido especificado.
     * Utiliza ZXing para crear la imagen QR y la retorna como array de bytes en PNG.
     *
     * @param contenido Texto que se codificará en el QR
     * @param ancho Ancho deseado en píxeles
     * @param alto Alto deseado en píxeles
     * @return Array de bytes con la imagen QR en formato PNG
     * @throws IOException Si ocurre un error al escribir la imagen
     * @throws WriterException Si ocurre un error al generar el código QR
     */
    @Override
    public byte[] generarCodigoQR(String contenido, int ancho, int alto) throws IOException, WriterException {
        log.debug("Generando código QR para contenido: {}", contenido);

        try {
            // Crear generador QR
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            // Generar la matriz de bits del QR
            BitMatrix bitMatrix = qrCodeWriter.encode(contenido, BarcodeFormat.QR_CODE, ancho, alto);

            // Convertir la matriz a imagen BufferedImage
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            // Convertir BufferedImage a array de bytes en PNG
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", outputStream);

            log.debug("Código QR generado exitosamente. Tamaño: {} bytes", outputStream.size());

            return outputStream.toByteArray();

        } catch (WriterException e) {
            log.error("Error al codificar el QR: {}", e.getMessage(), e);
            throw e;
        } catch (IOException e) {
            log.error("Error de entrada/salida al generar QR: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Genera un código QR con tamaño por defecto (250x250 píxeles).
     *
     * @param contenido Texto que se codificará en el QR
     * @return Array de bytes con la imagen QR en formato PNG
     * @throws IOException Si ocurre un error al escribir la imagen
     * @throws WriterException Si ocurre un error al generar el código QR
     */
    @Override
    public byte[] generarCodigoQR(String contenido) throws IOException, WriterException {
        return generarCodigoQR(contenido, TAMAÑO_DEFECTO_ANCHO, TAMAÑO_DEFECTO_ALTO);
    }
}

