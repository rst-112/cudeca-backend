package com.cudeca.service.impl;

import com.cudeca.service.IEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements IEmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendTestEmail(String to) {
        log.info("Enviando email de prueba a {}", to);
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("gruposapitos@gmail.com");
        message.setTo(to);
        message.setSubject("Correo de prueba CUDECA");
        message.setText("Este es un correo de prueba enviado desde el backend CUDECA.");
        mailSender.send(message);
        log.info("Email enviado correctamente a {}", to);
    }
}
