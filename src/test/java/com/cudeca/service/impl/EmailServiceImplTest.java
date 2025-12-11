package com.cudeca.service.impl;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

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
        ReflectionTestUtils.setField(emailServiceImpl, "senderEmail", "noreply@cudeca.org");
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
        assertThat(sentMessage.getSubject()).isEqualTo("Correo de Prueba");
        assertThat(sentMessage.getText()).contains("Funciona correctamente");
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
        assertThat(captor.getValue().getSubject()).contains("Correo de Prueba");
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

    // ===================== Tests para enviarCorreoHtml =====================

    @Test
    void enviarCorreoHtml_ShouldSendMimeMessageWithHtmlContent() throws Exception {
        // Arrange
        String destinatario = "usuario@test.com";
        String asunto = "Bienvenido a CUDECA";
        String contenidoHtml = "<html><body><h1>Hola</h1><p>Contenido HTML</p></body></html>";

        MimeMessage mockMimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        // Act
        emailServiceImpl.enviarCorreoHtml(destinatario, asunto, contenidoHtml);

        // Assert
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        MimeMessage sentMessage = captor.getValue();
        assertThat(sentMessage.getAllRecipients()).hasSize(1);
        assertThat(sentMessage.getAllRecipients()[0].toString()).isEqualTo(destinatario);
        assertThat(sentMessage.getSubject()).isEqualTo(asunto);
        // Verificar que el mensaje tiene contenido (es MimeMultipart)
        assertThat(sentMessage.getContent()).isNotNull();
    }

    @Test
    void enviarCorreoHtml_ShouldSetFromAddress() throws Exception {
        // Arrange
        String destinatario = "usuario@test.com";
        String asunto = "Test Subject";
        String contenidoHtml = "<html><body>Test</body></html>";

        MimeMessage mockMimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        // Act
        emailServiceImpl.enviarCorreoHtml(destinatario, asunto, contenidoHtml);

        // Assert
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        MimeMessage sentMessage = captor.getValue();
        assertThat(sentMessage.getFrom()).hasSize(1);
        assertThat(sentMessage.getFrom()[0].toString()).contains("noreply@cudeca.org");
    }

    @Test
    void enviarCorreoHtml_ShouldHandleExceptionGracefully() throws Exception {
        // Arrange
        String destinatario = "usuario@test.com";
        String asunto = "Test Subject";
        String contenidoHtml = "<html><body>Test</body></html>";

        MimeMessage mockMimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        doThrow(new RuntimeException("Error SMTP")).when(mailSender).send(any(MimeMessage.class));

        // Act - No debe lanzar excepción (método @Async con try-catch interno)
        emailServiceImpl.enviarCorreoHtml(destinatario, asunto, contenidoHtml);

        // Assert - Verificar que se intentó enviar
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void enviarCorreoHtml_WithMultipleRecipients() throws Exception {
        // Arrange
        MimeMessage mockMimeMessage1 = new MimeMessage(Session.getInstance(new Properties()));
        MimeMessage mockMimeMessage2 = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage1, mockMimeMessage2);

        // Act
        emailServiceImpl.enviarCorreoHtml("user1@test.com", "Asunto 1", "<html><body>HTML 1</body></html>");
        emailServiceImpl.enviarCorreoHtml("user2@test.com", "Asunto 2", "<html><body>HTML 2</body></html>");

        // Assert
        verify(mailSender, times(2)).send(any(MimeMessage.class));
    }

    @Test
    void enviarCorreoHtml_WithEmptyHtmlContent() throws Exception {
        // Arrange
        String destinatario = "usuario@test.com";
        String asunto = "Test Subject";
        String contenidoHtml = "";

        MimeMessage mockMimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        // Act
        emailServiceImpl.enviarCorreoHtml(destinatario, asunto, contenidoHtml);

        // Assert
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // ===================== Tests para enviarCorreoConAdjunto =====================

    @Test
    void enviarCorreoConAdjunto_ShouldSendMimeMessageWithAttachment() throws Exception {
        // Arrange
        String destinatario = "usuario@test.com";
        String asunto = "Tu ticket de entrada";
        String contenidoHtml = "<html><body><h1>Ticket adjunto</h1></body></html>";
        byte[] pdfBytes = new byte[]{1, 2, 3, 4, 5};
        String nombreArchivo = "ticket.pdf";

        MimeMessage mockMimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        // Act
        emailServiceImpl.enviarCorreoConAdjunto(destinatario, asunto, contenidoHtml, pdfBytes, nombreArchivo);

        // Assert
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        MimeMessage sentMessage = captor.getValue();
        assertThat(sentMessage.getAllRecipients()).hasSize(1);
        assertThat(sentMessage.getAllRecipients()[0].toString()).isEqualTo(destinatario);
        assertThat(sentMessage.getSubject()).isEqualTo(asunto);
    }

    @Test
    void enviarCorreoConAdjunto_ShouldSetFromAddress() throws Exception {
        // Arrange
        String destinatario = "usuario@test.com";
        String asunto = "Test Subject";
        String contenidoHtml = "<html><body>Test</body></html>";
        byte[] pdfBytes = new byte[]{1, 2, 3};
        String nombreArchivo = "test.pdf";

        MimeMessage mockMimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        // Act
        emailServiceImpl.enviarCorreoConAdjunto(destinatario, asunto, contenidoHtml, pdfBytes, nombreArchivo);

        // Assert
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        MimeMessage sentMessage = captor.getValue();
        assertThat(sentMessage.getFrom()).hasSize(1);
        assertThat(sentMessage.getFrom()[0].toString()).contains("noreply@cudeca.org");
    }

    @Test
    void enviarCorreoConAdjunto_ShouldHandleExceptionGracefully() throws Exception {
        // Arrange
        String destinatario = "usuario@test.com";
        String asunto = "Test Subject";
        String contenidoHtml = "<html><body>Test</body></html>";
        byte[] pdfBytes = new byte[]{1, 2, 3};
        String nombreArchivo = "test.pdf";

        MimeMessage mockMimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        doThrow(new RuntimeException("Error SMTP")).when(mailSender).send(any(MimeMessage.class));

        // Act - No debe lanzar excepción (método @Async con try-catch interno)
        emailServiceImpl.enviarCorreoConAdjunto(destinatario, asunto, contenidoHtml, pdfBytes, nombreArchivo);

        // Assert - Verificar que se intentó enviar
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void enviarCorreoConAdjunto_WithEmptyPdfBytes() throws Exception {
        // Arrange
        String destinatario = "usuario@test.com";
        String asunto = "Test Subject";
        String contenidoHtml = "<html><body>Test</body></html>";
        byte[] pdfBytes = new byte[0]; // PDF vacío
        String nombreArchivo = "empty.pdf";

        MimeMessage mockMimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        // Act
        emailServiceImpl.enviarCorreoConAdjunto(destinatario, asunto, contenidoHtml, pdfBytes, nombreArchivo);

        // Assert
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void enviarCorreoConAdjunto_WithLargePdfAttachment() throws Exception {
        // Arrange
        String destinatario = "usuario@test.com";
        String asunto = "Test Subject";
        String contenidoHtml = "<html><body>Test</body></html>";
        byte[] pdfBytes = new byte[10000]; // PDF grande (10KB)
        String nombreArchivo = "large.pdf";

        MimeMessage mockMimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        // Act
        emailServiceImpl.enviarCorreoConAdjunto(destinatario, asunto, contenidoHtml, pdfBytes, nombreArchivo);

        // Assert
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void enviarCorreoConAdjunto_WithMultipleEmails() throws Exception {
        // Arrange
        MimeMessage mockMimeMessage1 = new MimeMessage(Session.getInstance(new Properties()));
        MimeMessage mockMimeMessage2 = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage1, mockMimeMessage2);

        byte[] pdfBytes = new byte[]{1, 2, 3};

        // Act
        emailServiceImpl.enviarCorreoConAdjunto("user1@test.com", "Asunto 1", "<html><body>HTML 1</body></html>", pdfBytes, "ticket1.pdf");
        emailServiceImpl.enviarCorreoConAdjunto("user2@test.com", "Asunto 2", "<html><body>HTML 2</body></html>", pdfBytes, "ticket2.pdf");

        // Assert
        verify(mailSender, times(2)).send(any(MimeMessage.class));
    }

    @Test
    void enviarCorreoConAdjunto_WithSpecialCharactersInFileName() throws Exception {
        // Arrange
        String destinatario = "usuario@test.com";
        String asunto = "Test Subject";
        String contenidoHtml = "<html><body>Test</body></html>";
        byte[] pdfBytes = new byte[]{1, 2, 3};
        String nombreArchivo = "ticket-ñ-ü-á.pdf";

        MimeMessage mockMimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        // Act
        emailServiceImpl.enviarCorreoConAdjunto(destinatario, asunto, contenidoHtml, pdfBytes, nombreArchivo);

        // Assert
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
