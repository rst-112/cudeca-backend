package com.cudeca.service.impl;

import com.cudeca.model.security.Usuario;
import com.cudeca.service.JwtService;
// Importaciones de librería JWT
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
// Otras importaciones necesarias
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;

@Service
public class JwtServiceImpl implements JwtService {

    // 1. Clave Secreta: DEBE SER ÚNICA Y SECRETA (Se lee del application.yml)
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    // Tiempo de vida del token (ej. 1 día)
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    // =========================================================================

    @Override
    public String extractUsername(String token) {
        // Usa una función utilitaria para obtener el subject (email) del token
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public String generateToken(Usuario usuario) {
        // Mapea los datos del usuario (roles y ID) en el token (Payload)
        // Esto es crucial para la Autorización (RBAC)
        Claims claims = Jwts.claims()
                .setSubject(usuario.getEmail());

        // Añadir el ID del usuario y los roles como custom claims
        claims.put("userId", usuario.getId());
        claims.put("roles", usuario.getAuthorities()); // Obtener roles/permisos

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new java.util.Date(System.currentTimeMillis()))
                .setExpiration(new java.util.Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Firma el token con la clave secreta
                .compact();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // El token es válido si el email coincide con el UserDetails y no ha expirado
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // =========================================================================
    // Funciones Utilitarias (Necesarias para que el servicio funcione)

    private <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new java.util.Date());
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}