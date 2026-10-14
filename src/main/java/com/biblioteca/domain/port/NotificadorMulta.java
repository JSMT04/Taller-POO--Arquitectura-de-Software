package com.biblioteca.domain.port;

import com.biblioteca.domain.model.Usuario;

/**
 * Puerto de abstracción para el envío de notificaciones de multa a usuarios.
 *
 * <p>Aplica el Principio Abierto/Cerrado (OCP): nuevos canales de notificación
 * (Email, SMS, Push, WhatsApp, etc.) pueden agregarse implementando esta interfaz
 * sin modificar la capa de aplicación.</p>
 *
 * <p>Aplica el Principio de Inversión de Dependencias (DIP): la capa de aplicación
 * depende de esta abstracción, no de implementaciones concretas de infraestructura.</p>
 */
public interface NotificadorMulta {

    /**
     * Envía una notificación al usuario indicado con el mensaje de multa.
     *
     * @param mensaje el texto de la notificación a enviar.
     * @param usuario el usuario destinatario de la notificación.
     */
    void enviarNotificacion(String mensaje, Usuario usuario);
}
