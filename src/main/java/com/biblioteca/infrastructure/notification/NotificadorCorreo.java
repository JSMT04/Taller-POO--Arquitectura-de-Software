package com.biblioteca.infrastructure.notification;

import com.biblioteca.domain.model.Usuario;
import com.biblioteca.domain.port.NotificadorMulta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementación de {@link NotificadorMulta} que simula el envío de una notificación
 * por correo electrónico imprimiendo el mensaje en consola.
 *
 * <p>Aplica el Principio Abierto/Cerrado (OCP): esta clase extiende el sistema
 * de notificaciones sin modificar la capa de aplicación.</p>
 */
@Component
public class NotificadorCorreo implements NotificadorMulta {

    private static final Logger log = LoggerFactory.getLogger(NotificadorCorreo.class);

    @Override
    public void enviarNotificacion(String mensaje, Usuario usuario) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.printf ("║  [EMAIL] Enviando notificación a: %-30s  ║%n", usuario.getNombre());
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.println("║  Para:    " + usuario.getNombre());
        System.out.println("║  Asunto:  Notificación de Multa - Biblioteca Universitaria");
        System.out.println("║");
        // Dividir el mensaje en líneas de max 62 caracteres
        String[] palabras = mensaje.split(" ");
        StringBuilder linea = new StringBuilder("║  ");
        for (String palabra : palabras) {
            if (linea.length() + palabra.length() > 65) {
                System.out.println(linea);
                linea = new StringBuilder("║  ");
            }
            linea.append(palabra).append(" ");
        }
        if (linea.length() > 3) System.out.println(linea);
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        log.debug("EMAIL enviado a: {}", usuario.getNombre());
    }
}
