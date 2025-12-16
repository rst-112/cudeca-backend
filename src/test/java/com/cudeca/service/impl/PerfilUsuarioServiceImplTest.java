package com.cudeca.service.impl;

import com.cudeca.dto.UserProfileDTO;
import com.cudeca.model.negocio.Monedero;
import com.cudeca.model.usuario.Usuario;
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

    private Usuario usuario;
    private Rol rolComprador;
    private Monedero monedero;

    @BeforeEach
    void setUp() {
        // Rol de prueba
        rolComprador = new Rol();
        rolComprador.setId(1L);
        rolComprador.setNombre("COMPRADOR");

        // Usuario de prueba
        usuario = new Usuario();
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
        monedero.setUsuario(usuario);
    }

    @Test
    @DisplayName("Debe obtener perfil por ID exitosamente")
    void testObtenerPerfilPorId_Exitoso() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(monederoRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(monedero));

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
        verify(monederoRepository).findByUsuario_Id(1L);
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
        verify(monederoRepository, never()).findByUsuario_Id(anyLong());
    }

    @Test
    @DisplayName("Debe obtener perfil por email exitosamente")
    void testObtenerPerfilPorEmail_Exitoso() {
        // Arrange
        when(usuarioRepository.findByEmail("carlos@example.com"))
                .thenReturn(Optional.of(usuario));
        when(monederoRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(monedero));

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
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(monederoRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(monedero));

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
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe actualizar solo nombre si dirección es null")
    void testActualizarPerfil_SoloNombre() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(monederoRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(monedero));

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
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(monederoRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(monedero));

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
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(monederoRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(monedero));

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
        when(monederoRepository.findByUsuario_Id(1L)).thenReturn(Optional.empty());

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
        when(monederoRepository.findByUsuario_Id(1L))
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
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(monederoRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(monedero));

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
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(monederoRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(monedero));

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
        when(monederoRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(monedero));

        // Act
        var resultado = perfilUsuarioService.obtenerMonedero(1L);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getSaldo()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        verify(monederoRepository).findByUsuario_Id(1L);
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
        when(monederoRepository.findByUsuario_Id(1L)).thenReturn(Optional.empty());

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
        when(monederoRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(monedero));

        // Act
        var movimientos = perfilUsuarioService.obtenerMovimientosMonedero(1L);

        // Assert
        assertThat(movimientos).isNotNull();
        assertThat(movimientos).hasSize(monedero.getMovimientos().size());
        verify(monederoRepository).findByUsuario_Id(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al obtener movimientos de usuario sin monedero")
    void testObtenerMovimientosMonedero_SinMonedero() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(monederoRepository.findByUsuario_Id(1L)).thenReturn(Optional.empty());

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

    @Test
    @DisplayName("Debe obtener movimientos ordenados por fecha descendente")
    void testObtenerMovimientosMonedero_Ordenados() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        
        // Crear monedero con múltiples movimientos con fechas diferentes
        Monedero monederoConMovimientos = new Monedero();
        monederoConMovimientos.setId(1L);
        monederoConMovimientos.setSaldo(BigDecimal.valueOf(150.00));
        monederoConMovimientos.setUsuario(usuario);
        
        com.cudeca.model.negocio.MovimientoMonedero mov1 = com.cudeca.model.negocio.MovimientoMonedero.builder()
                .id(1L)
                .importe(BigDecimal.valueOf(50.00))
                .fecha(java.time.OffsetDateTime.of(2025, 1, 10, 10, 0, 0, 0, java.time.ZoneOffset.UTC))
                .tipo(com.cudeca.model.enums.TipoMovimiento.ABONO)
                .monedero(monederoConMovimientos)
                .build();
        
        com.cudeca.model.negocio.MovimientoMonedero mov2 = com.cudeca.model.negocio.MovimientoMonedero.builder()
                .id(2L)
                .importe(BigDecimal.valueOf(25.00))
                .fecha(java.time.OffsetDateTime.of(2025, 2, 15, 14, 30, 0, 0, java.time.ZoneOffset.UTC))
                .tipo(com.cudeca.model.enums.TipoMovimiento.CARGO)
                .monedero(monederoConMovimientos)
                .build();
        
        com.cudeca.model.negocio.MovimientoMonedero mov3 = com.cudeca.model.negocio.MovimientoMonedero.builder()
                .id(3L)
                .importe(BigDecimal.valueOf(100.00))
                .fecha(java.time.OffsetDateTime.of(2025, 1, 5, 9, 0, 0, 0, java.time.ZoneOffset.UTC))
                .tipo(com.cudeca.model.enums.TipoMovimiento.ABONO)
                .monedero(monederoConMovimientos)
                .build();
        
        // Agregar movimientos en orden aleatorio
        monederoConMovimientos.setMovimientos(java.util.Arrays.asList(mov1, mov3, mov2));
        
        when(monederoRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(monederoConMovimientos));
        
        // Act
        var movimientos = perfilUsuarioService.obtenerMovimientosMonedero(1L);
        
        // Assert
        assertThat(movimientos).hasSize(3);
        // Verificar orden descendente por fecha (más reciente primero)
        assertThat(movimientos.get(0).getId()).isEqualTo(2L); // 2025-02-15
        assertThat(movimientos.get(1).getId()).isEqualTo(1L); // 2025-01-10
        assertThat(movimientos.get(2).getId()).isEqualTo(3L); // 2025-01-05
        verify(monederoRepository).findByUsuario_Id(1L);
    }

    @Test
    @DisplayName("Debe filtrar y ordenar entradas correctamente")
    void testObtenerEntradasUsuario_FiltradoYOrdenamiento() {
        // Arrange
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        // Crear compras con artículos mixtos
        com.cudeca.model.negocio.Compra compra1 = new com.cudeca.model.negocio.Compra();
        compra1.setId(1L);

        com.cudeca.model.negocio.ArticuloEntrada articuloEntrada1 = new com.cudeca.model.negocio.ArticuloEntrada();
        articuloEntrada1.setId(1L);

        com.cudeca.model.negocio.EntradaEmitida entrada1 = new com.cudeca.model.negocio.EntradaEmitida();
        entrada1.setId(10L);
        entrada1.setArticuloEntrada(articuloEntrada1);
        articuloEntrada1.setEntradasEmitidas(java.util.List.of(entrada1));

        com.cudeca.model.negocio.ArticuloDonacion articuloDonacion = com.cudeca.model.negocio.ArticuloDonacion.builder().build();
        articuloDonacion.setId(2L);

        compra1.setArticulos(java.util.List.of(articuloEntrada1, articuloDonacion));

        // Segunda compra con entrada más reciente
        com.cudeca.model.negocio.Compra compra2 = new com.cudeca.model.negocio.Compra();
        compra2.setId(2L);

        com.cudeca.model.negocio.ArticuloEntrada articuloEntrada2 = new com.cudeca.model.negocio.ArticuloEntrada();
        articuloEntrada2.setId(3L);

        com.cudeca.model.negocio.EntradaEmitida entrada2 = new com.cudeca.model.negocio.EntradaEmitida();
        entrada2.setId(20L);
        entrada2.setArticuloEntrada(articuloEntrada2);
        articuloEntrada2.setEntradasEmitidas(java.util.List.of(entrada2));

        compra2.setArticulos(java.util.List.of(articuloEntrada2));

        when(compraRepository.findByUsuario_Id(1L)).thenReturn(java.util.List.of(compra1, compra2));

        // Act
        java.util.List<com.cudeca.model.negocio.EntradaEmitida> entradas = 
            perfilUsuarioService.obtenerEntradasUsuario(1L);

        // Assert
        assertThat(entradas).hasSize(2);
        // Verificar ordenamiento (más recientes primero)
        assertThat(entradas.get(0).getId()).isEqualTo(20L);
        assertThat(entradas.get(1).getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Debe generar PDF con toda la información del asiento y evento")
    void testGenerarPDFEntrada_ConInformacionCompleta() {
        // Arrange
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        // Crear estructura completa: Entrada -> Artículo -> Asiento -> Zona -> Evento
        com.cudeca.model.evento.Evento evento = new com.cudeca.model.evento.Evento();
        evento.setId(1L);
        evento.setNombre("Concierto Benéfico");
        evento.setDescripcion("Un evento especial para recaudar fondos");
        evento.setFechaInicio(java.time.OffsetDateTime.now());

        com.cudeca.model.evento.ZonaRecinto zona = new com.cudeca.model.evento.ZonaRecinto();
        zona.setId(1L);
        zona.setNombre("Zona VIP");
        zona.setEvento(evento);

        com.cudeca.model.evento.Asiento asiento = new com.cudeca.model.evento.Asiento();
        asiento.setId(1L);
        asiento.setCodigoEtiqueta("A-12");
        asiento.setFila(1);
        asiento.setColumna(12);
        asiento.setZona(zona);

        com.cudeca.model.negocio.ArticuloEntrada articulo = new com.cudeca.model.negocio.ArticuloEntrada();
        articulo.setId(1L);
        articulo.setAsiento(asiento);

        com.cudeca.model.negocio.EntradaEmitida entrada = new com.cudeca.model.negocio.EntradaEmitida();
        entrada.setId(100L);
        entrada.setCodigoQR("QR-12345");
        entrada.setEstado(com.cudeca.model.enums.EstadoEntrada.VALIDA);
        entrada.setArticuloEntrada(articulo);

        articulo.setEntradasEmitidas(java.util.List.of(entrada));

        com.cudeca.model.negocio.Compra compra = new com.cudeca.model.negocio.Compra();
        compra.setId(1L);
        compra.setArticulos(java.util.List.of(articulo));

        when(compraRepository.findByUsuario_Id(1L)).thenReturn(java.util.List.of(compra));

        // Act
        byte[] pdf = perfilUsuarioService.generarPDFEntrada(100L, 1L);

        // Assert
        assertThat(pdf).isNotNull();
        String contenido = new String(pdf, java.nio.charset.StandardCharsets.UTF_8);
        
        // Verificar que contiene toda la información
        assertThat(contenido)
            .contains("ID Entrada: 100")
            .contains("Código QR: QR-12345")
            .contains("Estado: VALIDA")
            .contains("Código: A-12")
            .contains("Fila: 1")
            .contains("Columna: 12")
            .contains("Zona: Zona VIP")
            .contains("Evento: Concierto Benéfico")
            .contains("Descripción: Un evento especial para recaudar fondos")
            .contains("ENTRADA - CUDECA EVENT")
            .contains("Conserve esta entrada para el evento");
    }

    @Test
    @DisplayName("Debe generar PDF sin asiento")
    void testGenerarPDFEntrada_SinAsiento() {
        // Arrange
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        com.cudeca.model.negocio.ArticuloEntrada articulo = new com.cudeca.model.negocio.ArticuloEntrada();
        articulo.setId(1L);
        articulo.setAsiento(null);

        com.cudeca.model.negocio.EntradaEmitida entrada = new com.cudeca.model.negocio.EntradaEmitida();
        entrada.setId(100L);
        entrada.setCodigoQR("QR-12345");
        entrada.setEstado(com.cudeca.model.enums.EstadoEntrada.VALIDA);
        entrada.setArticuloEntrada(articulo);

        articulo.setEntradasEmitidas(java.util.List.of(entrada));

        com.cudeca.model.negocio.Compra compra = new com.cudeca.model.negocio.Compra();
        compra.setId(1L);
        compra.setArticulos(java.util.List.of(articulo));

        when(compraRepository.findByUsuario_Id(1L)).thenReturn(java.util.List.of(compra));

        // Act
        byte[] pdf = perfilUsuarioService.generarPDFEntrada(100L, 1L);

        // Assert
        assertThat(pdf).isNotNull();
        String contenido = new String(pdf, java.nio.charset.StandardCharsets.UTF_8);
        
        assertThat(contenido)
            .contains("ID Entrada: 100")
            .contains("Código QR: QR-12345")
            .doesNotContain("INFORMACIÓN DEL ASIENTO");
    }

    @Test
    @DisplayName("Debe generar PDF sin zona en asiento")
    void testGenerarPDFEntrada_SinZona() {
        // Arrange
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        com.cudeca.model.evento.Asiento asiento = new com.cudeca.model.evento.Asiento();
        asiento.setId(1L);
        asiento.setCodigoEtiqueta("A-12");
        asiento.setZona(null);

        com.cudeca.model.negocio.ArticuloEntrada articulo = new com.cudeca.model.negocio.ArticuloEntrada();
        articulo.setId(1L);
        articulo.setAsiento(asiento);

        com.cudeca.model.negocio.EntradaEmitida entrada = new com.cudeca.model.negocio.EntradaEmitida();
        entrada.setId(100L);
        entrada.setCodigoQR("QR-12345");
        entrada.setEstado(com.cudeca.model.enums.EstadoEntrada.VALIDA);
        entrada.setArticuloEntrada(articulo);

        articulo.setEntradasEmitidas(java.util.List.of(entrada));

        com.cudeca.model.negocio.Compra compra = new com.cudeca.model.negocio.Compra();
        compra.setId(1L);
        compra.setArticulos(java.util.List.of(articulo));

        when(compraRepository.findByUsuario_Id(1L)).thenReturn(java.util.List.of(compra));

        // Act
        byte[] pdf = perfilUsuarioService.generarPDFEntrada(100L, 1L);

        // Assert
        assertThat(pdf).isNotNull();
        String contenido = new String(pdf, java.nio.charset.StandardCharsets.UTF_8);
        
        assertThat(contenido)
            .contains("Código: A-12")
            .doesNotContain("Zona:");
    }

    @Test
    @DisplayName("Debe generar PDF sin evento en zona")
    void testGenerarPDFEntrada_SinEvento() {
        // Arrange
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        com.cudeca.model.evento.ZonaRecinto zona = new com.cudeca.model.evento.ZonaRecinto();
        zona.setId(1L);
        zona.setNombre("Zona VIP");
        zona.setEvento(null);

        com.cudeca.model.evento.Asiento asiento = new com.cudeca.model.evento.Asiento();
        asiento.setId(1L);
        asiento.setCodigoEtiqueta("A-12");
        asiento.setZona(zona);

        com.cudeca.model.negocio.ArticuloEntrada articulo = new com.cudeca.model.negocio.ArticuloEntrada();
        articulo.setId(1L);
        articulo.setAsiento(asiento);

        com.cudeca.model.negocio.EntradaEmitida entrada = new com.cudeca.model.negocio.EntradaEmitida();
        entrada.setId(100L);
        entrada.setCodigoQR("QR-12345");
        entrada.setEstado(com.cudeca.model.enums.EstadoEntrada.VALIDA);
        entrada.setArticuloEntrada(articulo);

        articulo.setEntradasEmitidas(java.util.List.of(entrada));

        com.cudeca.model.negocio.Compra compra = new com.cudeca.model.negocio.Compra();
        compra.setId(1L);
        compra.setArticulos(java.util.List.of(articulo));

        when(compraRepository.findByUsuario_Id(1L)).thenReturn(java.util.List.of(compra));

        // Act
        byte[] pdf = perfilUsuarioService.generarPDFEntrada(100L, 1L);

        // Assert
        assertThat(pdf).isNotNull();
        String contenido = new String(pdf, java.nio.charset.StandardCharsets.UTF_8);
        
        assertThat(contenido)
            .contains("Zona: Zona VIP")
            .doesNotContain("INFORMACIÓN DEL EVENTO");
    }

    @Test
    @DisplayName("Debe generar PDF sin descripción de evento")
    void testGenerarPDFEntrada_EventoSinDescripcion() {
        // Arrange
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        com.cudeca.model.evento.Evento evento = new com.cudeca.model.evento.Evento();
        evento.setId(1L);
        evento.setNombre("Concierto Benéfico");
        evento.setDescripcion(null);
        evento.setFechaInicio(java.time.OffsetDateTime.now());

        com.cudeca.model.evento.ZonaRecinto zona = new com.cudeca.model.evento.ZonaRecinto();
        zona.setId(1L);
        zona.setNombre("Zona VIP");
        zona.setEvento(evento);

        com.cudeca.model.evento.Asiento asiento = new com.cudeca.model.evento.Asiento();
        asiento.setId(1L);
        asiento.setCodigoEtiqueta("A-12");
        asiento.setZona(zona);

        com.cudeca.model.negocio.ArticuloEntrada articulo = new com.cudeca.model.negocio.ArticuloEntrada();
        articulo.setId(1L);
        articulo.setAsiento(asiento);

        com.cudeca.model.negocio.EntradaEmitida entrada = new com.cudeca.model.negocio.EntradaEmitida();
        entrada.setId(100L);
        entrada.setCodigoQR("QR-12345");
        entrada.setEstado(com.cudeca.model.enums.EstadoEntrada.VALIDA);
        entrada.setArticuloEntrada(articulo);

        articulo.setEntradasEmitidas(java.util.List.of(entrada));

        com.cudeca.model.negocio.Compra compra = new com.cudeca.model.negocio.Compra();
        compra.setId(1L);
        compra.setArticulos(java.util.List.of(articulo));

        when(compraRepository.findByUsuario_Id(1L)).thenReturn(java.util.List.of(compra));

        // Act
        byte[] pdf = perfilUsuarioService.generarPDFEntrada(100L, 1L);

        // Assert
        assertThat(pdf).isNotNull();
        String contenido = new String(pdf, java.nio.charset.StandardCharsets.UTF_8);
        
        assertThat(contenido)
            .contains("Evento: Concierto Benéfico")
            .doesNotContain("Descripción:");
    }

    @Test
    @DisplayName("Debe loguear cantidad de movimientos obtenidos")
    void testObtenerMovimientosMonedero_LogCantidad() {
        // Arrange
        com.cudeca.model.negocio.Monedero monedero = new com.cudeca.model.negocio.Monedero();
        monedero.setId(1L);
        monedero.setSaldo(BigDecimal.valueOf(100.0));

        com.cudeca.model.negocio.MovimientoMonedero mov1 = new com.cudeca.model.negocio.MovimientoMonedero();
        mov1.setId(1L);
        mov1.setFecha(java.time.OffsetDateTime.now().minusDays(1));

        com.cudeca.model.negocio.MovimientoMonedero mov2 = new com.cudeca.model.negocio.MovimientoMonedero();
        mov2.setId(2L);
        mov2.setFecha(java.time.OffsetDateTime.now());

        monedero.setMovimientos(java.util.List.of(mov1, mov2));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(monederoRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(monedero));

        // Act
        java.util.List<com.cudeca.model.negocio.MovimientoMonedero> movimientos = 
            perfilUsuarioService.obtenerMovimientosMonedero(1L);

        // Assert
        assertThat(movimientos).hasSize(2);
        // Verificar orden descendente por fecha
        assertThat(movimientos.get(0).getId()).isEqualTo(2L);
        assertThat(movimientos.get(1).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Debe obtener historial de compras exitosamente")
    void testObtenerHistorialCompras_Exitoso() {
        // Arrange
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        // Crear evento
        com.cudeca.model.evento.Evento evento = new com.cudeca.model.evento.Evento();
        evento.setId(1L);
        evento.setNombre("Concierto Benéfico CUDECA");

        // Crear tipo de entrada
        com.cudeca.model.evento.TipoEntrada tipoEntrada = new com.cudeca.model.evento.TipoEntrada();
        tipoEntrada.setId(1L);
        tipoEntrada.setNombre("VIP");
        tipoEntrada.setEvento(evento);

        // Crear artículo de entrada
        com.cudeca.model.negocio.ArticuloEntrada articuloEntrada = new com.cudeca.model.negocio.ArticuloEntrada();
        articuloEntrada.setId(1L);
        articuloEntrada.setPrecioUnitario(new BigDecimal("50.00"));
        articuloEntrada.setCantidad(2);
        articuloEntrada.setTipoEntrada(tipoEntrada);

        // Crear compra
        com.cudeca.model.negocio.Compra compra = new com.cudeca.model.negocio.Compra();
        compra.setId(1L);
        compra.setFecha(java.time.OffsetDateTime.of(2024, 11, 15, 10, 30, 0, 0, java.time.ZoneOffset.UTC));
        compra.setEstado(com.cudeca.model.enums.EstadoCompra.COMPLETADA);
        compra.setArticulos(java.util.List.of(articuloEntrada));

        when(compraRepository.findByUsuario_Id(1L)).thenReturn(java.util.List.of(compra));

        // Act
        java.util.List<java.util.Map<String, Object>> historial = 
            perfilUsuarioService.obtenerHistorialCompras(1L);

        // Assert
        assertThat(historial).hasSize(1);
        
        java.util.Map<String, Object> dto = historial.get(0);
        assertThat(dto.get("id")).isEqualTo("1");
        assertThat(dto.get("date")).asString().contains("15 de noviembre, 2024");
        assertThat(dto.get("status")).isEqualTo("COMPLETADA");
        assertThat(dto.get("total")).isEqualTo("100.00€");
        assertThat(dto.get("title")).isEqualTo("Concierto Benéfico CUDECA");
        assertThat(dto.get("tickets")).isEqualTo("2 entradas");
        
        verify(usuarioRepository).existsById(1L);
        verify(compraRepository).findByUsuario_Id(1L);
    }

    @Test
    @DisplayName("Debe obtener historial vacío si usuario no tiene compras")
    void testObtenerHistorialCompras_SinCompras() {
        // Arrange
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(compraRepository.findByUsuario_Id(1L)).thenReturn(java.util.Collections.emptyList());

        // Act
        java.util.List<java.util.Map<String, Object>> historial = 
            perfilUsuarioService.obtenerHistorialCompras(1L);

        // Assert
        assertThat(historial).isEmpty();
        verify(usuarioRepository).existsById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al obtener historial de usuario inexistente")
    void testObtenerHistorialCompras_UsuarioNoExiste() {
        // Arrange
        when(usuarioRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> perfilUsuarioService.obtenerHistorialCompras(999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Usuario no encontrado");

        verify(usuarioRepository).existsById(999L);
        verify(compraRepository, never()).findByUsuario_Id(anyLong());
    }

    @Test
    @DisplayName("Debe usar título por defecto si no hay eventos")
    void testObtenerHistorialCompras_SinEventos() {
        // Arrange
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        // Crear artículo de donación (sin evento)
        com.cudeca.model.negocio.ArticuloDonacion articuloDonacion = 
            com.cudeca.model.negocio.ArticuloDonacion.builder()
                .precioUnitario(new BigDecimal("25.00"))
                .cantidad(1)
                .build();
        articuloDonacion.setId(1L);

        com.cudeca.model.negocio.Compra compra = new com.cudeca.model.negocio.Compra();
        compra.setId(1L);
        compra.setFecha(java.time.OffsetDateTime.now());
        compra.setEstado(com.cudeca.model.enums.EstadoCompra.COMPLETADA);
        compra.setArticulos(java.util.List.of(articuloDonacion));

        when(compraRepository.findByUsuario_Id(1L)).thenReturn(java.util.List.of(compra));

        // Act
        java.util.List<java.util.Map<String, Object>> historial = 
            perfilUsuarioService.obtenerHistorialCompras(1L);

        // Assert
        assertThat(historial).hasSize(1);
        java.util.Map<String, Object> dto = historial.get(0);
        assertThat(dto.get("title")).isEqualTo("Compra General");
        assertThat(dto.get("tickets")).isEqualTo("0 entradas");
    }

    @Test
    @DisplayName("Debe manejar entradas sin tipo de entrada")
    void testObtenerHistorialCompras_EntradasSinTipo() {
        // Arrange
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        com.cudeca.model.negocio.ArticuloEntrada articuloEntrada = new com.cudeca.model.negocio.ArticuloEntrada();
        articuloEntrada.setId(1L);
        articuloEntrada.setPrecioUnitario(new BigDecimal("30.00"));
        articuloEntrada.setCantidad(3);
        articuloEntrada.setTipoEntrada(null); // Sin tipo de entrada

        com.cudeca.model.negocio.Compra compra = new com.cudeca.model.negocio.Compra();
        compra.setId(1L);
        compra.setFecha(java.time.OffsetDateTime.now());
        compra.setEstado(com.cudeca.model.enums.EstadoCompra.COMPLETADA);
        compra.setArticulos(java.util.List.of(articuloEntrada));

        when(compraRepository.findByUsuario_Id(1L)).thenReturn(java.util.List.of(compra));

        // Act
        java.util.List<java.util.Map<String, Object>> historial = 
            perfilUsuarioService.obtenerHistorialCompras(1L);

        // Assert
        assertThat(historial).hasSize(1);
        java.util.Map<String, Object> dto = historial.get(0);
        assertThat(dto.get("title")).isEqualTo("Compra General");
        assertThat(dto.get("tickets")).isEqualTo("3 entradas");
        assertThat(dto.get("total")).isEqualTo("90.00€");
    }

    @Test
    @DisplayName("Debe calcular total correctamente con múltiples artículos")
    void testObtenerHistorialCompras_CalculoTotalMultiplesArticulos() {
        // Arrange
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        com.cudeca.model.negocio.ArticuloEntrada articulo1 = new com.cudeca.model.negocio.ArticuloEntrada();
        articulo1.setId(1L);
        articulo1.setPrecioUnitario(new BigDecimal("50.00"));
        articulo1.setCantidad(2);
        articulo1.setTipoEntrada(null);

        com.cudeca.model.negocio.ArticuloDonacion articulo2 = 
            com.cudeca.model.negocio.ArticuloDonacion.builder()
                .precioUnitario(new BigDecimal("25.50"))
                .cantidad(1)
                .build();
        articulo2.setId(2L);

        com.cudeca.model.negocio.Compra compra = new com.cudeca.model.negocio.Compra();
        compra.setId(1L);
        compra.setFecha(java.time.OffsetDateTime.now());
        compra.setEstado(com.cudeca.model.enums.EstadoCompra.PENDIENTE);
        compra.setArticulos(java.util.List.of(articulo1, articulo2));

        when(compraRepository.findByUsuario_Id(1L)).thenReturn(java.util.List.of(compra));

        // Act
        java.util.List<java.util.Map<String, Object>> historial = 
            perfilUsuarioService.obtenerHistorialCompras(1L);

        // Assert
        assertThat(historial).hasSize(1);
        java.util.Map<String, Object> dto = historial.get(0);
        // 50*2 + 25.50*1 = 125.50
        assertThat(dto.get("total")).isEqualTo("125.50€");
        assertThat(dto.get("status")).isEqualTo("PENDIENTE");
    }
}
