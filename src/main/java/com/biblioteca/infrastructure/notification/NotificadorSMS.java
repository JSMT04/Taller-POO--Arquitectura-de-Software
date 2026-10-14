package com.biblioteca.infrastructure.notification;

import com.biblioteca.domain.model.Usuario;
import com.biblioteca.domain.port.NotificadorMulta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementación de {@link NotificadorMulta} que simula el envío de un mensaje
 * de texto SMS imprimiendo en consola.
 *
 * <p>Demuestra el Principio Abierto/Cerrado (OCP): agregar este nuevo canal no
 * requirió modificar ningún código existente en las capas de dominio o aplicación.</p>
 */
@Component
public class NotificadorSMS implements NotificadorMulta {

    private static final Logger log = LoggerFactory.getLogger(NotificadorSMS.class);

    @Override
    public void enviarNotificacion(String mensaje, Usuario usuario) {
        System.out.println();
        System.out.println("┌──────────────────────────────────────────────────────────────────┐");
        System.out.printf ("│  [SMS] Enviando mensaje a: %-39s│%n", usuario.getNombre());
        System.out.println("├──────────────────────────────────────────────────────────────────┤");
        // SMS: mensaje corto truncado a 160 caracteres (límite real de SMS)
        String mensajeCorto = mensaje.length() > 160 ? mensaje.substring(0, 157) + "..." : mensaje;
        System.out.println("│  " + mensajeCorto);
        System.out.println("└──────────────────────────────────────────────────────────────────┘");
        log.debug("SMS enviado a: {}", usuario.getNombre());
    }
}
