package com.cudeca.service.impl;

import com.cudeca.dto.CheckoutRequest;
import com.cudeca.dto.CheckoutResponse;
import com.cudeca.model.enums.EstadoCompra;
import com.cudeca.model.negocio.Compra;
import com.cudeca.model.evento.TipoEntrada;
import com.cudeca.model.usuario.Comprador;
import com.cudeca.model.usuario.Invitado;
import com.cudeca.repository.CompraRepository;
import com.cudeca.repository.InvitadoRepository;
import com.cudeca.repository.TipoEntradaRepository;
import com.cudeca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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
    private com.cudeca.repository.AsientoRepository asientoRepository;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    private Comprador usuarioComprador;
    private TipoEntrada tipoEntrada;
    private CheckoutRequest checkoutRequest;

    @BeforeEach
    void setUp() {
        // Usuario comprador de prueba
        usuarioComprador = new Comprador();
        usuarioComprador.setId(1L);
        usuarioComprador.setNombre("Juan Pérez");
        usuarioComprador.setEmail("juan@example.com");

        // Tipo de entrada de prueba
        tipoEntrada = TipoEntrada.builder()
                .id(1L)
                .nombre("Entrada General")
                .costeBase(BigDecimal.valueOf(25.00))
                .cantidadTotal(100)
                .build();

        // Request de checkout básico
        checkoutRequest = new CheckoutRequest();
        checkoutRequest.setUsuarioId(1L);
        checkoutRequest.setDonacionExtra(5.0);

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
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComprador));
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));

        Compra compraGuardada = Compra.builder()
                .id(100L)
                .usuario(usuarioComprador)
                .estado(EstadoCompra.PENDIENTE)
                .fecha(Instant.now())
                .articulos(new ArrayList<>())
                .build();

        when(compraRepository.save(any(Compra.class))).thenReturn(compraGuardada);

        // Act
        CheckoutResponse response = checkoutService.procesarCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCompraId()).isEqualTo(100L);

        verify(usuarioRepository).findById(1L);
        verify(tipoEntradaRepository).findById(1L);
        verify(compraRepository).save(any(Compra.class));

        // Verificar que se creó la compra con los datos correctos
        ArgumentCaptor<Compra> compraCaptor = ArgumentCaptor.forClass(Compra.class);
        verify(compraRepository).save(compraCaptor.capture());

        Compra compraPersistida = compraCaptor.getValue();
        assertThat(compraPersistida.getUsuario()).isEqualTo(usuarioComprador);
        assertThat(compraPersistida.getEstado()).isEqualTo(EstadoCompra.PENDIENTE);
        assertThat(compraPersistida.getArticulos()).isNotEmpty();
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
                .fecha(Instant.now())
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
                .fecha(Instant.now())
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
                .fecha(Instant.now())
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
                .fecha(Instant.now())
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
                .fecha(Instant.now())
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
                .fecha(Instant.now())
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
                .fecha(Instant.now())
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
                .fecha(Instant.now())
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
                .fecha(Instant.now())
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
}

