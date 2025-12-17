package com.cudeca.controller;

import com.cudeca.dto.PaymentIntentDTO;
import com.cudeca.service.StripeService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final StripeService stripeService;

    @Value("${stripe.webhook-secret}")
    private String stripeWebhookSecret;

    // 1. Crear Intento de Pago
    @PostMapping("/crear-intento/{usuarioId}")
    public ResponseEntity<?> crearIntento(@PathVariable Long usuarioId, @RequestBody PaymentIntentDTO dto) {
        return ResponseEntity.ok(stripeService.crearIntentoPago(dto, usuarioId));
    }

    // 2. Webhook de Stripe
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        // Verificar firma
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Firma inválida");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error general");
        }

        // Procesar solo si es PAGO EXITOSO
        if ("payment_intent.succeeded".equals(event.getType())) {
            try {
                // Deserializar PaymentIntent desde JSON crudo
                EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
                String rawJson = dataObjectDeserializer.getRawJson();
                PaymentIntent paymentIntent = PaymentIntent.GSON.fromJson(rawJson, PaymentIntent.class);

                if (paymentIntent == null || paymentIntent.getMetadata() == null) {
                    return ResponseEntity.ok("Ignorado");
                }

                // Leer metadatos
                String usuarioIdStr = paymentIntent.getMetadata().get("usuarioId");
                String paymentMethod = paymentIntent.getMetadata().getOrDefault("paymentMethod", "TARJETA");

                if (usuarioIdStr != null) {
                    Long usuarioId = Long.parseLong(usuarioIdStr);
                    BigDecimal amount = new BigDecimal(paymentIntent.getAmount()).divide(new BigDecimal(100));
                    
                    stripeService.recargaManual(usuarioId, amount, paymentMethod);
                }
            } catch (Exception e) {
                log.error("Error procesando webhook: {}", e.getMessage(), e);
            }
        }

        return ResponseEntity.ok("Recibido");
    }

    // 3. Simular Bizum
    @PostMapping("/simular-bizum/{usuarioId}")
    public ResponseEntity<?> simularPagoBizum(@PathVariable Long usuarioId, @RequestBody PaymentIntentDTO dto) {
        stripeService.recargaManual(usuarioId, dto.getAmount(), "BIZUM");

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Pago Bizum simulado correctamente",
                "cantidad", dto.getAmount()
        ));
    }
}