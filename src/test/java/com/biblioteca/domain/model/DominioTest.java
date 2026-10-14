package com.biblioteca.domain.model;

import com.biblioteca.domain.port.ServicioCalendario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias del Modelo de Dominio")
class DominioTest {

    @Test
    @DisplayName("Polimorfismo: Estudiante tiene límite de 3 préstamos y Docente de 5")
    void testLimitesPrestamosPolimorficos() {
        Usuario estudiante = new Estudiante("E1", "Ana García", "Sistemas", 3);
        Usuario docente = new Docente("D1", "Carlos Mendoza", "Física", "TIEMPO_COMPLETO");

        assertEquals(3, estudiante.getLimitePrestamos());
        assertEquals(5, docente.getLimitePrestamos());
    }

    @Test
    @DisplayName("Ejemplar: transiciones de estado y disponibilidad")
    void testTransicionesEstadoEjemplar() {
        Ejemplar ejemplar = Ejemplar.builder()
                .codigoBarras("CB-100")
                .estado(Ejemplar.ESTADO_DISPONIBLE)
                .build();

        assertTrue(ejemplar.isDisponible());

        ejemplar.actualizarEstado(Ejemplar.ESTADO_PRESTADO);
        assertEquals(Ejemplar.ESTADO_PRESTADO, ejemplar.getEstado());
        assertFalse(ejemplar.isDisponible());

        ejemplar.actualizarEstado(Ejemplar.ESTADO_DISPONIBLE);
        assertEquals(Ejemplar.ESTADO_DISPONIBLE, ejemplar.getEstado());
        assertTrue(ejemplar.isDisponible());

        assertThrows(IllegalArgumentException.class, () -> ejemplar.actualizarEstado("ESTADO_INVALIDO"));
    }

    @Test
    @DisplayName("Libro: disponibilidad según ejemplares y primer ejemplar disponible")
    void testDisponibilidadLibro() {
        Ejemplar e1 = Ejemplar.builder().codigoBarras("CB-1").estado(Ejemplar.ESTADO_PRESTADO).build();
        Ejemplar e2 = Ejemplar.builder().codigoBarras("CB-2").estado(Ejemplar.ESTADO_DISPONIBLE).build();

        Libro libro = Libro.builder()
                .isbn("ISBN-001")
                .titulo("Clean Architecture")
                .ejemplares(List.of(e1, e2))
                .build();

        assertTrue(libro.estaDisponible());
        assertNotNull(libro.getPrimerEjemplarDisponible());
        assertEquals("CB-2", libro.getPrimerEjemplarDisponible().getCodigoBarras());

        e2.actualizarEstado(Ejemplar.ESTADO_PRESTADO);
        assertFalse(libro.estaDisponible());
        assertNull(libro.getPrimerEjemplarDisponible());
    }

    @Test
    @DisplayName("Prestamo: cálculo de multa con días hábiles laborales")
    void testCalculoMultaConDiasHabiles() {
        // Mock simple de ServicioCalendario: considera todos los días hábiles
        ServicioCalendario servicioCalendarioMock = fecha -> true;

        Calendar calDevolucion = Calendar.getInstance();
        calDevolucion.set(2026, Calendar.OCTOBER, 1); // 1 de Octubre (fecha límite pactada)
        Date fechaDevolucion = calDevolucion.getTime();

        Calendar calEntrega = Calendar.getInstance();
        calEntrega.set(2026, Calendar.OCTOBER, 4); // 4 de Octubre (devuelto 3 días después: 2, 3 y 4 de Oct)
        Date fechaEntrega = calEntrega.getTime();

        Ejemplar ejemplar = Ejemplar.builder().codigoBarras("CB-1").estado(Ejemplar.ESTADO_PRESTADO).build();

        Prestamo prestamo = Prestamo.builder()
                .idPrestamo("P-1")
                .fechaDevolucion(fechaDevolucion)
                .fechaEntrega(fechaEntrega)
                .ejemplares(List.of(ejemplar))
                .build();

        // 3 días hábiles * $2,500 COP * 1 ejemplar = $7,500 COP
        double multa = prestamo.calcularMulta(servicioCalendarioMock);
        assertEquals(3 * Prestamo.TARIFA_MULTA_POR_DIA_HABIL, multa);
        assertTrue(prestamo.tieneRetraso());
    }

    @Test
    @DisplayName("Prestamo: sin retraso ni multa si se entrega en la fecha pactada o antes")
    void testCalculoMultaSinRetraso() {
        ServicioCalendario servicioCalendarioMock = fecha -> true;

        Calendar calDevolucion = Calendar.getInstance();
        calDevolucion.set(2026, Calendar.OCTOBER, 10);
        Date fechaDevolucion = calDevolucion.getTime();

        Calendar calEntrega = Calendar.getInstance();
        calEntrega.set(2026, Calendar.OCTOBER, 8); // Entregado antes de la fecha límite
        Date fechaEntrega = calEntrega.getTime();

        Prestamo prestamo = Prestamo.builder()
                .idPrestamo("P-2")
                .fechaDevolucion(fechaDevolucion)
                .fechaEntrega(fechaEntrega)
                .build();

        double multa = prestamo.calcularMulta(servicioCalendarioMock);
        assertEquals(0.0, multa);
        assertFalse(prestamo.tieneRetraso());
    }
}
