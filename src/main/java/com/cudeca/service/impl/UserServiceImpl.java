package com.cudeca.service.impl;

import com.cudeca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Servicio fundamental de Spring Security.
 * Actúa como un puente entre la capa de seguridad y la base de datos,
 * permitiendo al sistema cargar los detalles del usuario durante el login.
 * <p>
 * Implementa la interfaz estándar 'UserDetailsService'.
 */
@Service
@RequiredArgsConstructor // Genera el constructor para UsuarioRepository
public class UserServiceImpl implements com.cudeca.service.UserService {

    // Inyección del repositorio de datos para acceder a la tabla 'usuarios'.
    // Esto es el punto de conexión con la persistencia (JpaRepository).
    private final UsuarioRepository usuarioRepository;

    /**
     * Localiza un usuario basado en el username (email).
     * Este método es llamado automáticamente por el AuthenticationManager durante el proceso de login.
     *
     * @param email El email proporcionado por el usuario en el intento de login.
     * @return UserDetails (La entidad Usuario) con los datos necesarios para la autenticación.
     * @throws UsernameNotFoundException Si el usuario no existe en la base de datos.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Usamos el método findByEmail de tu repositorio.
        return usuarioRepository.findByEmail(email)
                // Si el Optional está vacío, lanzamos la excepción específica que Spring espera.
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
    }
}