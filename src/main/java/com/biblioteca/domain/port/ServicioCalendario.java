package com.biblioteca.domain.port;

import java.util.Date;

/**
 * Puerto de abstracción para la consulta del calendario laboral.
 *
 * <p>Permite que el cálculo de multas del dominio consulte si una fecha
 * es hábil o no, sin acoplarse a ninguna implementación concreta
 * (calendarios de festivos, distintas zonas horarias, APIs externas, etc.).</p>
 *
 * <p>Aplica el Principio de Inversión de Dependencias (DIP).</p>
 */
public interface ServicioCalendario {

    /**
     * Determina si la fecha proporcionada es un día hábil laboral.
     *
     * @param fecha la fecha a evaluar.
     * @return {@code true} si es día hábil (generalmente lunes a viernes, sin festivos);
     *         {@code false} si es fin de semana o festivo.
     */
    boolean esDiaHabil(Date fecha);
}
