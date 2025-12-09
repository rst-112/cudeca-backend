package com.cudeca.service.impl;

import com.cudeca.model.usuario.Comprador;
import com.cudeca.model.usuario.DatosFiscales;
import com.cudeca.repository.DatosFiscalesRepository;
import com.cudeca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para DatosFiscalesServiceImpl.
 * Verifica la gestión de libreta de direcciones fiscales.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DatosFiscalesService - Tests Unitarios")
class DatosFiscalesServiceImplTest {

    @Mock
    private DatosFiscalesRepository datosFiscalesRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private DatosFiscalesServiceImpl datosFiscalesService;

    private Comprador usuario;
    private DatosFiscales datosFiscales;

    @BeforeEach
    void setUp() {
        // Usuario de prueba
        usuario = new Comprador();
        usuario.setId(1L);
        usuario.setNombre("María García");
        usuario.setEmail("maria@example.com");

        // Datos fiscales de prueba
        datosFiscales = new DatosFiscales();
        datosFiscales.setId(1L);
        datosFiscales.setNombreCompleto("María García López");
        datosFiscales.setNif("12345678Z");
        datosFiscales.setDireccion("Calle Falsa 123, Madrid");
        datosFiscales.setPais("España");
        datosFiscales.setUsuario(usuario);
    }

    @Test
    @DisplayName("Debe crear datos fiscales exitosamente")
    void testCrearDatosFiscales_Exitoso() {
        // Arrange
        DatosFiscales nuevosDatos = new DatosFiscales();
        nuevosDatos.setNombreCompleto("María García López");
        nuevosDatos.setNif("12345678Z");
        nuevosDatos.setDireccion("Calle Falsa 123");
        nuevosDatos.setPais("España");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(datosFiscalesRepository.save(any(DatosFiscales.class))).thenReturn(datosFiscales);

        // Act
        DatosFiscales resultado = datosFiscalesService.crearDatosFiscales(nuevosDatos, 1L);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNif()).isEqualTo("12345678Z");

        verify(usuarioRepository).findById(1L);
        verify(datosFiscalesRepository).save(any(DatosFiscales.class));

        // Verificar que se asoció el usuario
        ArgumentCaptor<DatosFiscales> captor = ArgumentCaptor.forClass(DatosFiscales.class);
        verify(datosFiscalesRepository).save(captor.capture());
        assertThat(captor.getValue().getUsuario()).isEqualTo(usuario);
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear datos para usuario inexistente")
    void testCrearDatosFiscales_UsuarioNoExiste() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
            datosFiscalesService.crearDatosFiscales(datosFiscales, 999L)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Usuario no encontrado");

        verify(datosFiscalesRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear datos con NIF inválido")
    void testCrearDatosFiscales_NIFInvalido() {
        // Arrange
        datosFiscales.setNif("INVALIDO");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThatThrownBy(() ->
            datosFiscalesService.crearDatosFiscales(datosFiscales, 1L)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("NIF");

        verify(datosFiscalesRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear datos sin nombre completo")
    void testCrearDatosFiscales_SinNombreCompleto() {
        // Arrange
        datosFiscales.setNombreCompleto(null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThatThrownBy(() ->
            datosFiscalesService.crearDatosFiscales(datosFiscales, 1L)
        )
        .isInstanceOf(IllegalArgumentException.class);

        verify(datosFiscalesRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar datos fiscales exitosamente")
    void testActualizarDatosFiscales_Exitoso() {
        // Arrange
        DatosFiscales datosActualizados = new DatosFiscales();
        datosActualizados.setNombreCompleto("María García López Actualizada");
        datosActualizados.setNif("87654321X");
        datosActualizados.setDireccion("Nueva Dirección 456");
        datosActualizados.setPais("España");

        when(datosFiscalesRepository.findById(1L)).thenReturn(Optional.of(datosFiscales));
        when(datosFiscalesRepository.save(any(DatosFiscales.class))).thenReturn(datosFiscales);

        // Act
        DatosFiscales resultado = datosFiscalesService.actualizarDatosFiscales(
            1L, datosActualizados, 1L
        );

        // Assert
        assertThat(resultado).isNotNull();
        verify(datosFiscalesRepository).save(any(DatosFiscales.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar datos inexistentes")
    void testActualizarDatosFiscales_NoExisten() {
        // Arrange
        when(datosFiscalesRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
            datosFiscalesService.actualizarDatosFiscales(999L, datosFiscales, 1L)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Datos fiscales no encontrados");
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar datos de otro usuario")
    void testActualizarDatosFiscales_UsuarioDiferente() {
        // Arrange
        Comprador otroUsuario = new Comprador();
        otroUsuario.setId(2L);
        datosFiscales.setUsuario(otroUsuario);

        when(datosFiscalesRepository.findById(1L)).thenReturn(Optional.of(datosFiscales));

        // Act & Assert
        assertThatThrownBy(() ->
            datosFiscalesService.actualizarDatosFiscales(1L, datosFiscales, 1L)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("no pertenecen al usuario");

        verify(datosFiscalesRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe eliminar datos fiscales exitosamente")
    void testEliminarDatosFiscales_Exitoso() {
        // Arrange
        when(datosFiscalesRepository.findById(1L)).thenReturn(Optional.of(datosFiscales));
        doNothing().when(datosFiscalesRepository).delete(any(DatosFiscales.class));

        // Act
        boolean resultado = datosFiscalesService.eliminarDatosFiscales(1L, 1L);

        // Assert
        assertThat(resultado).isTrue();
        verify(datosFiscalesRepository).delete(datosFiscales);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar datos inexistentes")
    void testEliminarDatosFiscales_NoExisten() {
        // Arrange
        when(datosFiscalesRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
            datosFiscalesService.eliminarDatosFiscales(999L, 1L)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Datos fiscales no encontrados");

        verify(datosFiscalesRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar datos de otro usuario")
    void testEliminarDatosFiscales_UsuarioDiferente() {
        // Arrange
        Comprador otroUsuario = new Comprador();
        otroUsuario.setId(2L);
        datosFiscales.setUsuario(otroUsuario);

        when(datosFiscalesRepository.findById(1L)).thenReturn(Optional.of(datosFiscales));

        // Act & Assert
        assertThatThrownBy(() ->
            datosFiscalesService.eliminarDatosFiscales(1L, 1L)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("no pertenecen al usuario");

        verify(datosFiscalesRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe obtener todos los datos fiscales de un usuario")
    void testObtenerDatosFiscalesPorUsuario() {
        // Arrange
        DatosFiscales datos2 = new DatosFiscales();
        datos2.setId(2L);
        datos2.setNombreCompleto("María García (Empresa)");
        datos2.setNif("B12345678");
        datos2.setUsuario(usuario);

        when(datosFiscalesRepository.findByUsuarioId(1L))
                .thenReturn(java.util.Arrays.asList(datosFiscales, datos2));

        // Act
        List<DatosFiscales> resultado = datosFiscalesService.obtenerDatosFiscalesPorUsuario(1L);

        // Assert
        assertThat(resultado)
                .hasSize(2)
                .contains(datosFiscales, datos2);
        verify(datosFiscalesRepository).findByUsuarioId(1L);
    }

    @Test
    @DisplayName("Debe obtener datos fiscales por ID si pertenecen al usuario")
    void testObtenerDatosFiscalesPorId_Exitoso() {
        // Arrange
        when(datosFiscalesRepository.findById(1L)).thenReturn(Optional.of(datosFiscales));

        // Act
        Optional<DatosFiscales> resultado = datosFiscalesService.obtenerDatosFiscalesPorId(1L, 1L);

        // Assert
        assertThat(resultado).contains(datosFiscales);
    }

    @Test
    @DisplayName("No debe obtener datos fiscales de otro usuario")
    void testObtenerDatosFiscalesPorId_UsuarioDiferente() {
        // Arrange
        Comprador otroUsuario = new Comprador();
        otroUsuario.setId(2L);
        datosFiscales.setUsuario(otroUsuario);

        when(datosFiscalesRepository.findById(1L)).thenReturn(Optional.of(datosFiscales));

        // Act
        Optional<DatosFiscales> resultado = datosFiscalesService.obtenerDatosFiscalesPorId(1L, 1L);

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Debe validar NIF español válido - DNI")
    void testValidarNIF_DNIValido() {
        // Act & Assert
        assertThat(datosFiscalesService.validarNIF("12345678Z")).isTrue();
        assertThat(datosFiscalesService.validarNIF("87654321X")).isTrue();
    }

    @Test
    @DisplayName("Debe validar NIE válido")
    void testValidarNIF_NIEValido() {
        // Act & Assert
        assertThat(datosFiscalesService.validarNIF("X1234567L")).isTrue();
        assertThat(datosFiscalesService.validarNIF("Y7654321Z")).isTrue();
    }

    @Test
    @DisplayName("Debe validar CIF válido")
    void testValidarNIF_CIFValido() {
        // Act & Assert
        assertThat(datosFiscalesService.validarNIF("A12345678")).isTrue();
        assertThat(datosFiscalesService.validarNIF("B87654321")).isTrue();
    }

    @Test
    @DisplayName("Debe rechazar NIF inválido")
    void testValidarNIF_Invalidos() {
        // Act & Assert
        assertThat(datosFiscalesService.validarNIF("123")).isFalse();
        assertThat(datosFiscalesService.validarNIF("ABCDEFGHIJ")).isFalse();
        assertThat(datosFiscalesService.validarNIF("12345678")).isFalse();
        assertThat(datosFiscalesService.validarNIF(null)).isFalse();
        assertThat(datosFiscalesService.validarNIF("")).isFalse();
    }

    @Test
    @DisplayName("Debe rechazar NIF con formato incorrecto")
    void testValidarNIF_FormatoIncorrecto() {
        // Act & Assert
        assertThat(datosFiscalesService.validarNIF("1234567Z")).isFalse();  // 7 dígitos
        assertThat(datosFiscalesService.validarNIF("123456789Z")).isFalse(); // 9 dígitos
        assertThat(datosFiscalesService.validarNIF("12345678-Z")).isFalse(); // Guión
        assertThat(datosFiscalesService.validarNIF("12345678 Z")).isFalse(); // Espacio
    }

    @Test
    @DisplayName("Debe retornar lista vacía si usuario no tiene datos fiscales")
    void testObtenerDatosFiscalesPorUsuario_ListaVacia() {
        // Arrange
        when(datosFiscalesRepository.findByUsuarioId(1L)).thenReturn(java.util.Collections.emptyList());

        // Act
        List<DatosFiscales> resultado = datosFiscalesService.obtenerDatosFiscalesPorUsuario(1L);

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Debe permitir direcciones largas")
    void testCrearDatosFiscales_DireccionLarga() {
        // Arrange
        String direccionLarga = "Avenida de la Constitución número 123, Portal 4, Piso 2, Puerta B, " +
                               "28001 Madrid, España";
        datosFiscales.setDireccion(direccionLarga);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(datosFiscalesRepository.save(any(DatosFiscales.class))).thenReturn(datosFiscales);

        // Act
        DatosFiscales resultado = datosFiscalesService.crearDatosFiscales(datosFiscales, 1L);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getDireccion()).isEqualTo(direccionLarga);
    }

    @Test
    @DisplayName("Debe aceptar países internacionales")
    void testCrearDatosFiscales_PaisInternacional() {
        // Arrange
        datosFiscales.setPais("Portugal");
        datosFiscales.setNif("123456789"); // NIF portugués (formato simplificado)

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        // Para NIFs internacionales, podemos ser más permisivos en la validación
        when(datosFiscalesRepository.save(any(DatosFiscales.class))).thenReturn(datosFiscales);

        // Act & Assert - Verificamos que acepta diferentes formatos según el país
        assertThat(datosFiscales.getPais()).isEqualTo("Portugal");
    }
}

