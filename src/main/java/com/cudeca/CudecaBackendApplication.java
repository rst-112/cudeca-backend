package com.cudeca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Clase principal del backend de CUDECA.
 * Inicia la aplicación Spring Boot.
 */
@SpringBootApplication
@EnableAsync
@SuppressWarnings("PMD.UseUtilityClass")
public class CudecaBackendApplication {

    private static final Logger logger = LoggerFactory.getLogger(CudecaBackendApplication.class);

    public static void main(final String[] args) {
        logger.info("Iniciando backend CUDECA...");
        SpringApplication.run(CudecaBackendApplication.class, args);
        logger.info("Aplicación CUDECA iniciada correctamente en puerto 8080");
    }
}
