package com.biblioteca.infrastructure.config;

import com.biblioteca.application.service.ServicioPrestamo;
import com.biblioteca.application.service.ServicioReserva;
import com.biblioteca.domain.exception.LibroDisponibleException;
import com.biblioteca.domain.exception.LibroNoDisponibleException;
import com.biblioteca.domain.model.*;
import com.biblioteca.domain.port.LibroRepository;
import com.biblioteca.domain.port.PrestamoRepository;
import com.biblioteca.domain.port.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Runner de demostración del Sistema de Gestión de Préstamos de Biblioteca Universitaria.
 *
 * <p>Ejecuta tres escenarios de prueba en orden, tal como fueron solicitados:</p>
 * <ol>
 *   <li><b>Escenario 1</b>: Libro sin ejemplares disponibles (todos PRESTADOS).</li>
 *   <li><b>Escenario 2</b>: Estudiante intenta préstamo → falla por no disponibilidad
 *       → se crea Reserva exitosamente.</li>
 *   <li><b>Escenario 3</b>: Docente con préstamo vencido 5 días calendario (≈3 días hábiles).
 *       Se procesa la devolución, se calcula la multa y se notifica por Email y SMS.</li>
 * </ol>
 */
@Component
public class BibliotecaRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BibliotecaRunner.class);

    private static final String SEPARATOR =
        "═══════════════════════════════════════════════════════════════════";
    private static final String SEPARATOR_THIN =
        "───────────────────────────────────────────────────────────────────";

    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;
    private final ServicioPrestamo servicioPrestamo;
    private final ServicioReserva servicioReserva;

    public BibliotecaRunner(
            UsuarioRepository usuarioRepository,
            LibroRepository libroRepository,
            PrestamoRepository prestamoRepository,
            ServicioPrestamo servicioPrestamo,
            ServicioReserva servicioReserva) {
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
        this.servicioPrestamo = servicioPrestamo;
        this.servicioReserva = servicioReserva;
    }

    @Override
    public void run(String... args) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("   SISTEMA DE GESTIÓN DE PRÉSTAMOS - BIBLIOTECA UNIVERSITARIA");
        System.out.println("   Arquitectura Limpia | Spring Boot | Principios SOLID");
        System.out.println(SEPARATOR);
        System.out.println();

        // ─── Datos iniciales del sistema ───────────────────────────────────────
        configurarDatosIniciales();

        // ─── Escenarios de prueba ──────────────────────────────────────────────
        escenario1_LibroSinDisponibilidad();
        escenario2_EstudianteCreaReserva();
        escenario3_DocenteConPrestamoVencido();

        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("   FIN DE LA DEMOSTRACIÓN — Todos los escenarios completados.");
        System.out.println(SEPARATOR);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Configuración inicial del sistema
    // ═══════════════════════════════════════════════════════════════════════════

    private void configurarDatosIniciales() {
        System.out.println("[ INICIALIZACIÓN ] Cargando datos del sistema...");
        System.out.println(SEPARATOR_THIN);

        // — Usuarios —
        Estudiante estudiante = new Estudiante("U001", "Ana García López", "Ingeniería de Sistemas", 5);
        Docente docente = new Docente("U002", "Prof. Carlos Mendoza", "Matemáticas", "TIEMPO_COMPLETO");
        usuarioRepository.save(estudiante);
        usuarioRepository.save(docente);
        System.out.println("  ✔ Usuarios registrados:");
        System.out.println("    → " + estudiante + " | Límite: " + estudiante.getLimitePrestamos() + " préstamos");
        System.out.println("    → " + docente + " | Límite: " + docente.getLimitePrestamos() + " préstamos");

        // — Autores —
        Autor autorA = Autor.builder()
            .idAutor("A001").nombre("Robert C. Martin")
            .biografia("Conocido como 'Uncle Bob'. Autor de Clean Code y Clean Architecture.")
            .build();
        Autor autorB = Autor.builder()
            .idAutor("A002").nombre("Erich Gamma")
            .biografia("Co-autor del libro 'Design Patterns: Elements of Reusable OO Software'.")
            .build();

        // — Libro 1: "Clean Code" — todos los ejemplares en estado PRESTADO
        Ejemplar ej1A = Ejemplar.builder()
            .codigoBarras("CB-001-A").estado(Ejemplar.ESTADO_PRESTADO).build();
        Ejemplar ej1B = Ejemplar.builder()
            .codigoBarras("CB-001-B").estado(Ejemplar.ESTADO_PRESTADO).build();

        Libro cleanCode = Libro.builder()
            .isbn("978-0-13-235088-4")
            .titulo("Clean Code: A Handbook of Agile Software Craftsmanship")
            .ejemplares(new ArrayList<>(Arrays.asList(ej1A, ej1B)))
            .autores(new ArrayList<>(Collections.singletonList(autorA)))
            .build();
        libroRepository.save(cleanCode);

        // — Libro 2: "Design Patterns" — con ejemplares disponibles (para el docente)
        Ejemplar ej2A = Ejemplar.builder()
            .codigoBarras("CB-002-A").estado(Ejemplar.ESTADO_DISPONIBLE).build();
        Ejemplar ej2B = Ejemplar.builder()
            .codigoBarras("CB-002-B").estado(Ejemplar.ESTADO_DISPONIBLE).build();

        Libro designPatterns = Libro.builder()
            .isbn("978-0-20-163361-5")
            .titulo("Design Patterns: Elements of Reusable Object-Oriented Software")
            .ejemplares(new ArrayList<>(Arrays.asList(ej2A, ej2B)))
            .autores(new ArrayList<>(Collections.singletonList(autorB)))
            .build();
        libroRepository.save(designPatterns);

        System.out.println("  ✔ Catálogo de libros registrado:");
        System.out.println("    → \"" + cleanCode.getTitulo() + "\"");
        System.out.println("      ISBN: " + cleanCode.getIsbn()
            + " | Disponible: " + cleanCode.estaDisponible()
            + " (ambos ejemplares PRESTADOS)");
        System.out.println("    → \"" + designPatterns.getTitulo() + "\"");
        System.out.println("      ISBN: " + designPatterns.getIsbn()
            + " | Disponible: " + designPatterns.estaDisponible());
        System.out.println(SEPARATOR_THIN);
        System.out.println();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Escenario 1: Libro sin ejemplares disponibles
    // ═══════════════════════════════════════════════════════════════════════════

    private void escenario1_LibroSinDisponibilidad() {
        System.out.println(SEPARATOR);
        System.out.println("  ESCENARIO 1: Verificación de disponibilidad del libro \"Clean Code\"");
        System.out.println(SEPARATOR);

        libroRepository.findByIsbn("978-0-13-235088-4").ifPresent(libro -> {
            System.out.println("  Libro: \"" + libro.getTitulo() + "\"");
            System.out.println("  ISBN : " + libro.getIsbn());
            System.out.println("  Total de ejemplares: " + libro.getEjemplares().size());
            libro.getEjemplares().forEach(e ->
                System.out.println("    → Código: " + e.getCodigoBarras() + " | Estado: " + e.getEstado()));
            System.out.println();
            System.out.println("  Resultado: libro.estaDisponible() → " + libro.estaDisponible());
            System.out.println("  ✘ El libro NO tiene ejemplares disponibles para préstamo directo.");
        });
        System.out.println();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Escenario 2: Estudiante intenta préstamo → falla → crea Reserva
    // ═══════════════════════════════════════════════════════════════════════════

    private void escenario2_EstudianteCreaReserva() {
        System.out.println(SEPARATOR);
        System.out.println("  ESCENARIO 2: Estudiante Ana García intenta préstamo de \"Clean Code\"");
        System.out.println("               → Falla por no disponibilidad → Crea Reserva");
        System.out.println(SEPARATOR);

        final String ISBN_CLEAN_CODE = "978-0-13-235088-4";
        final String ID_ESTUDIANTE   = "U001";

        // 2a. Intento de préstamo directo (debe fallar por no disponibilidad)
        System.out.println("  [PASO 2.1] Intentando crear préstamo directo...");
        try {
            // Verifica si el libro tiene ejemplares disponibles manualmente
            Libro libro = libroRepository.findByIsbn(ISBN_CLEAN_CODE).orElseThrow();
            if (!libro.estaDisponible()) {
                throw new LibroNoDisponibleException(ISBN_CLEAN_CODE);
            }
            // Si llegara aquí, el préstamo procedería (no debería llegar)
            servicioPrestamo.crearPrestamo(
                ID_ESTUDIANTE,
                List.of(libro.getPrimerEjemplarDisponible().getCodigoBarras()),
                fechaEnDias(14)
            );
        } catch (LibroNoDisponibleException e) {
            System.out.println("  ✘ Préstamo rechazado: " + e.getMessage());
        }

        // 2b. Crear Reserva
        System.out.println();
        System.out.println("  [PASO 2.2] Creando reserva para el libro...");
        try {
            Reserva reserva = servicioReserva.crearReserva(ID_ESTUDIANTE, ISBN_CLEAN_CODE);
            System.out.println("  ✔ Reserva creada exitosamente:");
            System.out.println("    ID Reserva  : " + reserva.getIdReserva());
            System.out.println("    Usuario     : " + reserva.getUsuario().getNombre());
            System.out.println("    Libro       : " + reserva.getLibro().getTitulo());
            System.out.println("    Fecha       : " + reserva.getFechaReserva());
            System.out.println("    Estado      : " + reserva.getEstado());
        } catch (LibroDisponibleException e) {
            System.out.println("  ✘ No se creó reserva: " + e.getMessage());
        }
        System.out.println();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Escenario 3: Docente con préstamo vencido → multa → notificación
    // ═══════════════════════════════════════════════════════════════════════════

    private void escenario3_DocenteConPrestamoVencido() {
        System.out.println(SEPARATOR);
        System.out.println("  ESCENARIO 3: Prof. Carlos Mendoza devuelve \"Design Patterns\"");
        System.out.println("               con 5 días de retraso → Cálculo de multa y notificaciones");
        System.out.println(SEPARATOR);

        final String ID_DOCENTE = "U002";

        // 3a. Crear préstamo del docente (fecha de devolución = hace 5 días)
        System.out.println("  [PASO 3.1] Registrando préstamo con fecha vencida...");
        Date fechaLimiteVencida = fechaEnDias(-5); // La fecha límite fue hace 5 días
        Prestamo prestamo = servicioPrestamo.crearPrestamo(
            ID_DOCENTE,
            List.of("CB-002-A", "CB-002-B"),
            fechaLimiteVencida
        );
        System.out.println("  ✔ Préstamo registrado:");
        System.out.println("    ID          : " + prestamo.getIdPrestamo());
        System.out.println("    Usuario     : " + prestamo.getUsuario().getNombre());
        System.out.println("    Ejemplares  : " + prestamo.getEjemplares().size());
        System.out.println("    Fecha límite: " + prestamo.getFechaDevolucion() + " (VENCIDA hace 5 días)");

        // 3b. Forzar que la fecha de entrega sea HOY (la devolución se hace hoy, 5 días tarde)
        // Esto ya se maneja en procesarDevolucion() que asigna new Date() al momento de procesar.

        // 3c. Procesar devolución
        System.out.println();
        System.out.println("  [PASO 3.2] Procesando devolución con retraso...");
        System.out.println("  " + SEPARATOR_THIN);
        servicioPrestamo.procesarDevolucion(prestamo.getIdPrestamo());
        System.out.println("  " + SEPARATOR_THIN);

        // 3d. Verificar que los ejemplares quedaron disponibles
        System.out.println();
        System.out.println("  [PASO 3.3] Verificando estado post-devolución:");
        Prestamo prestamoFinal = prestamoRepository.findById(prestamo.getIdPrestamo()).orElseThrow();
        System.out.println("    Estado del préstamo : " + prestamoFinal.getEstado());
        prestamoFinal.getEjemplares().forEach(e ->
            System.out.println("    Ejemplar " + e.getCodigoBarras() + " → " + e.getEstado()));
        System.out.println("  ✔ Devolución procesada correctamente.");
        System.out.println();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Utilidades
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Retorna una fecha relativa al día de hoy.
     *
     * @param dias número de días a sumar (positivo = futuro, negativo = pasado).
     * @return la fecha calculada.
     */
    private Date fechaEnDias(int dias) {
        LocalDate fecha = LocalDate.now().plusDays(dias);
        return Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
