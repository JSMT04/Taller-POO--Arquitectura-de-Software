package com.biblioteca.infrastructure.config;

import com.biblioteca.application.service.ServicioPrestamo;
import com.biblioteca.application.service.ServicioReserva;
import com.biblioteca.domain.exception.LibroDisponibleException;
import com.biblioteca.domain.exception.LibroNoDisponibleException;
import com.biblioteca.domain.model.*;
import com.biblioteca.domain.port.LibroRepository;
import com.biblioteca.domain.port.PrestamoRepository;
import com.biblioteca.domain.port.ReservaRepository;
import com.biblioteca.domain.port.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Runner principal y Menú Interactivo de Consola para el Sistema de Gestión de Biblioteca.
 *
 * <p>Provee una interfaz ligera por consola para probar manualmente todas las operaciones
 * del sistema (catálogo, usuarios, préstamos, devoluciones, reservas) y la ejecución
 * de los escenarios de prueba automatizados solicitados en el taller.</p>
 */
@Component
public class BibliotecaRunner implements CommandLineRunner {

    private static final String SEPARATOR =
        "===================================================================";
    private static final String SEPARATOR_THIN =
        "-------------------------------------------------------------------";

    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;
    private final ReservaRepository reservaRepository;
    private final ServicioPrestamo servicioPrestamo;
    private final ServicioReserva servicioReserva;

    public BibliotecaRunner(
            UsuarioRepository usuarioRepository,
            LibroRepository libroRepository,
            PrestamoRepository prestamoRepository,
            ReservaRepository reservaRepository,
            ServicioPrestamo servicioPrestamo,
            ServicioReserva servicioReserva) {
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
        this.reservaRepository = reservaRepository;
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

        // ─── Bucle del menú interactivo por consola ────────────────────────────
        iniciarMenuInteractivo();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Menú Interactivo por Consola
    // ═══════════════════════════════════════════════════════════════════════════

    private void iniciarMenuInteractivo() {
        Scanner scanner = new Scanner(System.in);
        boolean activo = true;

        while (activo) {
            mostrarOpcionesMenu();
            System.out.print("  Seleccione una opción: ");

            // Protección para entornos sin terminal interactivo (pipes, scripts, tests)
            if (!scanner.hasNextLine()) {
                System.out.println("\n[INFO] Fin de entrada detectado. Cerrando interfaz de consola.");
                break;
            }

            String opcion = scanner.nextLine().trim();
            System.out.println();

            switch (opcion) {
                case "1" -> menuConsultarCatalogo();
                case "2" -> menuListarUsuarios();
                case "3" -> menuListarPrestamosYReservas();
                case "4" -> menuRegistrarPrestamo(scanner);
                case "5" -> menuProcesarDevolucion(scanner);
                case "6" -> menuCrearReserva(scanner);
                case "7" -> ejecutarEscenariosDemostracion();
                case "0" -> {
                    System.out.println(SEPARATOR);
                    System.out.println("  ¡Gracias por usar el Sistema de Biblioteca! Hasta pronto.");
                    System.out.println(SEPARATOR);
                    activo = false;
                }
                default -> {
                    System.out.println("  [!] Opcion no valida. Por favor, ingrese un numero del menu.");
                    System.out.println();
                }
            }
        }
    }

    private void mostrarOpcionesMenu() {
        System.out.println(SEPARATOR);
        System.out.println("                        MENU PRINCIPAL");
        System.out.println(SEPARATOR);
        System.out.println("  [1] Ver catalogo de libros y ejemplares");
        System.out.println("  [2] Ver usuarios registrados y cupos disponibles");
        System.out.println("  [3] Ver historial de prestamos y reservas");
        System.out.println("  [4] Registrar un nuevo prestamo (Manual)");
        System.out.println("  [5] Procesar devolucion con calculo de multa (Manual)");
        System.out.println("  [6] Crear una reserva de libro (Manual)");
        System.out.println("  [7] Ejecutar demostracion automatica (Escenarios 1, 2 y 3)");
        System.out.println("  [0] Salir");
        System.out.println(SEPARATOR_THIN);
    }

    private void menuConsultarCatalogo() {
        System.out.println(SEPARATOR);
        System.out.println("  [CATALOGO BIBLIOGRAFICO Y DISPONIBILIDAD]");
        System.out.println(SEPARATOR);

        List<Libro> libros = libroRepository.findAll();
        if (libros.isEmpty()) {
            System.out.println("  No hay libros registrados en el catalogo.");
        } else {
            for (Libro libro : libros) {
                String autores = (libro.getAutores() != null && !libro.getAutores().isEmpty())
                    ? libro.getAutores().stream().map(Autor::getNombre).collect(Collectors.joining(", "))
                    : "No especificado";

                System.out.println("  * Titulo : \"" + libro.getTitulo() + "\"");
                System.out.println("    ISBN   : " + libro.getIsbn());
                System.out.println("    Autores: " + autores);
                System.out.println("    Disponible para prestamo: "
                    + (libro.estaDisponible() ? "[SI - Tiene ejemplares disponibles]" : "[NO - Todos prestados]"));
                System.out.println("    Ejemplares fisicos (" + libro.getEjemplares().size() + "):");

                for (Ejemplar ej : libro.getEjemplares()) {
                    System.out.println("      - Codigo: " + ej.getCodigoBarras()
                        + " | Estado: [" + ej.getEstado() + "]");
                }
                System.out.println();
            }
        }
    }

    private void menuListarUsuarios() {
        System.out.println(SEPARATOR);
        System.out.println("  [USUARIOS REGISTRADOS Y LIMITES POLIMORFICOS]");
        System.out.println(SEPARATOR);

        List<Usuario> usuarios = usuarioRepository.findAll();
        if (usuarios.isEmpty()) {
            System.out.println("  No hay usuarios registrados en el sistema.");
        } else {
            for (Usuario u : usuarios) {
                List<Prestamo> activos = prestamoRepository.findActivosByUsuarioId(u.getIdUsuario());
                int totalEjemplaresActivos = activos.stream().mapToInt(p -> p.getEjemplares().size()).sum();
                int cupoRestante = Math.max(0, u.getLimitePrestamos() - totalEjemplaresActivos);

                String rol = (u instanceof Estudiante) ? "Estudiante" : (u instanceof Docente ? "Docente" : "Usuario");
                System.out.println("  * [" + u.getIdUsuario() + "] " + u.getNombre() + " (" + rol + ")");
                if (u instanceof Estudiante e) {
                    System.out.println("    Carrera: " + e.getCarrera() + " | Semestre: " + e.getSemestre());
                } else if (u instanceof Docente d) {
                    System.out.println("    Departamento: " + d.getDepartamento() + " | Contrato: " + d.getTipoContrato());
                }
                System.out.println("    Limite maximo polimorfico : " + u.getLimitePrestamos() + " ejemplares");
                System.out.println("    Ejemplares activos actuales: " + totalEjemplaresActivos);
                System.out.println("    Cupo disponible para pedir : " + cupoRestante);
                System.out.println();
            }
        }
    }

    private void menuListarPrestamosYReservas() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        System.out.println(SEPARATOR);
        System.out.println("  [HISTORIAL DE PRESTAMOS Y RESERVAS]");
        System.out.println(SEPARATOR);

        System.out.println("  --- PRESTAMOS ---");
        List<Prestamo> prestamos = prestamoRepository.findAll();
        if (prestamos.isEmpty()) {
            System.out.println("  (Sin prestamos registrados)");
        } else {
            for (Prestamo p : prestamos) {
                String usuarioNom = (p.getUsuario() != null)
                    ? p.getUsuario().getNombre() + " [" + p.getUsuario().getIdUsuario() + "]"
                    : "Desconocido";
                String codigos = p.getEjemplares().stream()
                    .map(Ejemplar::getCodigoBarras)
                    .collect(Collectors.joining(", "));
                String fechaDevStr = p.getFechaDevolucion() != null ? sdf.format(p.getFechaDevolucion()) : "N/A";

                System.out.println("  * ID Prestamo  : " + p.getIdPrestamo());
                System.out.println("    Usuario      : " + usuarioNom);
                System.out.println("    Estado       : [" + p.getEstado() + "]");
                System.out.println("    Ejemplares   : " + codigos);
                System.out.println("    Fecha Limite : " + fechaDevStr);
                System.out.println();
            }
        }

        System.out.println("  --- RESERVAS ---");
        List<Reserva> reservas = reservaRepository.findAll();
        if (reservas.isEmpty()) {
            System.out.println("  (Sin reservas registradas)");
        } else {
            for (Reserva r : reservas) {
                String usuarioNom = (r.getUsuario() != null)
                    ? r.getUsuario().getNombre() + " [" + r.getUsuario().getIdUsuario() + "]"
                    : "Desconocido";
                String libroNom = (r.getLibro() != null)
                    ? "\"" + r.getLibro().getTitulo() + "\" (ISBN: " + r.getLibro().getIsbn() + ")"
                    : "Desconocido";
                String fechaResStr = r.getFechaReserva() != null ? sdf.format(r.getFechaReserva()) : "N/A";

                System.out.println("  * ID Reserva   : " + r.getIdReserva());
                System.out.println("    Usuario      : " + usuarioNom);
                System.out.println("    Libro        : " + libroNom);
                System.out.println("    Fecha        : " + fechaResStr);
                System.out.println("    Estado       : [" + r.getEstado() + "]");
                System.out.println();
            }
        }
    }

    private void menuRegistrarPrestamo(Scanner scanner) {
        System.out.println(SEPARATOR);
        System.out.println("  [REGISTRO DE NUEVO PRESTAMO]");
        System.out.println(SEPARATOR);

        System.out.print("  Ingrese ID del usuario (ej: U001, U002): ");
        String idUsuario = scanner.nextLine().trim();

        System.out.print("  Ingrese codigos de barra separados por coma (ej: CB-002-A): ");
        String codigosInput = scanner.nextLine().trim();

        if (codigosInput.isEmpty()) {
            System.out.println("  [X] Operacion cancelada: debe ingresar al menos un codigo de barras.");
            System.out.println();
            return;
        }

        List<String> codigos = Arrays.stream(codigosInput.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();

        System.out.print("  Ingrese dias de plazo para devolucion [Por defecto 14]: ");
        String diasInput = scanner.nextLine().trim();
        int dias = 14;
        if (!diasInput.isEmpty()) {
            try {
                dias = Integer.parseInt(diasInput);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Valor no numerico, usando 14 dias por defecto.");
            }
        }

        Date fechaDevolucion = fechaEnDias(dias);

        try {
            Prestamo prestamo = servicioPrestamo.crearPrestamo(idUsuario, codigos, fechaDevolucion);
            System.out.println();
            System.out.println("  [OK] Prestamo registrado exitosamente!");
            System.out.println("    ID Prestamo  : " + prestamo.getIdPrestamo());
            System.out.println("    Usuario      : " + prestamo.getUsuario().getNombre());
            System.out.println("    Ejemplares   : " + prestamo.getEjemplares().size());
            System.out.println("    Fecha Limite : " + prestamo.getFechaDevolucion());
        } catch (Exception e) {
            System.out.println();
            System.out.println("  [X] Error al crear prestamo: " + e.getMessage());
        }
        System.out.println();
    }

    private void menuProcesarDevolucion(Scanner scanner) {
        System.out.println(SEPARATOR);
        System.out.println("  [PROCESAR DEVOLUCION DE PRESTAMO]");
        System.out.println(SEPARATOR);

        // Mostrar préstamos activos para facilitar la selección
        List<Prestamo> activos = prestamoRepository.findAll().stream()
            .filter(p -> "ACTIVO".equals(p.getEstado()))
            .toList();

        if (activos.isEmpty()) {
            System.out.println("  (No hay prestamos con estado ACTIVO para devolver)");
        } else {
            System.out.println("  Prestamos activos disponibles para devolucion:");
            activos.forEach(p -> System.out.println("    -> ID: " + p.getIdPrestamo()
                + " | Usuario: " + (p.getUsuario() != null ? p.getUsuario().getNombre() : "N/A")
                + " | Fecha Limite: " + p.getFechaDevolucion()));
            System.out.println();
        }

        System.out.print("  Ingrese el ID del prestamo a devolver: ");
        String idPrestamo = scanner.nextLine().trim();

        if (idPrestamo.isEmpty()) {
            System.out.println("  [X] Operacion cancelada: ID de prestamo vacio.");
            System.out.println();
            return;
        }

        try {
            System.out.println("  " + SEPARATOR_THIN);
            servicioPrestamo.procesarDevolucion(idPrestamo);
            System.out.println("  " + SEPARATOR_THIN);
            System.out.println("  [OK] Proceso de devolucion finalizado correctamente.");
        } catch (Exception e) {
            System.out.println("  [X] Error al procesar devolucion: " + e.getMessage());
        }
        System.out.println();
    }

    private void menuCrearReserva(Scanner scanner) {
        System.out.println(SEPARATOR);
        System.out.println("  [CREAR RESERVA DE LIBRO]");
        System.out.println(SEPARATOR);

        System.out.print("  Ingrese ID del usuario (ej: U001, U002): ");
        String idUsuario = scanner.nextLine().trim();

        System.out.print("  Ingrese ISBN del libro (ej: 978-0-13-235088-4): ");
        String isbn = scanner.nextLine().trim();

        if (idUsuario.isEmpty() || isbn.isEmpty()) {
            System.out.println("  [X] Operacion cancelada: usuario e ISBN son obligatorios.");
            System.out.println();
            return;
        }

        try {
            Reserva reserva = servicioReserva.crearReserva(idUsuario, isbn);
            System.out.println();
            System.out.println("  [OK] Reserva creada exitosamente!");
            System.out.println("    ID Reserva : " + reserva.getIdReserva());
            System.out.println("    Usuario    : " + reserva.getUsuario().getNombre());
            System.out.println("    Libro      : " + reserva.getLibro().getTitulo());
            System.out.println("    Estado     : " + reserva.getEstado());
        } catch (LibroDisponibleException e) {
            System.out.println();
            System.out.println("  [X] Reserva rechazada (Regla de Negocio R7):");
            System.out.println("    " + e.getMessage());
            System.out.println("    Nota: Solo se pueden reservar libros cuyos ejemplares esten TODOS prestados.");
        } catch (Exception e) {
            System.out.println();
            System.out.println("  [X] Error al crear reserva: " + e.getMessage());
        }
        System.out.println();
    }

    private void ejecutarEscenariosDemostracion() {
        System.out.println(SEPARATOR);
        System.out.println("   EJECUCIÓN DE ESCENARIOS AUTOMÁTICOS DE DEMOSTRACIÓN");
        System.out.println(SEPARATOR);

        escenario1_LibroSinDisponibilidad();
        escenario2_EstudianteCreaReserva();
        escenario3_DocenteConPrestamoVencido();

        System.out.println(SEPARATOR);
        System.out.println("   FIN DE LA DEMOSTRACIÓN AUTOMÁTICA.");
        System.out.println(SEPARATOR);
        System.out.println();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Configuración inicial del sistema
    // ═══════════════════════════════════════════════════════════════════════════

    private void configurarDatosIniciales() {
        System.out.println("[ INICIALIZACION ] Cargando datos del sistema...");
        System.out.println(SEPARATOR_THIN);

        // -- Usuarios --
        Estudiante estudiante = new Estudiante("U001", "Ana Garcia Lopez", "Ingenieria de Sistemas", 5);
        Docente docente = new Docente("U002", "Prof. Carlos Mendoza", "Matematicas", "TIEMPO_COMPLETO");
        usuarioRepository.save(estudiante);
        usuarioRepository.save(docente);
        System.out.println("  [OK] Usuarios registrados:");
        System.out.println("    -> " + estudiante + " | Limite: " + estudiante.getLimitePrestamos() + " prestamos");
        System.out.println("    -> " + docente + " | Limite: " + docente.getLimitePrestamos() + " prestamos");

        // -- Autores --
        Autor autorA = Autor.builder()
            .idAutor("A001").nombre("Robert C. Martin")
            .biografia("Conocido como 'Uncle Bob'. Autor de Clean Code y Clean Architecture.")
            .build();
        Autor autorB = Autor.builder()
            .idAutor("A002").nombre("Erich Gamma")
            .biografia("Co-autor del libro 'Design Patterns: Elements of Reusable OO Software'.")
            .build();

        // -- Libro 1: "Clean Code" -- todos los ejemplares en estado PRESTADO
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

        // -- Libro 2: "Design Patterns" -- con ejemplares disponibles (para el docente)
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

        System.out.println("  [OK] Catalogo de libros registrado:");
        System.out.println("    -> \"" + cleanCode.getTitulo() + "\"");
        System.out.println("      ISBN: " + cleanCode.getIsbn()
            + " | Disponible: " + cleanCode.estaDisponible()
            + " (ambos ejemplares PRESTADOS)");
        System.out.println("    -> \"" + designPatterns.getTitulo() + "\"");
        System.out.println("      ISBN: " + designPatterns.getIsbn()
            + " | Disponible: " + designPatterns.estaDisponible());
        System.out.println(SEPARATOR_THIN);
        System.out.println();
    }

    // ===========================================================================
    // Escenario 1: Libro sin ejemplares disponibles
    // ===========================================================================

    private void escenario1_LibroSinDisponibilidad() {
        System.out.println(SEPARATOR);
        System.out.println("  ESCENARIO 1: Verificacion de disponibilidad del libro \"Clean Code\"");
        System.out.println(SEPARATOR);

        libroRepository.findByIsbn("978-0-13-235088-4").ifPresent(libro -> {
            System.out.println("  Libro: \"" + libro.getTitulo() + "\"");
            System.out.println("  ISBN : " + libro.getIsbn());
            System.out.println("  Total de ejemplares: " + libro.getEjemplares().size());
            libro.getEjemplares().forEach(e ->
                System.out.println("    -> Codigo: " + e.getCodigoBarras() + " | Estado: " + e.getEstado()));
            System.out.println();
            System.out.println("  Resultado: libro.estaDisponible() -> " + libro.estaDisponible());
            System.out.println("  [X] El libro NO tiene ejemplares disponibles para prestamo directo.");
        });
        System.out.println();
    }

    // ===========================================================================
    // Escenario 2: Estudiante intenta préstamo -> falla -> crea Reserva
    // ===========================================================================

    private void escenario2_EstudianteCreaReserva() {
        System.out.println(SEPARATOR);
        System.out.println("  ESCENARIO 2: Estudiante Ana Garcia intenta prestamo de \"Clean Code\"");
        System.out.println("               -> Falla por no disponibilidad -> Crea Reserva");
        System.out.println(SEPARATOR);

        final String ISBN_CLEAN_CODE = "978-0-13-235088-4";
        final String ID_ESTUDIANTE   = "U001";

        // 2a. Intento de préstamo directo (debe fallar por no disponibilidad)
        System.out.println("  [PASO 2.1] Intentando crear prestamo directo...");
        try {
            Libro libro = libroRepository.findByIsbn(ISBN_CLEAN_CODE).orElseThrow();
            if (!libro.estaDisponible()) {
                throw new LibroNoDisponibleException(ISBN_CLEAN_CODE);
            }
            servicioPrestamo.crearPrestamo(
                ID_ESTUDIANTE,
                List.of(libro.getPrimerEjemplarDisponible().getCodigoBarras()),
                fechaEnDias(14)
            );
        } catch (LibroNoDisponibleException e) {
            System.out.println("  [X] Prestamo rechazado: " + e.getMessage());
        }

        // 2b. Crear Reserva
        System.out.println();
        System.out.println("  [PASO 2.2] Creando reserva para el libro...");
        try {
            Reserva reserva = servicioReserva.crearReserva(ID_ESTUDIANTE, ISBN_CLEAN_CODE);
            System.out.println("  [OK] Reserva creada exitosamente:");
            System.out.println("    ID Reserva  : " + reserva.getIdReserva());
            System.out.println("    Usuario     : " + reserva.getUsuario().getNombre());
            System.out.println("    Libro       : " + reserva.getLibro().getTitulo());
            System.out.println("    Fecha       : " + reserva.getFechaReserva());
            System.out.println("    Estado      : " + reserva.getEstado());
        } catch (LibroDisponibleException e) {
            System.out.println("  [X] No se creo reserva: " + e.getMessage());
        }
        System.out.println();
    }

    // ===========================================================================
    // Escenario 3: Docente con préstamo vencido -> multa -> notificación
    // ===========================================================================

    private void escenario3_DocenteConPrestamoVencido() {
        System.out.println(SEPARATOR);
        System.out.println("  ESCENARIO 3: Prof. Carlos Mendoza devuelve \"Design Patterns\"");
        System.out.println("               con 5 dias de retraso -> Calculo de multa y notificaciones");
        System.out.println(SEPARATOR);

        final String ID_DOCENTE = "U002";

        // 3a. Crear préstamo del docente (fecha de devolución = hace 5 días)
        System.out.println("  [PASO 3.1] Registrando prestamo con fecha vencida...");
        Date fechaLimiteVencida = fechaEnDias(-5);
        Prestamo prestamo = servicioPrestamo.crearPrestamo(
            ID_DOCENTE,
            List.of("CB-002-A", "CB-002-B"),
            fechaLimiteVencida
        );
        System.out.println("  [OK] Prestamo registrado:");
        System.out.println("    ID          : " + prestamo.getIdPrestamo());
        System.out.println("    Usuario     : " + prestamo.getUsuario().getNombre());
        System.out.println("    Ejemplares  : " + prestamo.getEjemplares().size());
        System.out.println("    Fecha limite: " + prestamo.getFechaDevolucion() + " (VENCIDA hace 5 dias)");

        // 3b. Procesar devolución
        System.out.println();
        System.out.println("  [PASO 3.2] Procesando devolucion con retraso...");
        System.out.println("  " + SEPARATOR_THIN);
        servicioPrestamo.procesarDevolucion(prestamo.getIdPrestamo());
        System.out.println("  " + SEPARATOR_THIN);

        // 3c. Verificar que los ejemplares quedaron disponibles
        System.out.println();
        System.out.println("  [PASO 3.3] Verificando estado post-devolucion:");
        Prestamo prestamoFinal = prestamoRepository.findById(prestamo.getIdPrestamo()).orElseThrow();
        System.out.println("    Estado del prestamo : " + prestamoFinal.getEstado());
        prestamoFinal.getEjemplares().forEach(e ->
            System.out.println("    Ejemplar " + e.getCodigoBarras() + " -> " + e.getEstado()));
        System.out.println("  [OK] Devolucion procesada correctamente.");
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
