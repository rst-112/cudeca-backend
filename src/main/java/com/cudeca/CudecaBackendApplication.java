package com.cudeca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del backend de CUDECA.
 * Inicia la aplicación Spring Boot.
 */
@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass")
public class CudecaBackendApplication {
    public static void main(final String[] args) {
        SpringApplication.run(CudecaBackendApplication.class, args);
    }
}
