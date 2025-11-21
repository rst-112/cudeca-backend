package com.cudeca.controller;

import com.cudeca.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @GetMapping("/api/public/send-test-email")
    public Map<String, String> sendTestEmail(@RequestParam(defaultValue = "test@example.com") String to) {
        emailService.sendTestEmail(to);
        return Map.of("status", "ok", "message", "Correo de prueba enviado a " + to);
    }
}