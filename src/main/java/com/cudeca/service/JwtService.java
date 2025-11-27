package com.cudeca.service;

import com.cudeca.model.security.Usuario; // La entidad de B3
import java.security.Key;
import org.springframework.security.core.userdetails.UserDetails;

// La interfaz podría ser así:
public interface JwtService {

    String extractUsername(String token);

    String generateToken(Usuario usuario);

    boolean isTokenValid(String token, UserDetails userDetails);
}