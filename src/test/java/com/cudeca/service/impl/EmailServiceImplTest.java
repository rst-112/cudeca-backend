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

    @Test
    void sendTestEmail_WithMultipleRecipients() {
        // Arrange
        String email1 = "user1@cudeca.org";
        String email2 = "user2@cudeca.org";

        // Act
        emailServiceImpl.sendTestEmail(email1);
        emailServiceImpl.sendTestEmail(email2);

        // Assert
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendTestEmail_MessageHasCorrectSubject() {
        // Arrange
        String destinatario = "test@cudeca.org";

        // Act
        emailServiceImpl.sendTestEmail(destinatario);

        // Assert
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getSubject()).isNotEmpty();
        assertThat(captor.getValue().getSubject()).contains("CUDECA");
    }

    @Test
    void sendTestEmail_MessageHasText() {
        // Arrange
        String destinatario = "test@cudeca.org";

        // Act
        emailServiceImpl.sendTestEmail(destinatario);

        // Assert
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).isNotNull();
        assertThat(captor.getValue().getText()).isNotEmpty();
    }

    @Test
    void sendTestEmail_ToAddressMatchesRecipient() {
        // Arrange
        String destinatario = "specific@cudeca.org";

        // Act
        emailServiceImpl.sendTestEmail(destinatario);

        // Assert
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        String[] recipients = captor.getValue().getTo();
        assertThat(recipients).hasSize(1);
        assertThat(recipients[0]).isEqualTo(destinatario);
    }
}
