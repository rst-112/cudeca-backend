package com.cudeca.service.impl;

import com.cudeca.dto.usuario.AuthResponse;
import com.cudeca.dto.usuario.LoginRequest;
import com.cudeca.dto.usuario.RegisterRequest;
import com.cudeca.dto.usuario.UserResponse;
import com.cudeca.model.negocio.Compra;
import com.cudeca.model.negocio.Monedero;
import com.cudeca.model.usuario.Invitado;
import com.cudeca.model.usuario.Rol;
import com.cudeca.model.usuario.Usuario;
import com.cudeca.repository.*;
import com.cudeca.service.AuthService;
import com.cudeca.service.JwtService;
import com.cudeca.service.impl.ServiceExceptions.EmailAlreadyExistsException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio encargado de la lógica de negocio para la autenticación y registro
 * de usuarios.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

        private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

        private final UsuarioRepository usuarioRepository;
        private final RolRepository rolRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;

        // --- NUEVAS DEPENDENCIAS PARA MONEDERO Y FUSIÓN DE CUENTAS ---
        private final MonederoRepository monederoRepository;
        private final InvitadoRepository invitadoRepository;
        private final CompraRepository compraRepository;

        @Override
        @Transactional
        public AuthResponse register(RegisterRequest request) {
                log.info("Iniciando registro para email: {}", request.getEmail());

                // 1. Validar email
                if (usuarioRepository.existsByEmail(request.getEmail())) {
                        throw new EmailAlreadyExistsException(request.getEmail());
                }

                Rol rolDefault = rolRepository.findByNombreIgnoreCase("COMPRADOR")
                                .orElseThrow(() -> new RuntimeException(
                                                "Error: El rol 'COMPRADOR' no está configurado en la base de datos."));

                // 2. Crear el usuario
                Usuario nuevoUsuario = Usuario.builder()
                                .nombre(request.getNombre())
                                .email(request.getEmail())
                                .passwordHash(passwordEncoder.encode(request.getPassword()))
                                .roles(new HashSet<>(Collections.singletonList(rolDefault)))
                                .build();

                // 3. Guardar Usuario
                Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

                // 4. Crear Monedero (NUEVA FUNCIONALIDAD)
                crearMonederoInicial(usuarioGuardado);

                // 5. Reclamar cuenta de Invitado (NUEVA FUNCIONALIDAD)
                migrarDatosDeInvitado(usuarioGuardado);

                // 6. Generar token
                String jwtToken = jwtService.generateToken(usuarioGuardado);

                return buildAuthResponse(usuarioGuardado, jwtToken);
        }

        @Override
        public AuthResponse login(LoginRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

                Usuario user = usuarioRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

                String jwtToken = jwtService.generateToken(user);
                return buildAuthResponse(user, jwtToken);
        }

        // --- MÉTODOS PRIVADOS ---

        /**
         * Crea un monedero con saldo 0 para el nuevo usuario.
         */
        private void crearMonederoInicial(Usuario usuario) {
                Monedero monedero = Monedero.builder()
                                .usuario(usuario)
                                .saldo(BigDecimal.ZERO)
                                .build();

                monederoRepository.save(monedero);
                log.info("Monedero creado para el usuario ID: {}", usuario.getId());
        }

        /**
         * Busca si existe un Invitado con el mismo email.
         * Si existe, mueve sus compras al nuevo Usuario y borra al Invitado.
         */
        private void migrarDatosDeInvitado(Usuario usuario) {
                Optional<Invitado> invitadoOpt = invitadoRepository.findByEmail(usuario.getEmail());

                if (invitadoOpt.isPresent()) {
                        Invitado invitado = invitadoOpt.get();
                        log.info("Invitado encontrado con email {}. Iniciando migración de datos...",
                                        usuario.getEmail());

                        // Al estar en una transacción (@Transactional), podemos acceder a la lista lazy
                        List<Compra> comprasDelInvitado = invitado.getCompras();

                        if (comprasDelInvitado != null && !comprasDelInvitado.isEmpty()) {
                                for (Compra compra : comprasDelInvitado) {
                                        compra.setInvitado(null);
                                        compra.setUsuario(usuario);
                                }
                                // Guardamos los cambios en las compras
                                compraRepository.saveAll(comprasDelInvitado);
                                log.info("Se han migrado {} compras del invitado al usuario ID: {}",
                                                comprasDelInvitado.size(), usuario.getId());
                        }

                        // 3. Borrar al invitado para limpiar la base de datos
                        java.util.Optional.ofNullable(invitado.getCompras())
                                        .ifPresent(java.util.List::clear);

                        invitadoRepository.delete(invitado);
                        log.info("Registro de invitado eliminado correctamente tras la migración.");
                }
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
                                .isAdmin(user.esAdmin())
                                .build();
        }
}
