package com.cudeca.service.impl;

import com.cudeca.dto.usuario.AuthResponse;
import com.cudeca.dto.usuario.LoginRequest;
import com.cudeca.dto.usuario.RegisterRequest;
import com.cudeca.dto.usuario.UserResponse;
import com.cudeca.model.usuario.Rol;
import com.cudeca.model.usuario.Usuario;
import com.cudeca.repository.RolRepository;
import com.cudeca.repository.UsuarioRepository;
import com.cudeca.service.AuthService;
import com.cudeca.service.JwtService;
import com.cudeca.service.impl.ServiceExceptions.EmailAlreadyExistsException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Servicio encargado de la lógica de negocio para la autenticación y registro de usuarios.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Validar email
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        Rol rolDefault = rolRepository.findByNombreIgnoreCase("COMPRADOR")
                .orElseThrow(() -> new RuntimeException("Error: El rol 'COMPRADOR' no está configurado en la base de datos."));

        // 3. Crear el usuario
        Usuario nuevoUsuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(new HashSet<>(Collections.singletonList(rolDefault))) // Asignamos el objeto Rol encontrado
                .build();

        // 4. Guardar y generar token
        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
        String jwtToken = jwtService.generateToken(usuarioGuardado);

        return buildAuthResponse(usuarioGuardado, jwtToken);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Usuario user = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String jwtToken = jwtService.generateToken(user);
        return buildAuthResponse(user, jwtToken);
    }

    private AuthResponse buildAuthResponse(Usuario user, String token) {
        return AuthResponse.builder()
                .token(token)
                .user(UserResponse.builder()
                        .id(user.getId())
                        .nombre(user.getNombre())
                        .email(user.getEmail())
                        .rol(user.getRoles().stream()
                                .map(Rol::getNombre)
                                .collect(Collectors.joining(",")))
                        .build())
                .build();
    }
}