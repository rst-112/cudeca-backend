package com.cudeca.controller;

import com.cudeca.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test unitario del controlador EmailTestController.
 * Verifica el endpoint público de envío de correo.
 */
class EmailControllerTest {

    private EmailService emailService;
    private EmailController controller;

    @BeforeEach
    void setUp() {
        emailService = mock(EmailService.class);
        controller = new EmailController(emailService);
    }

    @Test
    void sendTestEmail_ShouldReturnOkResponseAndInvokeService() throws Exception {
        // Arrange
        String destinatario = "user@prueba.com";

        // Act
        ResponseEntity<Map<String, String>> response = controller.sendTestEmail(destinatario);

        // Assert
        verify(emailService, times(1)).sendTestEmail(destinatario);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody())
                .containsEntry("status", "ok")
                .containsEntry("message", "Correo de prueba enviado a " + destinatario);
    }

    @Test
    void sendTestEmail_ShouldUseDefaultValue_WhenNoParamProvided() throws Exception {
        // Act
        ResponseEntity<Map<String, String>> response = controller.sendTestEmail("test@example.com");

        // Assert
        verify(emailService, times(1)).sendTestEmail("test@example.com");
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().get("status")).isEqualTo("ok");
    }
}
