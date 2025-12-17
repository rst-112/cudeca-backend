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
import com.cudeca.model.usuario.VerificacionCuenta;
import com.cudeca.repository.*;
import com.cudeca.service.AuthService;
import com.cudeca.service.EmailService;
import com.cudeca.service.JwtService;
import com.cudeca.service.impl.ServiceExceptions.EmailAlreadyExistsException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
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

        // --- NUEVAS DEPENDENCIAS PARA MONEDERO Y FUSIÓN DE CUENTAS (FEATURE) ---
        private final MonederoRepository monederoRepository;
        private final InvitadoRepository invitadoRepository;
        private final CompraRepository compraRepository;

        // --- DEPENDENCIAS DE RECUPERACIÓN DE CONTRASEÑA (DEVELOP) ---
        private final VerificacionCuentaRepository verificacionRepository;
        private final EmailService emailService;

        @Value("${application.frontend.url:http://localhost:5173}")
        private String frontendUrl;

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

        // --- FUNCIONALIDAD DE RECUPERACIÓN DE CONTRASEÑA (DEVELOP) ---

        @Override
        @Transactional
        public void solicitarRecuperacionPassword(String email) {
                // Por seguridad, no lanzamos error si el usuario no existe para evitar
                // enumeración
                usuarioRepository.findByEmail(email).ifPresent(usuario -> {

                        // 1. Invalidar tokens anteriores pendientes
                        verificacionRepository.anularTokensPrevios(usuario.getId());

                        // 2. Generar token
                        String token = UUID.randomUUID().toString();

                        // 3. Guardar verificación
                        VerificacionCuenta verificacion = VerificacionCuenta.builder()
                                        .usuario(usuario)
                                        .email(email)
                                        .token(token)
                                        .expiraEn(OffsetDateTime.now().plusHours(1))
                                        .usado(false)
                                        .build();

                        verificacionRepository.save(verificacion);

                        // 4. Enviar Email
                        String htmlContent = getContent(usuario, token);

                        emailService.enviarCorreoHtml(email, "Restablecer Contraseña - CUDECA", htmlContent);
                        log.info("Email de recuperación enviado a: {}", email);
                });
        }

        private @NonNull String getContent(Usuario usuario, String token) {
                String link = frontendUrl + "/reset-password?token=" + token;
                return String.format(
                                "<div style=\"font-family: Arial, sans-serif; color: #333;\">%%n" +
                                                "<h2 style=\"color: #00A651;\">Recuperación de Contraseña</h2>%%n" +
                                                "<p>Hola %s,</p>%%n" +
                                                "<p>Hemos recibido una solicitud para restablecer tu contraseña en CUDECA.</p>%%n"
                                                +
                                                "<p>Haz clic en el siguiente botón para crear una nueva contraseña:</p>%%n"
                                                +
                                                "<a href=\"%s\" style=\"background-color: #00A651; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 20px 0;\">Restablecer Contraseña</a>%%n"
                                                +
                                                "<p style=\"font-size: 12px; color: #666;\">Este enlace expirará en 1 hora. Si no has solicitado esto, puedes ignorar este correo.</p>%%n"
                                                +
                                                "</div>",
                                usuario.getNombre(), link);
        }

        @Override
        @Transactional
        public void restablecerPassword(String token, String nuevaPassword) {
                // 1. Buscar y Validar Token
                VerificacionCuenta verificacion = verificacionRepository.findByToken(token)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "El enlace de recuperación no es válido."));

                if (verificacion.isUsado()) {
                        throw new IllegalArgumentException("Este enlace ya ha sido utilizado.");
                }

                if (verificacion.getExpiraEn().isBefore(OffsetDateTime.now())) {
                        throw new IllegalArgumentException("El enlace ha caducado. Solicita uno nuevo.");
                }

                // 2. Actualizar Usuario
                Usuario usuario = verificacion.getUsuario();
                if (usuario == null) {
                        throw new IllegalStateException("Error de integridad: Token sin usuario asociado.");
                }

                usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
                usuarioRepository.save(usuario);

                // 3. Marcar token como usado
                verificacion.setUsado(true);
                verificacionRepository.save(verificacion);

                log.info("Contraseña restablecida exitosamente para usuario ID: {}", usuario.getId());
        }
}
