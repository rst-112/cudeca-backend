package com.cudeca.service.impl;

import com.google.zxing.WriterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para QrCodeServiceImpl.
 * Cobertura esperada: >95%
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para QrCodeServiceImpl")
class QrCodeServiceImplTest {

    @InjectMocks
    private QrCodeServiceImpl qrCodeService;

    private String contenidoQR;

    @BeforeEach
    void setUp() {
        contenidoQR = "QR-TEST-123456";
    }

    @Test
    @DisplayName("Debe generar código QR con tamaño personalizado exitosamente")
    void debeGenerarCodigoQRConTamañoPersonalizado() throws IOException, WriterException {
        // Arrange
        int ancho = 300;
        int alto = 300;

        // Act
        byte[] resultado = qrCodeService.generarCodigoQR(contenidoQR, ancho, alto);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.length > 0, "El array de bytes del QR debe tener contenido");
    }

    @Test
    @DisplayName("Debe generar código QR con tamaño por defecto (250x250)")
    void debeGenerarCodigoQRConTamañoDefecto() throws IOException, WriterException {
        // Act
        byte[] resultado = qrCodeService.generarCodigoQR(contenidoQR);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.length > 0, "El array de bytes del QR debe tener contenido");
    }

    @Test
    @DisplayName("Debe lanzar excepción con contenido vacío")
    void debeLanzarExcepcionConContenidoVacio() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            qrCodeService.generarCodigoQR("");
        });
    }

    @Test
    @DisplayName("Debe generar QR con contenido largo")
    void debeGenerarQRConContenidoLargo() throws IOException, WriterException {
        // Arrange
        String contenidoLargo = "A".repeat(500);

        // Act
        byte[] resultado = qrCodeService.generarCodigoQR(contenidoLargo);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Debe generar QR con tamaño mínimo")
    void debeGenerarQRConTamañoMinimo() throws IOException, WriterException {
        // Arrange
        int tamañoMinimo = 50;

        // Act
        byte[] resultado = qrCodeService.generarCodigoQR(contenidoQR, tamañoMinimo, tamañoMinimo);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Debe generar QR con tamaño grande")
    void debeGenerarQRConTamañoGrande() throws IOException, WriterException {
        // Arrange
        int tamañoGrande = 500;

        // Act
        byte[] resultado = qrCodeService.generarCodigoQR(contenidoQR, tamañoGrande, tamañoGrande);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Debe generar QR con caracteres especiales")
    void debeGenerarQRConCaracteresEspeciales() throws IOException, WriterException {
        // Arrange
        String contenidoEspecial = "QR-2024@CUDECA#EVENT$123%";

        // Act
        byte[] resultado = qrCodeService.generarCodigoQR(contenidoEspecial);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Debe generar QR con URL")
    void debeGenerarQRConURL() throws IOException, WriterException {
        // Arrange
        String urlContenido = "https://cudeca.org/validar/QR-123456";

        // Act
        byte[] resultado = qrCodeService.generarCodigoQR(urlContenido);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Debe lanzar excepción con contenido null")
    void debeLanzarExcepcionConContenidoNull() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            qrCodeService.generarCodigoQR(null);
        });
    }

    @Test
    @DisplayName("Debe generar múltiples QR diferentes")
    void debeGenerarMultiplesQRDiferentes() throws IOException, WriterException {
        // Arrange
        String contenido1 = "QR-001";
        String contenido2 = "QR-002";

        // Act
        byte[] qr1 = qrCodeService.generarCodigoQR(contenido1);
        byte[] qr2 = qrCodeService.generarCodigoQR(contenido2);

        // Assert
        assertNotNull(qr1);
        assertNotNull(qr2);
        assertTrue(qr1.length > 0);
        assertTrue(qr2.length > 0);
        assertNotEquals(qr1.length, qr2.length, "QR con diferente contenido deben tener diferente tamaño (usualmente)");
    }

    @Test
    @DisplayName("Debe generar QR con dimensiones rectangulares")
    void debeGenerarQRConDimensionesRectangulares() throws IOException, WriterException {
        // Arrange
        int ancho = 300;
        int alto = 200;

        // Act
        byte[] resultado = qrCodeService.generarCodigoQR(contenidoQR, ancho, alto);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Debe generar QR con contenido JSON")
    void debeGenerarQRConContenidoJSON() throws IOException, WriterException {
        // Arrange
        String jsonContenido = "{\"id\":\"123\",\"event\":\"CUDECA2024\",\"seat\":\"A1\"}";

        // Act
        byte[] resultado = qrCodeService.generarCodigoQR(jsonContenido);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }
}
