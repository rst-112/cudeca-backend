package com.cudeca.service;

import com.cudeca.dto.usuario.AuthResponse;
import com.cudeca.dto.usuario.LoginRequest;
import com.cudeca.dto.usuario.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void solicitarRecuperacionPassword(String email);

    void restablecerPassword(String token, String nuevaPassword);
}
