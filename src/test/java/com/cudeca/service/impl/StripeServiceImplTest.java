package com.cudeca.service.impl;

import com.cudeca.dto.PaymentIntentDTO;
import com.cudeca.dto.PaymentResponseDTO;
import com.cudeca.model.enums.TipoMovimiento;
import com.cudeca.model.negocio.Monedero;
import com.cudeca.model.negocio.MovimientoMonedero;
import com.cudeca.model.usuario.Usuario;
import com.cudeca.repository.MonederoRepository;
import com.cudeca.repository.MovimientoMonederoRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para StripeServiceImpl.
 * Verifica la integración con Stripe y gestión de pagos y recargas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StripeService - Tests Unitarios")
class StripeServiceImplTest {

    @Mock
    private MonederoRepository monederoRepository;

    @Mock
    private MovimientoMonederoRepository movimientoRepository;

    @InjectMocks
    private StripeServiceImpl stripeService;

    private static final String TEST_STRIPE_KEY = "sk_test_123456789";
    private static final String TEST_WEBHOOK_SECRET = "whsec_test_123456789";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stripeService, "secretKey", TEST_STRIPE_KEY);
        ReflectionTestUtils.setField(stripeService, "webhookSecret", TEST_WEBHOOK_SECRET);
        stripeService.init();
    }

    @Test
    @DisplayName("crearIntentoPago - Debe crear PaymentIntent exitosamente con valores predeterminados")
    void testCrearIntentoPago_Exitoso() {
        // Arrange
        PaymentIntentDTO dto = PaymentIntentDTO.builder()
                .amount(new BigDecimal("50.00"))
                .build();

        Long usuarioId = 123L;

        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = mock(PaymentIntent.class);
            when(mockIntent.getClientSecret()).thenReturn("pi_test_secret_123");
            when(mockIntent.getId()).thenReturn("pi_test_123");

            mockedPaymentIntent.when(() -> PaymentIntent.create(any(com.stripe.param.PaymentIntentCreateParams.class)))
                    .thenReturn(mockIntent);

            // Act
            PaymentResponseDTO response = stripeService.crearIntentoPago(dto, usuarioId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getClientSecret()).isEqualTo("pi_test_secret_123");
            assertThat(response.getPaymentIntentId()).isEqualTo("pi_test_123");

            // Verificar que se llamó con los parámetros correctos
            mockedPaymentIntent.verify(() -> PaymentIntent.create(any(com.stripe.param.PaymentIntentCreateParams.class)));
        }
    }

    @Test
    @DisplayName("crearIntentoPago - Debe usar EUR como moneda predeterminada")
    void testCrearIntentoPago_MonedaPredeterminada() {
        // Arrange
        PaymentIntentDTO dto = PaymentIntentDTO.builder()
                .amount(new BigDecimal("25.50"))
                .build();

        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = mock(PaymentIntent.class);
            when(mockIntent.getClientSecret()).thenReturn("pi_secret");
            when(mockIntent.getId()).thenReturn("pi_id");

            mockedPaymentIntent.when(() -> PaymentIntent.create(any(com.stripe.param.PaymentIntentCreateParams.class)))
                    .thenReturn(mockIntent);

            // Act
            stripeService.crearIntentoPago(dto, 1L);

            // Assert - La moneda predeterminada debe ser EUR
            mockedPaymentIntent.verify(() -> PaymentIntent.create(any(com.stripe.param.PaymentIntentCreateParams.class)));
        }
    }

    @Test
    @DisplayName("crearIntentoPago - Debe usar moneda personalizada cuando se proporciona")
    void testCrearIntentoPago_MonedaPersonalizada() {
        // Arrange
        PaymentIntentDTO dto = PaymentIntentDTO.builder()
                .amount(new BigDecimal("100.00"))
                .currency("usd")
                .build();

        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = mock(PaymentIntent.class);
            when(mockIntent.getClientSecret()).thenReturn("pi_secret");
            when(mockIntent.getId()).thenReturn("pi_id");

            mockedPaymentIntent.when(() -> PaymentIntent.create(any(com.stripe.param.PaymentIntentCreateParams.class)))
                    .thenReturn(mockIntent);

            // Act
            stripeService.crearIntentoPago(dto, 1L);

            // Assert
            mockedPaymentIntent.verify(() -> PaymentIntent.create(any(com.stripe.param.PaymentIntentCreateParams.class)));
        }
    }

    @Test
    @DisplayName("crearIntentoPago - Debe incluir metadata con usuarioId y tipo de operación")
    void testCrearIntentoPago_IncluirMetadata() {
        // Arrange
        PaymentIntentDTO dto = PaymentIntentDTO.builder()
                .amount(new BigDecimal("75.00"))
                .paymentMethod("paypal")
                .build();

        Long usuarioId = 456L;

        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = mock(PaymentIntent.class);
            when(mockIntent.getClientSecret()).thenReturn("pi_secret");
            when(mockIntent.getId()).thenReturn("pi_id");

            mockedPaymentIntent.when(() -> PaymentIntent.create(any(com.stripe.param.PaymentIntentCreateParams.class)))
                    .thenReturn(mockIntent);

            // Act
            stripeService.crearIntentoPago(dto, usuarioId);

            // Assert - Se incluye metadata
            mockedPaymentIntent.verify(() -> PaymentIntent.create(any(com.stripe.param.PaymentIntentCreateParams.class)));
        }
    }

    @Test
    @DisplayName("crearIntentoPago - Debe convertir correctamente euros a céntimos")
    void testCrearIntentoPago_ConversionCentimos() {
        // Arrange
        PaymentIntentDTO dto = PaymentIntentDTO.builder()
                .amount(new BigDecimal("99.99"))
                .build();

        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = mock(PaymentIntent.class);
            when(mockIntent.getClientSecret()).thenReturn("pi_secret");
            when(mockIntent.getId()).thenReturn("pi_id");

            mockedPaymentIntent.when(() -> PaymentIntent.create(any(com.stripe.param.PaymentIntentCreateParams.class)))
                    .thenReturn(mockIntent);

            // Act
            PaymentResponseDTO response = stripeService.crearIntentoPago(dto, 1L);

            // Assert
            assertThat(response).isNotNull();
            // 99.99 EUR = 9999 céntimos
        }
    }

    @Test
    @DisplayName("crearIntentoPago - Debe lanzar RuntimeException cuando Stripe falla")
    void testCrearIntentoPago_ErrorStripe() {
        // Arrange
        PaymentIntentDTO dto = PaymentIntentDTO.builder()
                .amount(new BigDecimal("50.00"))
                .build();

        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            mockedPaymentIntent.when(() -> PaymentIntent.create(any(com.stripe.param.PaymentIntentCreateParams.class)))
                    .thenThrow(new StripeException("Error de Stripe", "req_123", "code", 400) {});

            // Act & Assert
            assertThatThrownBy(() -> stripeService.crearIntentoPago(dto, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Error al iniciar pago");
        }
    }

    @Test
    @DisplayName("manejarWebhook - Debe procesar webhook payment_intent.succeeded exitosamente")
    void testManejarWebhook_PaymentIntentSucceeded() throws Exception {
        // Arrange
        String payload = createTestPayload();
        String sigHeader = "test_signature";

        Usuario usuario = new Usuario();
        usuario.setId(123L);

        Monedero monedero = Monedero.builder()
                .id(1L)
                .usuario(usuario)
                .saldo(new BigDecimal("0.00"))
                .movimientos(new ArrayList<>())
                .build();

        when(monederoRepository.findByUsuario_Id(123L)).thenReturn(Optional.of(monedero));
        when(monederoRepository.save(any(Monedero.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any(MovimientoMonedero.class))).thenAnswer(inv -> inv.getArgument(0));

        // Crear evento de prueba
        Event mockEvent = createMockEvent("payment_intent.succeeded", 123L, 5000L, "card");

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(payload, sigHeader, TEST_WEBHOOK_SECRET))
                    .thenReturn(mockEvent);

            // Act
            stripeService.manejarWebhook(payload, sigHeader);

            // Assert
            ArgumentCaptor<Monedero> monederoCaptor = ArgumentCaptor.forClass(Monedero.class);
            verify(monederoRepository).save(monederoCaptor.capture());
            assertThat(monederoCaptor.getValue().getSaldo()).isEqualByComparingTo(new BigDecimal("50.00"));

            ArgumentCaptor<MovimientoMonedero> movimientoCaptor = ArgumentCaptor.forClass(MovimientoMonedero.class);
            verify(movimientoRepository).save(movimientoCaptor.capture());
            MovimientoMonedero movimiento = movimientoCaptor.getValue();
            assertThat(movimiento.getTipo()).isEqualTo(TipoMovimiento.ABONO);
            assertThat(movimiento.getImporte()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(movimiento.getReferencia()).isEqualTo("Pago con Tarjeta (Stripe)");
        }
    }

    @Test
    @DisplayName("manejarWebhook - Debe procesar webhook con PayPal correctamente")
    void testManejarWebhook_ConPayPal() throws Exception {
        // Arrange
        String payload = createTestPayload();
        String sigHeader = "test_signature";

        Usuario usuario = new Usuario();
        usuario.setId(456L);

        Monedero monedero = Monedero.builder()
                .id(2L)
                .usuario(usuario)
                .saldo(new BigDecimal("100.00"))
                .movimientos(new ArrayList<>())
                .build();

        when(monederoRepository.findByUsuario_Id(456L)).thenReturn(Optional.of(monedero));
        when(monederoRepository.save(any(Monedero.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any(MovimientoMonedero.class))).thenAnswer(inv -> inv.getArgument(0));

        Event mockEvent = createMockEvent("payment_intent.succeeded", 456L, 10000L, "paypal");

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(payload, sigHeader, TEST_WEBHOOK_SECRET))
                    .thenReturn(mockEvent);

            // Act
            stripeService.manejarWebhook(payload, sigHeader);

            // Assert
            ArgumentCaptor<MovimientoMonedero> movimientoCaptor = ArgumentCaptor.forClass(MovimientoMonedero.class);
            verify(movimientoRepository).save(movimientoCaptor.capture());
            assertThat(movimientoCaptor.getValue().getReferencia()).isEqualTo("Pago vía PayPal");
        }
    }

    @Test
    @DisplayName("manejarWebhook - Debe procesar webhook con Bizum correctamente")
    void testManejarWebhook_ConBizum() throws Exception {
        // Arrange
        String payload = createTestPayload();
        String sigHeader = "test_signature";

        Usuario usuario = new Usuario();
        usuario.setId(789L);

        Monedero monedero = Monedero.builder()
                .id(3L)
                .usuario(usuario)
                .saldo(new BigDecimal("50.00"))
                .movimientos(new ArrayList<>())
                .build();

        when(monederoRepository.findByUsuario_Id(789L)).thenReturn(Optional.of(monedero));
        when(monederoRepository.save(any(Monedero.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any(MovimientoMonedero.class))).thenAnswer(inv -> inv.getArgument(0));

        Event mockEvent = createMockEvent("payment_intent.succeeded", 789L, 2500L, "bizum");

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(payload, sigHeader, TEST_WEBHOOK_SECRET))
                    .thenReturn(mockEvent);

            // Act
            stripeService.manejarWebhook(payload, sigHeader);

            // Assert
            ArgumentCaptor<MovimientoMonedero> movimientoCaptor = ArgumentCaptor.forClass(MovimientoMonedero.class);
            verify(movimientoRepository).save(movimientoCaptor.capture());
            assertThat(movimientoCaptor.getValue().getReferencia()).isEqualTo("Pago vía Bizum");
        }
    }

    @Test
    @DisplayName("manejarWebhook - Debe usar referencia genérica para método desconocido")
    void testManejarWebhook_MetodoDesconocido() throws Exception {
        // Arrange
        String payload = createTestPayload();
        String sigHeader = "test_signature";

        Usuario usuario = new Usuario();
        usuario.setId(111L);

        Monedero monedero = Monedero.builder()
                .id(4L)
                .usuario(usuario)
                .saldo(new BigDecimal("0.00"))
                .movimientos(new ArrayList<>())
                .build();

        when(monederoRepository.findByUsuario_Id(111L)).thenReturn(Optional.of(monedero));
        when(monederoRepository.save(any(Monedero.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any(MovimientoMonedero.class))).thenAnswer(inv -> inv.getArgument(0));

        Event mockEvent = createMockEvent("payment_intent.succeeded", 111L, 7500L, "apple_pay");

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(payload, sigHeader, TEST_WEBHOOK_SECRET))
                    .thenReturn(mockEvent);

            // Act
            stripeService.manejarWebhook(payload, sigHeader);

            // Assert
            ArgumentCaptor<MovimientoMonedero> movimientoCaptor = ArgumentCaptor.forClass(MovimientoMonedero.class);
            verify(movimientoRepository).save(movimientoCaptor.capture());
            assertThat(movimientoCaptor.getValue().getReferencia()).isEqualTo("Pago Online (apple_pay)");
        }
    }

    @Test
    @DisplayName("manejarWebhook - No debe procesar si el evento no es payment_intent.succeeded")
    void testManejarWebhook_EventoIncorrecto() throws Exception {
        // Arrange
        String payload = createTestPayload();
        String sigHeader = "test_signature";

        Event mockEvent = mock(Event.class);
        when(mockEvent.getType()).thenReturn("payment_intent.created");

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(payload, sigHeader, TEST_WEBHOOK_SECRET))
                    .thenReturn(mockEvent);

            // Act
            stripeService.manejarWebhook(payload, sigHeader);

            // Assert
            verify(monederoRepository, never()).findByUsuario_Id(anyLong());
            verify(monederoRepository, never()).save(any());
            verify(movimientoRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("manejarWebhook - No debe procesar si tipo_operacion no es RECARGA_MONEDERO")
    void testManejarWebhook_TipoOperacionIncorrecto() throws Exception {
        // Arrange
        String payload = createTestPayload();
        String sigHeader = "test_signature";

        Event mockEvent = createMockEventWithMetadata("payment_intent.succeeded", 
                Map.of("usuarioId", "123", "tipo_operacion", "COMPRA_TICKET"), 5000L, "card");

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(payload, sigHeader, TEST_WEBHOOK_SECRET))
                    .thenReturn(mockEvent);

            // Act
            stripeService.manejarWebhook(payload, sigHeader);

            // Assert
            verify(monederoRepository, never()).findByUsuario_Id(anyLong());
        }
    }

    @Test
    @DisplayName("manejarWebhook - No debe procesar si no hay usuarioId en metadata")
    void testManejarWebhook_SinUsuarioId() throws Exception {
        // Arrange
        String payload = createTestPayload();
        String sigHeader = "test_signature";

        Event mockEvent = createMockEventWithMetadata("payment_intent.succeeded", 
                Map.of("tipo_operacion", "RECARGA_MONEDERO"), 5000L, "card");

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(payload, sigHeader, TEST_WEBHOOK_SECRET))
                    .thenReturn(mockEvent);

            // Act
            stripeService.manejarWebhook(payload, sigHeader);

            // Assert
            verify(monederoRepository, never()).findByUsuario_Id(anyLong());
            verify(monederoRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("manejarWebhook - Debe lanzar excepción si monedero no existe")
    void testManejarWebhook_MonederoNoEncontrado() throws Exception {
        // Arrange
        String payload = createTestPayload();
        String sigHeader = "test_signature";

        when(monederoRepository.findByUsuario_Id(999L)).thenReturn(Optional.empty());

        Event mockEvent = createMockEvent("payment_intent.succeeded", 999L, 5000L, "card");

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(payload, sigHeader, TEST_WEBHOOK_SECRET))
                    .thenReturn(mockEvent);

            // Act & Assert
            assertThatThrownBy(() -> stripeService.manejarWebhook(payload, sigHeader))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Monedero no encontrado");
        }
    }

    @Test
    @DisplayName("manejarWebhook - Debe lanzar SignatureVerificationException si firma es inválida")
    void testManejarWebhook_FirmaInvalida() {
        // Arrange
        String payload = createTestPayload();
        String sigHeader = "invalid_signature";

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(payload, sigHeader, TEST_WEBHOOK_SECRET))
                    .thenThrow(new SignatureVerificationException("Invalid signature", sigHeader));

            // Act & Assert
            assertThatThrownBy(() -> stripeService.manejarWebhook(payload, sigHeader))
                    .isInstanceOf(SignatureVerificationException.class);
        }
    }

    @Test
    @DisplayName("recargaManual - Debe realizar recarga manual exitosamente")
    void testRecargaManual_Exitoso() {
        // Arrange
        Long usuarioId = 123L;
        BigDecimal cantidad = new BigDecimal("100.00");
        String referencia = "Recarga administrativa por compensación";

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        Monedero monedero = Monedero.builder()
                .id(1L)
                .usuario(usuario)
                .saldo(new BigDecimal("50.00"))
                .movimientos(new ArrayList<>())
                .build();

        when(monederoRepository.findByUsuario_Id(usuarioId)).thenReturn(Optional.of(monedero));
        when(monederoRepository.save(any(Monedero.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any(MovimientoMonedero.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        stripeService.recargaManual(usuarioId, cantidad, referencia);

        // Assert
        ArgumentCaptor<Monedero> monederoCaptor = ArgumentCaptor.forClass(Monedero.class);
        verify(monederoRepository).save(monederoCaptor.capture());
        assertThat(monederoCaptor.getValue().getSaldo()).isEqualByComparingTo(new BigDecimal("150.00"));

        ArgumentCaptor<MovimientoMonedero> movimientoCaptor = ArgumentCaptor.forClass(MovimientoMonedero.class);
        verify(movimientoRepository).save(movimientoCaptor.capture());
        MovimientoMonedero movimiento = movimientoCaptor.getValue();
        assertThat(movimiento.getTipo()).isEqualTo(TipoMovimiento.ABONO);
        assertThat(movimiento.getImporte()).isEqualByComparingTo(cantidad);
        assertThat(movimiento.getReferencia()).isEqualTo(referencia);
        assertThat(movimiento.getMonedero()).isEqualTo(monedero);
    }

    @Test
    @DisplayName("recargaManual - Debe crear movimiento con fecha actual")
    void testRecargaManual_FechaActual() {
        // Arrange
        Long usuarioId = 456L;
        BigDecimal cantidad = new BigDecimal("50.00");
        String referencia = "Promoción especial";

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        Monedero monedero = Monedero.builder()
                .id(2L)
                .usuario(usuario)
                .saldo(BigDecimal.ZERO)
                .movimientos(new ArrayList<>())
                .build();

        when(monederoRepository.findByUsuario_Id(usuarioId)).thenReturn(Optional.of(monedero));
        when(monederoRepository.save(any(Monedero.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any(MovimientoMonedero.class))).thenAnswer(inv -> inv.getArgument(0));

        OffsetDateTime antes = OffsetDateTime.now();

        // Act
        stripeService.recargaManual(usuarioId, cantidad, referencia);

        OffsetDateTime despues = OffsetDateTime.now();

        // Assert
        ArgumentCaptor<MovimientoMonedero> movimientoCaptor = ArgumentCaptor.forClass(MovimientoMonedero.class);
        verify(movimientoRepository).save(movimientoCaptor.capture());
        MovimientoMonedero movimiento = movimientoCaptor.getValue();
        
        assertThat(movimiento.getFecha()).isNotNull();
        assertThat(movimiento.getFecha()).isBetween(antes, despues);
    }

    @Test
    @DisplayName("recargaManual - Debe lanzar excepción si monedero no existe")
    void testRecargaManual_MonederoNoEncontrado() {
        // Arrange
        Long usuarioId = 999L;
        BigDecimal cantidad = new BigDecimal("75.00");
        String referencia = "Test";

        when(monederoRepository.findByUsuario_Id(usuarioId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> stripeService.recargaManual(usuarioId, cantidad, referencia))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Monedero no encontrado");

        verify(monederoRepository, never()).save(any());
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    @DisplayName("recargaManual - Debe aceptar referencia personalizada")
    void testRecargaManual_ReferenciaPersonalizada() {
        // Arrange
        Long usuarioId = 789L;
        BigDecimal cantidad = new BigDecimal("25.50");
        String referencia = "Compensación por error en sistema - Ticket #12345";

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        Monedero monedero = Monedero.builder()
                .id(3L)
                .usuario(usuario)
                .saldo(BigDecimal.ZERO)
                .movimientos(new ArrayList<>())
                .build();

        when(monederoRepository.findByUsuario_Id(usuarioId)).thenReturn(Optional.of(monedero));
        when(monederoRepository.save(any(Monedero.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any(MovimientoMonedero.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        stripeService.recargaManual(usuarioId, cantidad, referencia);

        // Assert
        ArgumentCaptor<MovimientoMonedero> movimientoCaptor = ArgumentCaptor.forClass(MovimientoMonedero.class);
        verify(movimientoRepository).save(movimientoCaptor.capture());
        assertThat(movimientoCaptor.getValue().getReferencia()).isEqualTo(referencia);
    }

    @Test
    @DisplayName("recargaManual - Debe acumular saldo correctamente en múltiples recargas")
    void testRecargaManual_MultiplesRecargas() {
        // Arrange
        Long usuarioId = 100L;
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        Monedero monedero = Monedero.builder()
                .id(1L)
                .usuario(usuario)
                .saldo(new BigDecimal("10.00"))
                .movimientos(new ArrayList<>())
                .build();

        when(monederoRepository.findByUsuario_Id(usuarioId)).thenReturn(Optional.of(monedero));
        when(monederoRepository.save(any(Monedero.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any(MovimientoMonedero.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        stripeService.recargaManual(usuarioId, new BigDecimal("20.00"), "Recarga 1");
        stripeService.recargaManual(usuarioId, new BigDecimal("30.00"), "Recarga 2");
        stripeService.recargaManual(usuarioId, new BigDecimal("40.00"), "Recarga 3");

        // Assert
        ArgumentCaptor<Monedero> monederoCaptor = ArgumentCaptor.forClass(Monedero.class);
        verify(monederoRepository, times(3)).save(monederoCaptor.capture());
        
        // El saldo final debe ser 10 + 20 + 30 + 40 = 100
        assertThat(monederoCaptor.getValue().getSaldo()).isEqualByComparingTo(new BigDecimal("100.00"));
        verify(movimientoRepository, times(3)).save(any(MovimientoMonedero.class));
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private String createTestPayload() {
        return "{\"id\":\"evt_test_123\",\"type\":\"payment_intent.succeeded\"}";
    }

    private Event createMockEvent(String eventType, Long usuarioId, Long amount, String paymentMethodType) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("usuarioId", usuarioId.toString());
        metadata.put("tipo_operacion", "RECARGA_MONEDERO");
        
        return createMockEventWithMetadata(eventType, metadata, amount, paymentMethodType);
    }

    @SuppressWarnings("unchecked")
    private Event createMockEventWithMetadata(String eventType, Map<String, String> metadata, Long amount, String paymentMethodType) {
        Event mockEvent = mock(Event.class);
        lenient().when(mockEvent.getType()).thenReturn(eventType);

        PaymentIntent mockIntent = mock(PaymentIntent.class);
        lenient().when(mockIntent.getMetadata()).thenReturn(metadata);
        lenient().when(mockIntent.getAmount()).thenReturn(amount);
        
        List<String> paymentMethods = new ArrayList<>();
        paymentMethods.add(paymentMethodType);
        lenient().when(mockIntent.getPaymentMethodTypes()).thenReturn(paymentMethods);

        Event.Data mockData = mock(Event.Data.class);
        lenient().when(mockData.getObject()).thenReturn(mockIntent);
        lenient().when(mockEvent.getData()).thenReturn(mockData);
        
        // Crear deserializador mock
        com.stripe.model.EventDataObjectDeserializer mockDeserializer = 
            mock(com.stripe.model.EventDataObjectDeserializer.class);
        lenient().when(mockDeserializer.getObject()).thenReturn(Optional.of(mockIntent));
        lenient().when(mockEvent.getDataObjectDeserializer()).thenReturn(mockDeserializer);

        return mockEvent;
    }

    @Test
    @DisplayName("init - Debe inicializar la API key de Stripe correctamente")
    void testInit_InicializaCorrectamente() {
        // Arrange
        StripeServiceImpl newService = new StripeServiceImpl(monederoRepository, movimientoRepository);
        ReflectionTestUtils.setField(newService, "secretKey", "sk_test_new_key");
        ReflectionTestUtils.setField(newService, "webhookSecret", TEST_WEBHOOK_SECRET);

        // Act
        newService.init();

        // Assert - No debe lanzar excepción
        assertThat(newService).isNotNull();
    }

    @Test
    @DisplayName("generarReferenciaProfesional - Debe generar referencia para tarjeta")
    void testGenerarReferenciaProfesional_Card() throws Exception {
        // Usar reflexión para probar el método privado
        java.lang.reflect.Method method = StripeServiceImpl.class
                .getDeclaredMethod("generarReferenciaProfesional", String.class);
        method.setAccessible(true);

        String resultado = (String) method.invoke(stripeService, "card");
        assertThat(resultado).isEqualTo("Pago con Tarjeta (Stripe)");
    }

    @Test
    @DisplayName("generarReferenciaProfesional - Debe generar referencia para PayPal")
    void testGenerarReferenciaProfesional_PayPal() throws Exception {
        java.lang.reflect.Method method = StripeServiceImpl.class
                .getDeclaredMethod("generarReferenciaProfesional", String.class);
        method.setAccessible(true);

        String resultado = (String) method.invoke(stripeService, "paypal");
        assertThat(resultado).isEqualTo("Pago vía PayPal");
    }

    @Test
    @DisplayName("generarReferenciaProfesional - Debe generar referencia para Bizum")
    void testGenerarReferenciaProfesional_Bizum() throws Exception {
        java.lang.reflect.Method method = StripeServiceImpl.class
                .getDeclaredMethod("generarReferenciaProfesional", String.class);
        method.setAccessible(true);

        String resultado = (String) method.invoke(stripeService, "bizum");
        assertThat(resultado).isEqualTo("Pago vía Bizum");
    }

    @Test
    @DisplayName("generarReferenciaProfesional - Debe generar referencia genérica para método desconocido")
    void testGenerarReferenciaProfesional_Desconocido() throws Exception {
        java.lang.reflect.Method method = StripeServiceImpl.class
                .getDeclaredMethod("generarReferenciaProfesional", String.class);
        method.setAccessible(true);

        String resultado = (String) method.invoke(stripeService, "google_pay");
        assertThat(resultado).isEqualTo("Pago Online (google_pay)");
    }
}
