package com.cudeca.service.impl;

import com.cudeca.dto.usuario.AuthResponse;
import com.cudeca.dto.usuario.LoginRequest;
import com.cudeca.dto.usuario.RegisterRequest;
import com.cudeca.model.usuario.Usuario;
import com.cudeca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de la lógica de negocio para la autenticación y registro de usuarios.
 * Orquesta la seguridad (PasswordEncoder, AuthenticationManager) y la generación de tokens (JwtService).
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtServiceImpl jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registra un nuevo usuario en el sistema.
     * Cifra la contraseña y genera un token JWT inicial.
     *
     * @param request DTO con los datos del registro (nombre, email, password).
     * @return AuthResponse con el token JWT generado.
     */
    public AuthResponse register(RegisterRequest request) {

        // 1. Creación de la entidad Usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setEmail(request.getEmail());

        // 2. Seguridad: Cifrado de contraseña
        // IMPORTANTE: Nunca guardamos la contraseña en texto plano.
        // Usamos BCrypt para generar el hash que se guardará en la columna 'password_hash'.
        nuevoUsuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // 3. Persistencia
        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        // 4. Generación del Token (Auto-Login)
        // Permitimos que el usuario entre directamente tras registrarse sin tener que loguearse de nuevo.
        String jwtToken = jwtService.generateToken(usuarioGuardado);

        return new AuthResponse(jwtToken);
    }

    /**
     * Autentica a un usuario existente.
     *
     * @param request DTO con las credenciales (email, password).
     * @return AuthResponse con el token JWT si las credenciales son válidas.
     */
    public AuthResponse login(LoginRequest request) {
        // 1. Delegamos la autenticación a Spring Security
        // Si la contraseña no coincide o el usuario no existe, este método lanzará una excepción (BadCredentialsException).
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. Recuperamos el usuario completo de la BD
        // Necesario para inyectar datos adicionales (como el ID o Roles) en el Token.
        Usuario user = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado tras autenticación: " + request.getEmail()));

        // 3. Generamos el token firmado
        String jwtToken = jwtService.generateToken(user);

        return new AuthResponse(jwtToken);
    }
}