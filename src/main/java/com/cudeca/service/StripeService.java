package com.cudeca.service;

import com.cudeca.dto.PaymentIntentDTO;
import com.cudeca.dto.PaymentResponseDTO;
import com.stripe.exception.SignatureVerificationException;

import java.math.BigDecimal;

public interface StripeService {
    PaymentResponseDTO crearIntentoPago(PaymentIntentDTO datos, Long usuarioId);

    void manejarWebhook(String payload, String sigHeader) throws SignatureVerificationException;

    void recargaManual(Long usuarioId, BigDecimal cantidad, String referencia);
}