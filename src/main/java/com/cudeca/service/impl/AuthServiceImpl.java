package com.cudeca.service.impl;

// Importaciones de DTOs y Servicios
import com.cudeca.dto.AuthResponse;
import com.cudeca.dto.LoginRequest;
import com.cudeca.dto.RegisterRequest;
import com.cudeca.service.JwtService;
import com.cudeca.model.aux.EmailValidator

// Importaciones de la entidad (temporal, hasta que B3 la provea)
import com.cudeca.model.security.Usuario;
import com.cudeca.repository.UsuarioRepository; // Repositorio de B3

// Importaciones de Spring Security
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    // Constructor (inyección de dependencias)
    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    // El método de Login
    public AuthResponse login(LoginRequest request) {

        // 1. Verificación de Credenciales (DS 8, pasos 2-8)
        // Intentamos autenticar. Si falla (credenciales inválidas), el AuthenticationManager
        // lanzará una excepción (AuthenticationException).
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Si la línea anterior no lanza excepción, la autenticación fue exitosa.

        // 2. Recuperar la Entidad Usuario
        // Necesitas la entidad completa (Usuario) para obtener el ID, ya que el token JWT
        // debe incluir el 'userId' como claim.
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                // Si el usuario no existe tras la autenticación, es un error del sistema.
                .orElseThrow(() -> new RuntimeException("Error interno: Usuario autenticado no encontrado en DB."));

        // 3. Generación del Token JWT (DS 8, paso 9)
        // Usamos tu JwtService para crear el token firmado.
        String jwtToken = jwtService.generateToken(usuario);

        // 4. Retorno de la Respuesta (DS 8, pasos 10-12)
        return new AuthResponse(jwtToken);
    }
    // El método de Registro
    public AuthResponse register(RegisterRequest request) {

        // 1. VERIFICACIÓN DE UNICIDAD DEL EMAIL (Regla de Negocio)
        // Se debe verificar que el email no esté ya en uso, ya que es un campo UNIQUE en la DB.
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            // Se lanza una excepción que será manejada por el Controller (ej. devolver 400 Bad Request).
            throw new RuntimeException("El email ya está registrado en el sistema.");
        }
        if (EmailValidator.isEmailValid(request.getEmail())) {
            // Se lanza una excepción que será manejada por el Controller (ej. devolver 400 Bad Request).
            throw new RuntimeException("El email no tiene un formato válido");
        }

        // 2. CREACIÓN DE LA ENTIDAD USUARIO (Trabajo de B3)
        Usuario nuevoUsuario = new Usuario();

        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setEmail(request.getEmail());

        // 3. CIFRADO DE LA CONTRASEÑA (Seguridad)
        // Usamos el PasswordEncoder para hashear la contraseña ANTES de guardarla.
        String passwordHash = passwordEncoder.encode(request.getPassword());
        nuevoUsuario.setPasswordHash(passwordHash);

        // NOTA IMPORTANTE: Aquí se debería asignar el Rol por defecto (ej. "Comprador").
        // nuevoUsuario.setRoles(asignarRolCompradorPorDefecto());

        // 4. GUARDAR EL NUEVO USUARIO EN LA BASE DE DATOS (Persistencia)
        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        // 5. GENERAR EL TOKEN JWT (Logueo Automático)
        // Tras el registro, el usuario se loguea automáticamente y recibe su token.
        String jwtToken = jwtService.generateToken(usuarioGuardado);

        // 6. RETORNO DE LA RESPUESTA
        return new AuthResponse(jwtToken, usuarioGuardado.getId());
    }


}