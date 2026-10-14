package com.biblioteca.application.service;

import com.biblioteca.domain.exception.LibroDisponibleException;
import com.biblioteca.domain.exception.LimitePrestamosSuperadoException;
import com.biblioteca.domain.model.*;
import com.biblioteca.domain.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias de Servicios de Aplicación")
class ServiciosApplicationTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ServicioCalendario servicioCalendario;

    @Mock
    private NotificadorMulta notificadorMock;

    private ServicioPrestamo servicioPrestamo;
    private ServicioReserva servicioReserva;

    @BeforeEach
    void setUp() {
        servicioPrestamo = new ServicioPrestamo(
                usuarioRepository,
                libroRepository,
                prestamoRepository,
                servicioCalendario,
                List.of(notificadorMock)
        );
        servicioReserva = new ServicioReserva(
                usuarioRepository,
                libroRepository,
                reservaRepository
        );
    }

    @Test
    @DisplayName("Crear préstamo: falla si supera el límite de préstamos del estudiante")
    void testCrearPrestamoSuperaLimiteEstudiante() {
        Estudiante estudiante = new Estudiante("U1", "Ana", "Sistemas", 4);
        when(usuarioRepository.findById("U1")).thenReturn(Optional.of(estudiante));

        // Simula 2 ejemplares activos en un préstamo anterior
        Ejemplar eActivo1 = Ejemplar.builder().codigoBarras("CB-A1").estado(Ejemplar.ESTADO_PRESTADO).build();
        Ejemplar eActivo2 = Ejemplar.builder().codigoBarras("CB-A2").estado(Ejemplar.ESTADO_PRESTADO).build();
        Prestamo prestamoPrevio = Prestamo.builder()
                .idPrestamo("P-PREV")
                .ejemplares(List.of(eActivo1, eActivo2))
                .build();
        when(prestamoRepository.findActivosByUsuarioId("U1")).thenReturn(List.of(prestamoPrevio));

        // Intenta pedir 2 ejemplares adicionales (2 activos + 2 solicitados = 4 > límite 3 de Estudiante)
        List<String> codigos = List.of("CB-1", "CB-2");

        assertThrows(LimitePrestamosSuperadoException.class, () ->
                servicioPrestamo.crearPrestamo("U1", codigos, new Date())
        );
    }

    @Test
    @DisplayName("Crear préstamo: falla si algún ejemplar ya está prestado")
    void testCrearPrestamoEjemplarNoDisponible() {
        Docente docente = new Docente("U2", "Carlos", "Matemáticas", "TIEMPO_COMPLETO");
        Ejemplar ejNoDisp = Ejemplar.builder().codigoBarras("CB-1").estado(Ejemplar.ESTADO_PRESTADO).build();

        when(usuarioRepository.findById("U2")).thenReturn(Optional.of(docente));
        when(prestamoRepository.findActivosByUsuarioId("U2")).thenReturn(Collections.emptyList());
        when(libroRepository.findEjemplarByCodigoBarras("CB-1")).thenReturn(Optional.of(ejNoDisp));

        assertThrows(IllegalStateException.class, () ->
                servicioPrestamo.crearPrestamo("U2", List.of("CB-1"), new Date())
        );
    }

    @Test
    @DisplayName("Crear reserva: rechaza si el libro tiene ejemplares disponibles")
    void testCrearReservaFallaSiLibroDisponible() {
        Usuario estudiante = new Estudiante("U1", "Ana", "Sistemas", 4);
        Ejemplar ejemplar = Ejemplar.builder().codigoBarras("CB-1").estado(Ejemplar.ESTADO_DISPONIBLE).build();
        Libro libro = Libro.builder().isbn("ISBN-1").titulo("Libro A").ejemplares(List.of(ejemplar)).build();

        when(usuarioRepository.findById("U1")).thenReturn(Optional.of(estudiante));
        when(libroRepository.findByIsbn("ISBN-1")).thenReturn(Optional.of(libro));

        assertThrows(LibroDisponibleException.class, () ->
                servicioReserva.crearReserva("U1", "ISBN-1")
        );
    }

    @Test
    @DisplayName("Crear reserva: exitosa si todos los ejemplares están prestados")
    void testCrearReservaExitosa() {
        Usuario estudiante = new Estudiante("U1", "Ana", "Sistemas", 4);
        Ejemplar ejPrestado = Ejemplar.builder().codigoBarras("CB-1").estado(Ejemplar.ESTADO_PRESTADO).build();
        Libro libro = Libro.builder().isbn("ISBN-1").titulo("Libro A").ejemplares(List.of(ejPrestado)).build();

        when(usuarioRepository.findById("U1")).thenReturn(Optional.of(estudiante));
        when(libroRepository.findByIsbn("ISBN-1")).thenReturn(Optional.of(libro));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));

        Reserva reserva = servicioReserva.crearReserva("U1", "ISBN-1");

        assertNotNull(reserva);
        assertEquals("PENDIENTE", reserva.getEstado());
        assertEquals("Ana", reserva.getUsuario().getNombre());
    }

    @Test
    @DisplayName("Devolución: calcula multa con retraso y notifica")
    void testProcesarDevolucionConMulta() {
        Docente docente = new Docente("U2", "Carlos", "Matemáticas", "TIEMPO_COMPLETO");
        Ejemplar ejemplar = Ejemplar.builder().codigoBarras("CB-1").estado(Ejemplar.ESTADO_PRESTADO).build();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -5);
        Date fechaVencida = cal.getTime();

        Prestamo prestamo = Prestamo.builder()
                .idPrestamo("P-100")
                .usuario(docente)
                .ejemplares(new ArrayList<>(List.of(ejemplar)))
                .fechaDevolucion(fechaVencida)
                .estado("ACTIVO")
                .build();

        when(prestamoRepository.findById("P-100")).thenReturn(Optional.of(prestamo));
        // Hace que esDiaHabil retorne true para generar multa por mora
        when(servicioCalendario.esDiaHabil(any())).thenReturn(true);

        servicioPrestamo.procesarDevolucion("P-100");

        assertEquals("DEVUELTO", prestamo.getEstado());
        assertEquals(Ejemplar.ESTADO_DISPONIBLE, ejemplar.getEstado());
        verify(notificadorMock, times(1)).enviarNotificacion(anyString(), eq(docente));
        verify(prestamoRepository, times(1)).save(prestamo);
    }
}
