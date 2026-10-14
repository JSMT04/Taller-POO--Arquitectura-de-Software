package com.biblioteca.infrastructure.calendar;

import com.biblioteca.domain.port.ServicioCalendario;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

/**
 * Implementación de {@link ServicioCalendario} que considera como días hábiles
 * todos los días de lunes a viernes, excluyendo sábados y domingos.
 *
 * <p>Esta es una implementación "mock" o básica. En un entorno productivo, podría
 * extenderse para consultar calendarios de festivos oficiales (APIs del gobierno,
 * bases de datos de festivos, etc.) sin modificar el dominio ni la aplicación.</p>
 */
@Component
public class ServicioCalendarioHabil implements ServicioCalendario {

    /**
     * Determina si la fecha proporcionada es un día hábil (lunes a viernes).
     *
     * @param fecha la fecha a evaluar.
     * @return {@code true} si la fecha es lunes, martes, miércoles, jueves o viernes;
     *         {@code false} si es sábado o domingo.
     */
    @Override
    public boolean esDiaHabil(Date fecha) {
        if (fecha == null) return false;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        int diaSemana = calendar.get(Calendar.DAY_OF_WEEK);

        // Calendar.SATURDAY = 7, Calendar.SUNDAY = 1
        return diaSemana != Calendar.SATURDAY && diaSemana != Calendar.SUNDAY;
    }
}
