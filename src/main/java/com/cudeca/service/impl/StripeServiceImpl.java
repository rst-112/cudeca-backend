package com.cudeca.service.impl;

import com.cudeca.dto.PaymentIntentDTO;
import com.cudeca.dto.PaymentResponseDTO;
import com.cudeca.model.enums.TipoMovimiento;
import com.cudeca.model.negocio.Monedero;
import com.cudeca.model.negocio.MovimientoMonedero;
import com.cudeca.repository.MonederoRepository;
import com.cudeca.repository.MovimientoMonederoRepository;
import com.cudeca.service.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeServiceImpl implements StripeService {

    private static String stripeApiKey;
    
    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final MonederoRepository monederoRepository;
    private final MovimientoMonederoRepository movimientoRepository;

    @PostConstruct
    @SuppressFBWarnings(
        value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
        justification = "Stripe SDK requires setting static apiKey field. Synchronization prevents race conditions."
    )
    public void init() {
        synchronized (StripeServiceImpl.class) {
            if (stripeApiKey == null || !stripeApiKey.equals(secretKey)) {
                stripeApiKey = secretKey;
                Stripe.apiKey = secretKey;
            }
        }
    }

    @Override
    public PaymentResponseDTO crearIntentoPago(PaymentIntentDTO datos, Long usuarioId) {
        try {
            // Stripe trabaja en céntimos
            long amountInCents = datos.getAmount().multiply(new BigDecimal(100)).longValue();

            // 1. DEFINIMOS LA LISTA EXACTA DE MÉTODOS QUE QUEREMOS
            java.util.List<String> paymentMethodTypes = new java.util.ArrayList<>();
            paymentMethodTypes.add("card");
            paymentMethodTypes.add("paypal");

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(datos.getCurrency() != null ? datos.getCurrency() : "eur")
                    .setDescription("Recarga Monedero Cudeca")
                    .putMetadata("usuarioId", usuarioId.toString())
                    .putMetadata("tipo_operacion", "RECARGA_MONEDERO")
                    .putMetadata("paymentMethod", datos.getPaymentMethod() != null ? datos.getPaymentMethod().toUpperCase(Locale.ROOT) : "CARD")

                    // 2. CAMBIO CLAVE: Usamos addAllPaymentMethodType en vez de setAutomaticPaymentMethods
                    .addAllPaymentMethodType(paymentMethodTypes)
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            return PaymentResponseDTO.builder()
                    .clientSecret(intent.getClientSecret())
                    .paymentIntentId(intent.getId())
                    .build();
        } catch (Exception e) {
            log.error("Error Stripe: ", e);
            throw new RuntimeException("Error al iniciar pago", e);
        }
    }

    @Override
    @Transactional
    public void manejarWebhook(String payload, String sigHeader) throws SignatureVerificationException {
        Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);

            if (intent != null && "RECARGA_MONEDERO".equals(intent.getMetadata().get("tipo_operacion"))) {
                procesarRecargaExitosa(intent);
            }
        }
    }

    private void procesarRecargaExitosa(PaymentIntent intent) {
        String uidStr = intent.getMetadata().get("usuarioId");
        if (uidStr == null) return;

        Long usuarioId = Long.parseLong(uidStr);
        BigDecimal importe = BigDecimal.valueOf(intent.getAmount()).divide(new BigDecimal(100));

        Monedero monedero = monederoRepository.findByUsuario_Id(usuarioId)
                .orElseThrow(() -> new RuntimeException("Monedero no encontrado"));

        monedero.ingresar(importe);
        monederoRepository.save(monedero);

        // 1. Extraer el tipo de método de pago de forma dinámica
        String tipoPago = intent.getPaymentMethodTypes().getFirst();

        // 2. Obtener detalle profesional
        String detalleReferencia = generarReferenciaProfesional(tipoPago);

        MovimientoMonedero mov = MovimientoMonedero.builder()
                .monedero(monedero)
                .tipo(TipoMovimiento.ABONO)
                .importe(importe)
                .fecha(OffsetDateTime.now())
                .referencia(detalleReferencia)
                .build();

        movimientoRepository.save(mov);
        log.info("Saldo actualizado para usuario {}: +{}€", usuarioId, importe);
    }

    private String generarReferenciaProfesional(String tipo) {
        return switch (tipo) {
            case "card" -> "Pago con Tarjeta (Stripe)";
            case "paypal" -> "Pago vía PayPal";
            case "bizum" -> "Pago vía Bizum";
            default -> "Pago Online (" + tipo + ")";
        };
    }

    @Override
    @Transactional
    public void recargaManual(Long usuarioId, BigDecimal cantidad, String referencia) {
        Monedero monedero = monederoRepository.findByUsuario_Id(usuarioId)
                .orElseThrow(() -> new RuntimeException("Monedero no encontrado"));

        monedero.ingresar(cantidad);
        monederoRepository.save(monedero);

        MovimientoMonedero mov = MovimientoMonedero.builder()
                .monedero(monedero)
                .tipo(TipoMovimiento.ABONO)
                .importe(cantidad)
                .fecha(OffsetDateTime.now())
                .referencia(referencia)
                .build();

        movimientoRepository.save(mov);
        log.info("Recarga manual para usuario {}: +{}€ | Ref: {}", usuarioId, cantidad, referencia);
    }
}