package com.cudeca.controller;

import com.cudeca.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test unitario del controlador EmailTestController.
 * Verifica el endpoint público de envío de correo.
 */
class EmailControllerTest {

    private EmailServiceImpl emailServiceImpl;
    private EmailController controller;

    @BeforeEach
    void setUp() {
        emailServiceImpl = mock(EmailServiceImpl.class);
        controller = new EmailController(emailServiceImpl);
    }

    @Test
    void sendTestEmail_ShouldReturnOkResponseAndInvokeService() {
        // Arrange
        String destinatario = "user@prueba.com";

        // Act
        Map<String, String> response = controller.sendTestEmail(destinatario);

        // Assert
        verify(emailServiceImpl, times(1)).sendTestEmail(destinatario);
        assertThat(response)
                .containsEntry("status", "ok")
                .containsEntry("message", "Correo de prueba enviado a " + destinatario);
    }

    @Test
    void sendTestEmail_ShouldUseDefaultValue_WhenNoParamProvided() {
        // Act
        Map<String, String> response = controller.sendTestEmail("test@example.com");

        // Assert
        verify(emailServiceImpl, times(1)).sendTestEmail("test@example.com");
        assertThat(response.get("status")).isEqualTo("ok");
    }
}
