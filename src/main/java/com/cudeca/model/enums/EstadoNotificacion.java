package com.cudeca.model.enums;

public enum EstadoNotificacion {
    PENDIENTE, // Está en cola para enviarse
    ENVIADA,   // Se envió correctamente al servidor de correo
    ERROR      // Falló el envío (quizás el email no existe)
}
