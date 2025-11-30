package com.cudeca.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio encargado de la generación, firma y validación de tokens JWT (JSON Web Tokens).
 * Lee la clave secreta y el tiempo de expiración desde application.yml.
 */
@Service
public class JwtServiceImpl {

    // Se inyecta la clave secreta (Base64) desde la configuración de la aplicación.
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    // Se inyecta el tiempo de validez del token en milisegundos.
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Genera un token JWT para un usuario con claims estándar.
     * Es el método de acceso principal para generar tokens.
     *
     * @param userDetails Los detalles del usuario (email/username, roles, etc.).
     * @return El token JWT firmado como String.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Genera un token JWT, permitiendo añadir claims personalizados (como el userId o roles).
     *
     * @param extraClaims Claims adicionales a incluir en el payload.
     * @param userDetails Detalles del usuario.
     * @return El token JWT firmado.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // Claim 'sub' (Sujeto): Usamos el email del usuario.
                .setIssuedAt(new Date(System.currentTimeMillis())) // Claim 'iat' (Emitido en)
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Claim 'exp' (Expiración)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Firma con la clave secreta (HS256)
                .compact();
    }

    /**
     * Valida un token comprobando su firma y si el username coincide con los UserDetails.
     *
     * @param token El token JWT a validar.
     * @param userDetails Los detalles del usuario (cargados desde UserService).
     * @return true si el token es válido y no ha expirado.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // ------------------------------------------------------------------------
    // MÉTODOS DE EXTRACCIÓN
    // ------------------------------------------------------------------------

    /**
     * Extrae el username (Subject) del payload del token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Método genérico para extraer cualquier claim (ej. "roles", "userId")
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // Parsear el token usando la clave de firma secreta.
        return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
    }

    // ------------------------------------------------------------------------
    // MÉTODO DE SEGURIDAD
    // ------------------------------------------------------------------------

    /**
     * Convierte la clave secreta de Base64 (String) a un objeto Key (requerido por JJWT).
     */
    private Key getSignInKey() {
        // Decodificamos la clave Base64 que viene del application.yml
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}