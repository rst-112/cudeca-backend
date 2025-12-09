package com.cudeca.service.impl;

import com.cudeca.dto.UserProfileDTO;
import com.cudeca.model.negocio.Monedero;
import com.cudeca.model.usuario.Comprador;
import com.cudeca.model.usuario.Rol;
import com.cudeca.repository.MonederoRepository;
import com.cudeca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PerfilUsuarioServiceImpl.
 * Verifica la gestión del perfil de usuario.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PerfilUsuarioService - Tests Unitarios")
class PerfilUsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MonederoRepository monederoRepository;

    @Mock
    private com.cudeca.repository.CompraRepository compraRepository;

    @InjectMocks
    private PerfilUsuarioServiceImpl perfilUsuarioService;

    private Comprador usuario;
    private Rol rolComprador;
    private Monedero monedero;

    @BeforeEach
    void setUp() {
        // Rol de prueba
        rolComprador = new Rol();
        rolComprador.setId(1L);
        rolComprador.setNombre("COMPRADOR");

        // Usuario de prueba
        usuario = new Comprador();
        usuario.setId(1L);
        usuario.setNombre("Carlos Martínez");
        usuario.setEmail("carlos@example.com");
        usuario.setDireccion("Calle Principal 456");

        Set<Rol> roles = new HashSet<>();
        roles.add(rolComprador);
        usuario.setRoles(roles);

        // Monedero de prueba
        monedero = new Monedero();
        monedero.setId(1L);
        monedero.setSaldo(BigDecimal.valueOf(50.00));
        monedero.setComprador(usuario);
    }

    @Test
    @DisplayName("Debe obtener perfil por ID exitosamente")
    void testObtenerPerfilPorId_Exitoso() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(monederoRepository.findByComprador_Id(1L)).thenReturn(Optional.of(monedero));

        // Act
        UserProfileDTO resultado = perfilUsuarioService.obtenerPerfilPorId(1L);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Carlos Martínez");
        assertThat(resultado.getEmail()).isEqualTo("carlos@example.com");
        assertThat(resultado.getDireccion()).isEqualTo("Calle Principal 456");
        assertThat(resultado.getRol()).isEqualTo("COMPRADOR");
        assertThat(resultado.getSaldoMonedero()).isEqualByComparingTo(BigDecimal.valueOf(50.00));

        verify(usuarioRepository).findById(1L);
        verify(monederoRepository).findByComprador_Id(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción si usuario no existe")
    void testObtenerPerfilPorId_UsuarioNoExiste() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> perfilUsuarioService.obtenerPerfilPorId(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(usuarioRepository).findById(999L);
        verify(monederoRepository, never()).findByComprador_Id(anyLong());
    }

    @Test
    @DisplayName("Debe obtener perfil por email exitosamente")
    void testObtenerPerfilPorEmail_Exitoso() {
        // Arrange
        when(usuarioRepository.findByEmail("carlos@example.com"))
                .thenReturn(Optional.of(usuario));
        when(monederoRepository.findByComprador_Id(1L)).thenReturn(Optional.of(monedero));

        // Act
        Optional<UserProfileDTO> resultado = perfilUsuarioService.obtenerPerfilPorEmail("carlos@example.com");

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEmail()).isEqualTo("carlos@example.com");
        assertThat(resultado.get().getNombre()).isEqualTo("Carlos Martínez");

        verify(usuarioRepository).findByEmail("carlos@example.com");
    }

    @Test
    @DisplayName("Debe retornar Optional vacío si email no existe")
    void testObtenerPerfilPorEmail_EmailNoExiste() {
        // Arrange
        when(usuarioRepository.findByEmail("noexiste@example.com"))
                .thenReturn(Optional.empty());

        // Act
        Optional<UserProfileDTO> resultado = perfilUsuarioService.obtenerPerfilPorEmail("noexiste@example.com");

        // Assert
        assertThat(resultado).isEmpty();
        verify(usuarioRepository).findByEmail("noexiste@example.com");
    }

    @Test
    @DisplayName("Debe retornar Optional vacío si email es null")
    void testObtenerPerfilPorEmail_EmailNull() {
        // Act
        Optional<UserProfileDTO> resultado = perfilUsuarioService.obtenerPerfilPorEmail(null);

        // Assert
        assertThat(resultado).isEmpty();
        verify(usuarioRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Debe retornar Optional vacío si email es vacío")
    void testObtenerPerfilPorEmail_EmailVacio() {
        // Act
        Optional<UserProfileDTO> resultado = perfilUsuarioService.obtenerPerfilPorEmail("   ");

        // Assert
        assertThat(resultado).isEmpty();
        verify(usuarioRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Debe actualizar perfil exitosamente")
    void testActualizarPerfil_Exitoso() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Comprador.class))).thenReturn(usuario);
        when(monederoRepository.findByComprador_Id(1L)).thenReturn(Optional.of(monedero));

        // Act
        UserProfileDTO resultado = perfilUsuarioService.actualizarPerfil(
                1L,
                "Carlos Martínez Actualizado",
                "Nueva Dirección 789"
        );

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Carlos Martínez Actualizado");
        assertThat(resultado.getDireccion()).isEqualTo("Nueva Dirección 789");

        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).save(any(Comprador.class));
    }

    @Test
    @DisplayName("Debe actualizar solo nombre si dirección es null")
    void testActualizarPerfil_SoloNombre() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Comprador.class))).thenReturn(usuario);
        when(monederoRepository.findByComprador_Id(1L)).thenReturn(Optional.of(monedero));

        // Act
        UserProfileDTO resultado = perfilUsuarioService.actualizarPerfil(
                1L,
                "Nuevo Nombre",
                null
        );

        // Assert
        assertThat(resultado).isNotNull();
        verify(usuarioRepository).save(argThat(u ->
            u.getNombre().equals("Nuevo Nombre") &&
            u.getDireccion() != null  // No debería cambiar
        ));
    }

    @Test
    @DisplayName("Debe actualizar solo dirección si nombre es null")
    void testActualizarPerfil_SoloDireccion() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Comprador.class))).thenReturn(usuario);
        when(monederoRepository.findByComprador_Id(1L)).thenReturn(Optional.of(monedero));

        // Act
        UserProfileDTO resultado = perfilUsuarioService.actualizarPerfil(
                1L,
                null,
                "Nueva Dirección"
        );

        // Assert
        assertThat(resultado).isNotNull();
        verify(usuarioRepository).save(argThat(u ->
            u.getDireccion().equals("Nueva Dirección")
        ));
    }

    @Test
    @DisplayName("Debe lanzar excepción si nombre excede 100 caracteres")
    void testActualizarPerfil_NombreMuyLargo() {
        // Arrange
        String nombreLargo = "A".repeat(101);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThatThrownBy(() ->
            perfilUsuarioService.actualizarPerfil(1L, nombreLargo, null)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("nombre no puede exceder 100 caracteres");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("No debe actualizar con nombre vacío")
    void testActualizarPerfil_NombreVacio() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Comprador.class))).thenReturn(usuario);
        when(monederoRepository.findByComprador_Id(1L)).thenReturn(Optional.of(monedero));

        // Act
        perfilUsuarioService.actualizarPerfil(1L, "   ", "Nueva Dirección");

        // Assert
        // El nombre no debería cambiar si es blank
        verify(usuarioRepository).save(argThat(u ->
            !u.getNombre().isBlank()
        ));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar perfil de usuario inexistente")
    void testActualizarPerfil_UsuarioNoExiste() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
            perfilUsuarioService.actualizarPerfil(999L, "Nombre", "Dirección")
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Usuario no encontrado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe obtener usuario por ID")
    void testObtenerUsuarioPorId_Exitoso() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act
        var resultado = perfilUsuarioService.obtenerUsuarioPorId(1L);

        // Assert
        assertThat(resultado).contains(usuario);
        verify(usuarioRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe retornar Optional vacío si usuario no existe por ID")
    void testObtenerUsuarioPorId_NoExiste() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        var resultado = perfilUsuarioService.obtenerUsuarioPorId(999L);

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Debe convertir Usuario a DTO correctamente")
    void testConvertirAPerfilDTO_Exitoso() {
        // Act
        UserProfileDTO resultado = perfilUsuarioService.convertirAPerfilDTO(usuario);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(usuario.getId());
        assertThat(resultado.getNombre()).isEqualTo(usuario.getNombre());
        assertThat(resultado.getEmail()).isEqualTo(usuario.getEmail());
        assertThat(resultado.getDireccion()).isEqualTo(usuario.getDireccion());
        assertThat(resultado.getRol()).isEqualTo("COMPRADOR");
    }

    @Test
    @DisplayName("Debe retornar null al convertir Usuario null")
    void testConvertirAPerfilDTO_UsuarioNull() {
        // Act
        UserProfileDTO resultado = perfilUsuarioService.convertirAPerfilDTO(null);

        // Assert
        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("Debe asignar rol por defecto COMPRADOR si usuario no tiene roles")
    void testConvertirAPerfilDTO_SinRoles() {
        // Arrange
        usuario.setRoles(new HashSet<>());

        // Act
        UserProfileDTO resultado = perfilUsuarioService.convertirAPerfilDTO(usuario);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getRol()).isEqualTo("COMPRADOR");
    }

    @Test
    @DisplayName("Debe obtener saldo cero si usuario no tiene monedero")
    void testObtenerPerfilPorId_SinMonedero() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(monederoRepository.findByComprador_Id(1L)).thenReturn(Optional.empty());

        // Act
        UserProfileDTO resultado = perfilUsuarioService.obtenerPerfilPorId(1L);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getSaldoMonedero()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Debe verificar que usuario existe")
    void testExisteUsuario_Existe() {
        // Arrange
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean resultado = perfilUsuarioService.existeUsuario(1L);

        // Assert
        assertThat(resultado).isTrue();
        verify(usuarioRepository).existsById(1L);
    }

    @Test
    @DisplayName("Debe verificar que usuario no existe")
    void testExisteUsuario_NoExiste() {
        // Arrange
        when(usuarioRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean resultado = perfilUsuarioService.existeUsuario(999L);

        // Assert
        assertThat(resultado).isFalse();
        verify(usuarioRepository).existsById(999L);
    }

    @Test
    @DisplayName("Debe manejar múltiples roles y obtener el primero")
    void testConvertirAPerfilDTO_MultipleRoles() {
        // Arrange
        Rol rolAdmin = new Rol();
        rolAdmin.setId(2L);
        rolAdmin.setNombre("ADMINISTRADOR");

        Set<Rol> roles = new HashSet<>();
        roles.add(rolComprador);
        roles.add(rolAdmin);
        usuario.setRoles(roles);

        // Act
        UserProfileDTO resultado = perfilUsuarioService.convertirAPerfilDTO(usuario);

        // Assert
        assertThat(resultado).isNotNull();
        // Debe obtener uno de los roles (el primero en el stream)
        assertThat(resultado.getRol()).isIn("COMPRADOR", "ADMINISTRADOR");
    }

    @Test
    @DisplayName("Debe manejar errores al obtener monedero sin fallar")
    void testObtenerPerfilPorId_ErrorMonedero() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(monederoRepository.findByComprador_Id(1L))
                .thenThrow(new RuntimeException("Error de BD"));

        // Act
        UserProfileDTO resultado = perfilUsuarioService.obtenerPerfilPorId(1L);

        // Assert
        assertThat(resultado).isNotNull();
        // Debe retornar el perfil aunque falle el monedero
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getSaldoMonedero()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Debe permitir actualizar con nombre de exactamente 100 caracteres")
    void testActualizarPerfil_NombreExacto100Caracteres() {
        // Arrange
        String nombre100 = "A".repeat(100);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Comprador.class))).thenReturn(usuario);
        when(monederoRepository.findByComprador_Id(1L)).thenReturn(Optional.of(monedero));

        // Act
        UserProfileDTO resultado = perfilUsuarioService.actualizarPerfil(1L, nombre100, null);

        // Assert
        assertThat(resultado).isNotNull();
        verify(usuarioRepository).save(argThat(u ->
            u.getNombre().length() == 100
        ));
    }

    @Test
    @DisplayName("Debe permitir dirección vacía al actualizar")
    void testActualizarPerfil_DireccionVacia() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Comprador.class))).thenReturn(usuario);
        when(monederoRepository.findByComprador_Id(1L)).thenReturn(Optional.of(monedero));

        // Act
        UserProfileDTO resultado = perfilUsuarioService.actualizarPerfil(1L, null, "");

        // Assert
        assertThat(resultado).isNotNull();
        verify(usuarioRepository).save(argThat(u ->
            u.getDireccion().isEmpty()
        ));
    }

    // --- TESTS PARA NUEVOS MÉTODOS ---

    @Test
    @DisplayName("Debe obtener entradas de usuario sin entradas")
    void testObtenerEntradasUsuario_SinEntradas() {
        // Arrange
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(compraRepository.findByUsuario_Id(1L)).thenReturn(java.util.Collections.emptyList());

        // Act
        var entradas = perfilUsuarioService.obtenerEntradasUsuario(1L);

        // Assert
        assertThat(entradas).isEmpty();
        verify(usuarioRepository).existsById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al obtener entradas de usuario inexistente")
    void testObtenerEntradasUsuario_UsuarioNoExiste() {
        // Arrange
        when(usuarioRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> perfilUsuarioService.obtenerEntradasUsuario(999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    @DisplayName("Debe obtener monedero de un comprador")
    void testObtenerMonedero_Exitoso() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(monederoRepository.findByComprador_Id(1L)).thenReturn(Optional.of(monedero));

        // Act
        var resultado = perfilUsuarioService.obtenerMonedero(1L);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getSaldo()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        verify(monederoRepository).findByComprador_Id(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al obtener monedero de usuario inexistente")
    void testObtenerMonedero_UsuarioNoExiste() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> perfilUsuarioService.obtenerMonedero(999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    @DisplayName("Debe lanzar excepción si usuario no tiene monedero")
    void testObtenerMonedero_SinMonedero() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(monederoRepository.findByComprador_Id(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> perfilUsuarioService.obtenerMonedero(1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no tiene monedero configurado");
    }

    @Test
    @DisplayName("Debe obtener movimientos del monedero ordenados por fecha descendente")
    void testObtenerMovimientosMonedero_Exitoso() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(monederoRepository.findByComprador_Id(1L)).thenReturn(Optional.of(monedero));

        // Act
        var movimientos = perfilUsuarioService.obtenerMovimientosMonedero(1L);

        // Assert
        assertThat(movimientos).isNotNull();
        assertThat(movimientos).hasSize(monedero.getMovimientos().size());
        verify(monederoRepository).findByComprador_Id(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al obtener movimientos de usuario sin monedero")
    void testObtenerMovimientosMonedero_SinMonedero() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(monederoRepository.findByComprador_Id(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> perfilUsuarioService.obtenerMovimientosMonedero(1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no tiene monedero configurado");
    }

    @Test
    @DisplayName("Debe generar PDF de entrada con contenido básico")
    void testGenerarPDFEntrada_UsuarioSinEntradas() {
        // Arrange
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(compraRepository.findByUsuario_Id(1L)).thenReturn(java.util.Collections.emptyList());

        // Act & Assert
        assertThatThrownBy(() -> perfilUsuarioService.generarPDFEntrada(999L, 1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Entrada no encontrada o no pertenece al usuario");
    }

    @Test
    @DisplayName("Debe lanzar excepción al generar PDF para usuario inexistente")
    void testGenerarPDFEntrada_UsuarioNoExiste() {
        // Arrange
        when(usuarioRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> perfilUsuarioService.generarPDFEntrada(1L, 999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Usuario no encontrado");
    }
}
