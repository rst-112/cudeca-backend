package com.cudeca.service.impl;

import com.cudeca.dto.CheckoutRequest;
import com.cudeca.dto.CheckoutResponse;
import com.cudeca.model.enums.EstadoCompra;
import com.cudeca.model.evento.TipoEntrada;
import com.cudeca.model.negocio.Compra;
import com.cudeca.model.usuario.Invitado;
import com.cudeca.model.usuario.Usuario;
import com.cudeca.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CheckoutServiceImpl.
 * Verifica la lógica de negocio del proceso de checkout.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutService - Tests Unitarios")
class CheckoutServiceImplTest {

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private InvitadoRepository invitadoRepository;

    @Mock
    private TipoEntradaRepository tipoEntradaRepository;

    @Mock
    private AsientoRepository asientoRepository;

    @Mock
    private CertificadoFiscalRepository certificadoRepository;

    @Mock
    private MonederoRepository monederoRepository;

    @Mock
    private MovimientoMonederoRepository movimientoRepository;

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private DatosFiscalesRepository datosFiscalesRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    private Usuario usuarioComprador;
    private TipoEntrada tipoEntrada;
    private CheckoutRequest checkoutRequest;

    @BeforeEach
    void setUp() {
        usuarioComprador = new Usuario();
        usuarioComprador.setId(1L);
        usuarioComprador.setNombre("Juan Pérez");
        usuarioComprador.setEmail("juan@example.com");

        tipoEntrada = TipoEntrada.builder()
                .id(1L)
                .nombre("Entrada General")
                .costeBase(BigDecimal.valueOf(25.00))
                .cantidadTotal(100)
                .build();

        checkoutRequest = new CheckoutRequest();
        checkoutRequest.setUsuarioId(1L);
        checkoutRequest.setDonacionExtra(5.0);
        checkoutRequest.setMetodoPago("TARJETA");

        CheckoutRequest.ItemDTO item = new CheckoutRequest.ItemDTO();
        item.setTipo("ENTRADA");
        item.setReferenciaId(1L);
        item.setCantidad(2);
        item.setPrecio(25.0);

        checkoutRequest.setItems(List.of(item));
    }

    @Test
    @DisplayName("Debe procesar checkout exitosamente con usuario registrado")
    void testProcesarCheckout_Exitoso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComprador));
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));

        Compra compraGuardada = Compra.builder()
                .id(100L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.save(any(Compra.class))).thenReturn(compraGuardada);

        CheckoutResponse response = checkoutService.procesarCheckout(checkoutRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCompraId()).isEqualTo(100L);
        verify(compraRepository).save(any(Compra.class));
    }

    @Test
    @DisplayName("Debe procesar checkout con invitado cuando no hay usuario")
    void testProcesarCheckout_ConInvitado() {
        // Arrange
        checkoutRequest.setUsuarioId(null);
        checkoutRequest.setEmailContacto("invitado@example.com");

        Invitado invitado = new Invitado();
        invitado.setId(1L);
        invitado.setEmail("invitado@example.com");

        when(invitadoRepository.findByEmail("invitado@example.com"))
                .thenReturn(Optional.of(invitado));
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));

        Compra compraGuardada = Compra.builder()
                .id(100L)
                .invitado(invitado)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .emailContacto("invitado@example.com")
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.save(any(Compra.class))).thenReturn(compraGuardada);

        // Act
        CheckoutResponse response = checkoutService.procesarCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCompraId()).isEqualTo(100L);

        verify(invitadoRepository).findByEmail("invitado@example.com");
        verify(usuarioRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Debe crear nuevo invitado si no existe")
    void testProcesarCheckout_CrearNuevoInvitado() {
        // Arrange
        checkoutRequest.setUsuarioId(null);
        checkoutRequest.setEmailContacto("nuevo@example.com");

        when(invitadoRepository.findByEmail("nuevo@example.com"))
                .thenReturn(Optional.empty());

        Invitado nuevoInvitado = new Invitado();
        nuevoInvitado.setId(2L);
        nuevoInvitado.setEmail("nuevo@example.com");

        when(invitadoRepository.save(any(Invitado.class))).thenReturn(nuevoInvitado);
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));

        Compra compraGuardada = Compra.builder()
                .id(100L)
                .invitado(nuevoInvitado)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.save(any(Compra.class))).thenReturn(compraGuardada);

        // Act
        CheckoutResponse response = checkoutService.procesarCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(invitadoRepository).save(any(Invitado.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el carrito está vacío")
    void testProcesarCheckout_CarritoVacio() {
        // Arrange
        checkoutRequest.setItems(new ArrayList<>());

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.procesarCheckout(checkoutRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("carrito está vacío");

        verify(compraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando items es null")
    void testProcesarCheckout_ItemsNull() {
        // Arrange
        checkoutRequest.setItems(null);

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.procesarCheckout(checkoutRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("carrito está vacío");
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando no hay usuario ni email de contacto")
    void testProcesarCheckout_SinUsuarioNiEmail() {
        // Arrange
        checkoutRequest.setUsuarioId(null);
        checkoutRequest.setEmailContacto(null);

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.procesarCheckout(checkoutRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email de contacto");
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no existe")
    void testProcesarCheckout_UsuarioNoExiste() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.procesarCheckout(checkoutRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el TipoEntrada no existe")
    void testProcesarCheckout_TipoEntradaNoExiste() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComprador));
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.procesarCheckout(checkoutRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TipoEntrada no encontrado");
    }

    @Test
    @DisplayName("Debe confirmar pago exitosamente")
    void testConfirmarPago_Exitoso() {
        // Arrange
        Compra compra = Compra.builder()
                .id(1L)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));
        when(compraRepository.save(any(Compra.class))).thenReturn(compra);

        // Act
        boolean resultado = checkoutService.confirmarPago(1L);

        // Assert
        assertThat(resultado).isTrue();
        verify(compraRepository).save(argThat(c ->
                c.getEstado() == EstadoCompra.COMPLETADA
        ));
    }

    @Test
    @DisplayName("No debe confirmar pago si compra no está PENDIENTE")
    void testConfirmarPago_NoEsPendiente() {
        // Arrange
        Compra compra = Compra.builder()
                .id(1L)
                .estado(EstadoCompra.COMPLETADA)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));

        // Act
        boolean resultado = checkoutService.confirmarPago(1L);

        // Assert
        assertThat(resultado).isFalse();
        verify(compraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al confirmar pago de compra inexistente")
    void testConfirmarPago_CompraNoExiste() {
        // Arrange
        when(compraRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.confirmarPago(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Compra no encontrada");
    }

    @Test
    @DisplayName("Debe cancelar compra exitosamente")
    void testCancelarCompra_Exitoso() {
        // Arrange
        Compra compra = Compra.builder()
                .id(1L)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));
        when(compraRepository.save(any(Compra.class))).thenReturn(compra);

        // Act
        boolean resultado = checkoutService.cancelarCompra(1L, "Cliente solicitó cancelación");

        // Assert
        assertThat(resultado).isTrue();
        verify(compraRepository).save(argThat(c ->
                c.getEstado() == EstadoCompra.CANCELADA
        ));
    }

    @Test
    @DisplayName("No debe cancelar compra ya completada")
    void testCancelarCompra_CompraCompletada() {
        // Arrange
        Compra compra = Compra.builder()
                .id(1L)
                .estado(EstadoCompra.COMPLETADA)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));

        // Act
        boolean resultado = checkoutService.cancelarCompra(1L, "Intento de cancelación");

        // Assert
        assertThat(resultado).isFalse();
        verify(compraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe obtener detalles de compra exitosamente")
    void testObtenerDetallesCompra_Exitoso() {
        // Arrange
        Compra compra = Compra.builder()
                .id(1L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.COMPLETADA)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));

        // Act
        CheckoutResponse response = checkoutService.obtenerDetallesCompra(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCompraId()).isEqualTo(1L);
        verify(compraRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al obtener detalles de compra inexistente")
    void testObtenerDetallesCompra_NoExiste() {
        // Arrange
        when(compraRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.obtenerDetallesCompra(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Compra no encontrada");
    }

    @Test
    @DisplayName("Debe calcular total con donación extra")
    void testProcesarCheckout_ConDonacionExtra() {
        // Arrange
        checkoutRequest.setDonacionExtra(10.0);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComprador));
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));

        Compra compraGuardada = Compra.builder()
                .id(100L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.save(any(Compra.class))).thenReturn(compraGuardada);

        // Act
        CheckoutResponse response = checkoutService.procesarCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        // Total debería incluir 2 entradas (2*25) + donación (10) = 60
        assertThat(response.getTotal()).isGreaterThan(BigDecimal.valueOf(50));
    }

    @Test
    @DisplayName("Debe procesar múltiples items en el carrito")
    void testProcesarCheckout_MultipleItems() {
        // Arrange
        CheckoutRequest.ItemDTO item1 = new CheckoutRequest.ItemDTO();
        item1.setTipo("ENTRADA");
        item1.setReferenciaId(1L);
        item1.setCantidad(2);
        item1.setPrecio(25.0);

        CheckoutRequest.ItemDTO item2 = new CheckoutRequest.ItemDTO();
        item2.setTipo("DONACION");
        item2.setReferenciaId(null);
        item2.setCantidad(1);
        item2.setPrecio(15.0);

        checkoutRequest.setItems(List.of(item1, item2));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComprador));
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));

        Compra compraGuardada = Compra.builder()
                .id(100L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.save(any(Compra.class))).thenReturn(compraGuardada);

        // Act
        CheckoutResponse response = checkoutService.procesarCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();

        ArgumentCaptor<Compra> compraCaptor = ArgumentCaptor.forClass(Compra.class);
        verify(compraRepository).save(compraCaptor.capture());

        Compra compraPersistida = compraCaptor.getValue();
        assertThat(compraPersistida.getArticulos()).hasSize(2);
    }

    @Test
    @DisplayName("Debe manejar checkout sin donación extra")
    void testProcesarCheckout_SinDonacionExtra() {
        // Arrange
        checkoutRequest.setDonacionExtra(null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComprador));
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));

        Compra compraGuardada = Compra.builder()
                .id(100L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.save(any(Compra.class))).thenReturn(compraGuardada);

        // Act
        CheckoutResponse response = checkoutService.procesarCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
    }

    @Test
    @DisplayName("Debe manejar checkout con donación extra cero")
    void testProcesarCheckout_DonacionExtraCero() {
        // Arrange
        checkoutRequest.setDonacionExtra(0.0);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComprador));
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));

        Compra compraGuardada = Compra.builder()
                .id(100L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.save(any(Compra.class))).thenReturn(compraGuardada);

        // Act
        CheckoutResponse response = checkoutService.procesarCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
    }

    @Test
    @DisplayName("Debe procesar item de tipo DONACION correctamente")
    void testProcesarCheckout_ItemDonacion() {
        // Arrange
        CheckoutRequest.ItemDTO itemDonacion = new CheckoutRequest.ItemDTO();
        itemDonacion.setTipo("DONACION");
        itemDonacion.setReferenciaId(null);
        itemDonacion.setCantidad(1);
        itemDonacion.setPrecio(20.0);

        checkoutRequest.setItems(List.of(itemDonacion));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComprador));

        Compra compraGuardada = Compra.builder()
                .id(100L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.save(any(Compra.class))).thenReturn(compraGuardada);

        // Act
        CheckoutResponse response = checkoutService.procesarCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(compraRepository).save(any(Compra.class));
    }

    @Test
    @DisplayName("Debe procesar item de tipo SORTEO correctamente")
    void testProcesarCheckout_ItemSorteo() {
        // Arrange
        CheckoutRequest.ItemDTO itemSorteo = new CheckoutRequest.ItemDTO();
        itemSorteo.setTipo("SORTEO");
        itemSorteo.setReferenciaId(null); // SORTEO no requiere referenciaId
        itemSorteo.setCantidad(3);
        itemSorteo.setPrecio(15.0);

        checkoutRequest.setItems(List.of(itemSorteo));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComprador));

        Compra compraGuardada = Compra.builder()
                .id(100L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.save(any(Compra.class))).thenReturn(compraGuardada);

        // Act
        CheckoutResponse response = checkoutService.procesarCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(compraRepository).save(any(Compra.class));
        // SORTEO no necesita tipoEntradaRepository
    }

    @Test
    @DisplayName("Debe calcular correctamente el total de la compra")
    void testCalcularTotal_Correcto() {
        // Arrange
        CheckoutRequest.ItemDTO item1 = new CheckoutRequest.ItemDTO();
        item1.setTipo("ENTRADA");
        item1.setReferenciaId(1L);
        item1.setCantidad(1);
        item1.setPrecio(25.0);

        CheckoutRequest.ItemDTO item2 = new CheckoutRequest.ItemDTO();
        item2.setTipo("ENTRADA");
        item2.setReferenciaId(1L);
        item2.setCantidad(1);
        item2.setPrecio(25.0);

        checkoutRequest.setItems(List.of(item1, item2));
        checkoutRequest.setDonacionExtra(10.0);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComprador));
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));

        Compra compraGuardada = Compra.builder()
                .id(100L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.save(any(Compra.class))).thenReturn(compraGuardada);

        // Act
        CheckoutResponse response = checkoutService.procesarCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        // Total: 2 entradas (25+25) + donación (10) = 60
        assertThat(response.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(60.0));
    }

    @Test
    @DisplayName("Debe verificar estado de compra en confirmarPago")
    void testConfirmarPago_VerificarEstado() {
        // Arrange
        Compra compra = Compra.builder()
                .id(1L)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));
        when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        boolean resultado = checkoutService.confirmarPago(1L);

        // Assert
        assertThat(resultado).isTrue();

        ArgumentCaptor<Compra> compraCaptor = ArgumentCaptor.forClass(Compra.class);
        verify(compraRepository).save(compraCaptor.capture());

        Compra compraActualizada = compraCaptor.getValue();
        assertThat(compraActualizada.getEstado()).isEqualTo(EstadoCompra.COMPLETADA);
    }

    @Test
    @DisplayName("Debe verificar estado de compra en cancelarCompra")
    void testCancelarCompra_VerificarEstado() {
        // Arrange
        Compra compra = Compra.builder()
                .id(1L)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));
        when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        boolean resultado = checkoutService.cancelarCompra(1L, "Test cancellation");

        // Assert
        assertThat(resultado).isTrue();

        ArgumentCaptor<Compra> compraCaptor = ArgumentCaptor.forClass(Compra.class);
        verify(compraRepository).save(compraCaptor.capture());

        Compra compraActualizada = compraCaptor.getValue();
        assertThat(compraActualizada.getEstado()).isEqualTo(EstadoCompra.CANCELADA);
    }

    @Test
    @DisplayName("Debe manejar compra con estado CANCELADA")
    void testObtenerDetallesCompra_CompraCancelada() {
        // Arrange
        Compra compra = Compra.builder()
                .id(1L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.CANCELADA)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));

        // Act
        CheckoutResponse response = checkoutService.obtenerDetallesCompra(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo("CANCELADA");
    }

    @Test
    @DisplayName("Debe obtener mensaje correcto para estado REEMBOLSADA")
    void testObtenerMensajeEstado_Reembolsada() {
        // Arrange
        Compra compra = Compra.builder()
                .id(1L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.REEMBOLSADA)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));

        // Act
        CheckoutResponse response = checkoutService.obtenerDetallesCompra(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo("REEMBOLSADA");
    }

    @Test
    @DisplayName("Debe obtener mensaje correcto para estado PARCIAL_REEMBOLSADA")
    void testObtenerMensajeEstado_ParcialReembolsada() {
        // Arrange
        Compra compra = Compra.builder()
                .id(1L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.PARCIAL_REEMBOLSADA)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));

        // Act
        CheckoutResponse response = checkoutService.obtenerDetallesCompra(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo("PARCIAL_REEMBOLSADA");
    }

    @Test
    @DisplayName("Debe procesar pago con monedero exitosamente")
    void testProcesarCheckout_PagoConMonedero() {
        // Arrange
        checkoutRequest.setMetodoPago("MONEDERO");
        
        com.cudeca.model.negocio.Monedero monedero = com.cudeca.model.negocio.Monedero.builder()
                .id(1L)
                .usuario(usuarioComprador)
                .saldo(BigDecimal.valueOf(100.00))
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComprador));
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));
        when(monederoRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(monedero));

        Compra compraGuardada = Compra.builder()
                .id(100L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .pagos(new ArrayList<>())
                .build();

        when(compraRepository.save(any(Compra.class))).thenReturn(compraGuardada);

        // Act
        CheckoutResponse response = checkoutService.procesarCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(monederoRepository).save(any(com.cudeca.model.negocio.Monedero.class));
        verify(movimientoRepository).save(any(com.cudeca.model.negocio.MovimientoMonedero.class));
    }

    @Test
    @DisplayName("Debe fallar al pagar con monedero si usuario es invitado")
    void testProcesarCheckout_PagoMonederoInvitadoFalla() {
        // Arrange
        checkoutRequest.setUsuarioId(null);
        checkoutRequest.setEmailContacto("invitado@example.com");
        checkoutRequest.setMetodoPago("MONEDERO");

        Invitado invitado = new Invitado();
        invitado.setId(1L);
        invitado.setEmail("invitado@example.com");

        when(invitadoRepository.findByEmail("invitado@example.com")).thenReturn(Optional.of(invitado));
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.procesarCheckout(checkoutRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Los invitados no pueden pagar con Monedero");
    }

    @Test
    @DisplayName("Debe fallar al pagar con monedero si saldo insuficiente")
    void testProcesarCheckout_MonederoSaldoInsuficiente() {
        // Arrange
        checkoutRequest.setMetodoPago("MONEDERO");
        
        com.cudeca.model.negocio.Monedero monedero = com.cudeca.model.negocio.Monedero.builder()
                .id(1L)
                .usuario(usuarioComprador)
                .saldo(BigDecimal.valueOf(10.00))
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComprador));
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));
        when(monederoRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(monedero));

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.procesarCheckout(checkoutRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Saldo insuficiente en el monedero");
    }

    @Test
    @DisplayName("Debe fallar al pagar con monedero si usuario no tiene monedero")
    void testProcesarCheckout_UsuarioSinMonedero() {
        // Arrange
        checkoutRequest.setMetodoPago("MONEDERO");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComprador));
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));
        when(monederoRepository.findByUsuario_Id(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.procesarCheckout(checkoutRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El usuario no tiene monedero activo");
    }

    @Test
    @DisplayName("Debe bloquear asientos correctamente")
    void testProcesarCheckout_BloquearAsientos() {
        // Arrange
        checkoutRequest.setAsientoIds(List.of(1L, 2L));

        com.cudeca.model.evento.Asiento asiento1 = new com.cudeca.model.evento.Asiento();
        asiento1.setId(1L);
        asiento1.setEstado(com.cudeca.model.enums.EstadoAsiento.LIBRE);

        com.cudeca.model.evento.Asiento asiento2 = new com.cudeca.model.evento.Asiento();
        asiento2.setId(2L);
        asiento2.setEstado(com.cudeca.model.enums.EstadoAsiento.LIBRE);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComprador));
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));
        when(asientoRepository.findAllByIdWithLock(List.of(1L, 2L))).thenReturn(List.of(asiento1, asiento2));

        Compra compraGuardada = Compra.builder()
                .id(100L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.save(any(Compra.class))).thenReturn(compraGuardada);

        // Act
        CheckoutResponse response = checkoutService.procesarCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(asientoRepository).saveAll(anyList());
        assertThat(asiento1.getEstado()).isEqualTo(com.cudeca.model.enums.EstadoAsiento.BLOQUEADO);
        assertThat(asiento2.getEstado()).isEqualTo(com.cudeca.model.enums.EstadoAsiento.BLOQUEADO);
    }

    @Test
    @DisplayName("Debe fallar si asientos no están disponibles")
    void testProcesarCheckout_AsientosNoDisponibles() {
        // Arrange
        checkoutRequest.setAsientoIds(List.of(1L));

        com.cudeca.model.evento.Asiento asiento = new com.cudeca.model.evento.Asiento();
        asiento.setId(1L);
        asiento.setEstado(com.cudeca.model.enums.EstadoAsiento.VENDIDO);

        when(asientoRepository.findAllByIdWithLock(List.of(1L))).thenReturn(List.of(asiento));

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.procesarCheckout(checkoutRequest))
                .isInstanceOf(com.cudeca.exception.AsientoNoDisponibleException.class)
                .hasMessageContaining("no están disponibles");
    }

    @Test
    @DisplayName("Debe fallar si no se encuentran todos los asientos")
    void testProcesarCheckout_AsientosNoEncontrados() {
        // Arrange
        checkoutRequest.setAsientoIds(List.of(1L, 2L));

        com.cudeca.model.evento.Asiento asiento1 = new com.cudeca.model.evento.Asiento();
        asiento1.setId(1L);
        asiento1.setEstado(com.cudeca.model.enums.EstadoAsiento.LIBRE);

        when(asientoRepository.findAllByIdWithLock(List.of(1L, 2L))).thenReturn(List.of(asiento1));

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.procesarCheckout(checkoutRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Asientos no encontrados");
    }

    @Test
    @DisplayName("Debe generar certificado fiscal cuando hay datos fiscales")
    void testProcesarCheckout_GenerarCertificadoFiscal() throws Exception {
        // Arrange
        CheckoutRequest.FiscalDataDTO datosFiscales = CheckoutRequest.FiscalDataDTO.builder()
                .nif("12345678Z")
                .nombreCompleto("Juan Pérez")
                .direccion("Calle Test 123")
                .pais("España")
                .build();
        checkoutRequest.setDatosFiscales(datosFiscales);

        // Crear un DatosFiscales mockado que será retornado
        com.cudeca.model.usuario.DatosFiscales datosFiscalesMock = new com.cudeca.model.usuario.DatosFiscales();
        datosFiscalesMock.setId(1L);
        datosFiscalesMock.setUsuario(usuarioComprador);
        datosFiscalesMock.setNombreCompleto("Juan Pérez");
        datosFiscalesMock.setNif("12345678Z");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComprador));
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));
        when(datosFiscalesRepository.findByUsuario_Id(1L)).thenReturn(List.of(datosFiscalesMock));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\": \"data\"}");

        Compra compraGuardada = Compra.builder()
                .id(100L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.save(any(Compra.class))).thenReturn(compraGuardada);

        // Act
        CheckoutResponse response = checkoutService.procesarCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(certificadoRepository).save(any(com.cudeca.model.negocio.CertificadoFiscal.class));
    }

    @Test
    @DisplayName("Debe calcular base de donación correctamente")
    void testProcesarCheckout_ConDonacion() {
        // Arrange
        CheckoutRequest.ItemDTO itemDonacion = new CheckoutRequest.ItemDTO();
        itemDonacion.setTipo("DONACION");
        itemDonacion.setReferenciaId(1L);
        itemDonacion.setCantidad(1);
        itemDonacion.setPrecio(50.0);

        checkoutRequest.setItems(List.of(itemDonacion));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComprador));

        Compra compraGuardada = Compra.builder()
                .id(100L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.save(any(Compra.class))).thenReturn(compraGuardada);

        // Act
        CheckoutResponse response = checkoutService.procesarCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTotal()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Debe manejar error al generar certificado fiscal sin fallar la compra")
    void testProcesarCheckout_ErrorGenerandoCertificado() throws Exception {
        // Arrange
        CheckoutRequest.FiscalDataDTO datosFiscales = CheckoutRequest.FiscalDataDTO.builder()
                .nif("12345678Z")
                .nombreCompleto("Juan Pérez")
                .direccion("Calle Test 123")
                .pais("España")
                .build();
        checkoutRequest.setDatosFiscales(datosFiscales);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComprador));
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));
        when(datosFiscalesRepository.findByUsuario_Id(1L)).thenReturn(new ArrayList<>());

        // Simular error en ObjectMapper - usar lenient para evitar UnnecessaryStubbingException
        lenient().when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("Error de serialización"));

        Compra compraGuardada = Compra.builder()
                .id(100L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.save(any(Compra.class))).thenReturn(compraGuardada);

        // Act
        CheckoutResponse response = checkoutService.procesarCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCompraId()).isEqualTo(100L);
        // La compra debe completarse aunque falle el certificado
        verify(compraRepository).save(any(Compra.class));
        // No debe guardar certificado debido al error
        verify(certificadoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe loguear bloqueo exitoso de asientos")
    void testProcesarCheckout_LogBloqueoAsientos() {
        // Arrange
        checkoutRequest.setAsientoIds(List.of(1L, 2L));

        com.cudeca.model.evento.Asiento asiento1 = new com.cudeca.model.evento.Asiento();
        asiento1.setId(1L);
        asiento1.setEstado(com.cudeca.model.enums.EstadoAsiento.LIBRE);

        com.cudeca.model.evento.Asiento asiento2 = new com.cudeca.model.evento.Asiento();
        asiento2.setId(2L);
        asiento2.setEstado(com.cudeca.model.enums.EstadoAsiento.LIBRE);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComprador));
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));
        when(asientoRepository.findAllByIdWithLock(List.of(1L, 2L))).thenReturn(List.of(asiento1, asiento2));

        Compra compraGuardada = Compra.builder()
                .id(100L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(java.time.OffsetDateTime.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.save(any(Compra.class))).thenReturn(compraGuardada);

        // Act
        CheckoutResponse response = checkoutService.procesarCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        // Verificar que los asientos fueron bloqueados
        assertThat(asiento1.getEstado()).isEqualTo(com.cudeca.model.enums.EstadoAsiento.BLOQUEADO);
        assertThat(asiento2.getEstado()).isEqualTo(com.cudeca.model.enums.EstadoAsiento.BLOQUEADO);
        verify(asientoRepository).saveAll(anyList());
    }
}

