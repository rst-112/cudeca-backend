package com.cudeca.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact; // Añadido para mejor documentación
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la documentación OpenAPI (Swagger).
 * Define la estructura base de la API y el esquema de seguridad JWT.
 */
@Configuration
@OpenAPIDefinition(
        // 1. Información General del Proyecto
        info = @Info(
                title = "API Backend CUDECA",
                version = "1.0",
                description = "Documentación de endpoints para la gestión de eventos y donaciones."
                // Contacto del equipo de Backend (B4)
                // contact = @Contact(name = "Equipo Backend (B4)", email = "tu.email@ejemplo.com")
        ),
        // 2. Seguridad Global: Aplica el esquema 'bearerAuth' a todos los endpoints por defecto.
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
// 3. Definición del Esquema de Seguridad (Aparece como botón 'Authorize' en Swagger)
@SecurityScheme(
        name = "bearerAuth", // Nombre de referencia interna para el @SecurityRequirement
        description = "JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\"",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP, // Tipo de seguridad: HTTP (para cabeceras)
        bearerFormat = "JWT",          // Formato del token
        in = SecuritySchemeIn.HEADER   // Dónde buscar el token (en la cabecera 'Authorization')
)
public class OpenApiConfig {
    // Clase vacía: La configuración se maneja enteramente mediante anotaciones.
}