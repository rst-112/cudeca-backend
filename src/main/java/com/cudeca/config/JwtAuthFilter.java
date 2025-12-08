package com.cudeca.config;

import com.cudeca.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de seguridad que se ejecuta una vez por cada petición HTTP.
 * Es responsable de interceptar el token JWT de la cabecera 'Authorization',
 * validarlo y autenticar al usuario dentro del contexto de seguridad de Spring.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    // Servicio para la lógica de token (generar/validar/extraer claims)
    private final JwtService jwtService;

    // Servicio para cargar los detalles del usuario desde la base de datos
    private final UserDetailsService userDetailsService;

    /**
     * Lógica principal del filtro ejecutada en cada petición.
     * * @param request La petición HTTP entrante.
     *
     * @param response    La respuesta HTTP saliente.
     * @param filterChain La cadena de filtros para continuar la ejecución.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Verificar el Token: Si no hay token o no tiene el formato "Bearer ",
        // se permite el paso al siguiente filtro sin autenticar.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extracción: Quitar "Bearer " para obtener el token puro
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);

        // 3. Autenticación: Si el email existe Y el usuario no está ya autenticado en el contexto
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Cargar usuario desde la DB (usando el email extraído)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 4. Validación: Verificar que el token es auténtico y no ha expirado
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Crear el objeto de autenticación de Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // La contraseña ya no es necesaria aquí
                        userDetails.getAuthorities() // Los roles/permisos
                );

                // Añadir detalles de la petición (IP, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 5. Establecer la Sesión (Stateless): Marcar al usuario como autenticado para esta petición
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continuar con el siguiente filtro en la cadena (Autorización, Controllers, etc.)
        filterChain.doFilter(request, response);
    }
}