package com.cudeca.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test unitario del servicio EmailService.
 * Verifica que los correos se construyen y envían correctamente.
 */
class EmailServiceImplTest {

    private JavaMailSender mailSender;
    private EmailServiceImpl emailServiceImpl;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        emailServiceImpl = new EmailServiceImpl(mailSender);
    }

    @Test
    void sendTestEmail_ShouldBuildAndSendCorrectMessage() {
        // Arrange
        String destinatario = "test@cudeca.org";

        // Act
        emailServiceImpl.sendTestEmail(destinatario);

        // Assert
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertThat(sentMessage.getTo()).containsExactly(destinatario);
        assertThat(sentMessage.getSubject()).isEqualTo("Correo de prueba CUDECA");
        assertThat(sentMessage.getText()).contains("correo de prueba");
    }

    @Test
    void sendTestEmail_ShouldNotThrow_WhenMailSenderSucceeds() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        emailServiceImpl.sendTestEmail("user@example.com");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
