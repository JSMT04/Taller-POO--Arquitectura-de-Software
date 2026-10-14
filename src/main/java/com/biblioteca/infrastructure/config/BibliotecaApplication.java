package com.biblioteca.infrastructure.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal del Sistema de Gestión de Préstamos de Biblioteca.
 *
 * <p>La anotación {@code @SpringBootApplication} habilita:
 * <ul>
 *   <li>Auto-configuración de Spring Boot.</li>
 *   <li>Escaneo de componentes en el paquete {@code com.biblioteca} y sub-paquetes.</li>
 * </ul>
 */
@SpringBootApplication(scanBasePackages = "com.biblioteca")
public class BibliotecaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BibliotecaApplication.class, args);
    }
}
