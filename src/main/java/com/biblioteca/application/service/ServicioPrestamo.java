package com.biblioteca.application.service;

import com.biblioteca.domain.exception.LimitePrestamosSuperadoException;
import com.biblioteca.domain.exception.RecursoNoEncontradoException;
import com.biblioteca.domain.model.Ejemplar;
import com.biblioteca.domain.model.Prestamo;
import com.biblioteca.domain.model.Usuario;
import com.biblioteca.domain.port.LibroRepository;
import com.biblioteca.domain.port.NotificadorMulta;
import com.biblioteca.domain.port.PrestamoRepository;
import com.biblioteca.domain.port.ServicioCalendario;
import com.biblioteca.domain.port.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Servicio de aplicación que orquesta los casos de uso de préstamos:
 * creación y procesamiento de devoluciones con cálculo de multas.
 *
 * <p>Aplica SRP, OCP y DIP: depende de abstracciones, no de implementaciones.</p>
 */
@Service
public class ServicioPrestamo {

    private static final Logger log = Logger.getLogger(ServicioPrestamo.class.getName());

    private final UsuarioRepository  usuarioRepository;
    private final LibroRepository    libroRepository;
    private final PrestamoRepository prestamoRepository;
    private final ServicioCalendario servicioCalendario;

    /**
     * Lista de notificadores inyectados por Spring. Todos los beans que implementen
     * {@link NotificadorMulta} se inyectan aquí automáticamente (OCP).
     */
    private final List<NotificadorMulta> notificadores;

    public ServicioPrestamo(UsuarioRepository usuarioRepository,
                            LibroRepository libroRepository,
                            PrestamoRepository prestamoRepository,
                            ServicioCalendario servicioCalendario,
                            List<NotificadorMulta> notificadores) {
        this.usuarioRepository  = usuarioRepository;
        this.libroRepository    = libroRepository;
        this.prestamoRepository = prestamoRepository;
        this.servicioCalendario = servicioCalendario;
        this.notificadores      = notificadores;
    }

    /**
     * Crea un nuevo préstamo para el usuario indicado con los ejemplares solicitados.
     *
     * @param usuarioId          ID del usuario que solicita el préstamo.
     * @param codigosEjemplares  códigos de barras de los ejemplares a prestar.
     * @param fechaDevolucion    fecha límite pactada para la devolución.
     * @return el {@link Prestamo} creado y persistido.
     */
    public Prestamo crearPrestamo(String usuarioId,
                                  List<String> codigosEjemplares,
                                  Date fechaDevolucion) {

        log.info("Iniciando préstamo para usuario: " + usuarioId);

        // 1. Verificar existencia del usuario
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", usuarioId));

        // 2. Verificar límite de préstamos activos + nuevos
        List<Prestamo> prestamosActivos = prestamoRepository.findActivosByUsuarioId(usuarioId);
        int ejemplaresActivosCount = prestamosActivos.stream()
            .mapToInt(p -> p.getEjemplares().size())
            .sum();
        int totalSolicitados = ejemplaresActivosCount + codigosEjemplares.size();

        if (totalSolicitados > usuario.getLimitePrestamos()) {
            log.warning("Usuario " + usuarioId + " supera límite: activos=" + ejemplaresActivosCount
                + ", solicitados=" + codigosEjemplares.size()
                + ", límite=" + usuario.getLimitePrestamos());
            throw new LimitePrestamosSuperadoException(
                usuarioId, usuario.getLimitePrestamos(), totalSolicitados);
        }

        // 3. Recuperar y validar ejemplares
        List<Ejemplar> ejemplaresParaPrestar = new ArrayList<>();
        for (String codigo : codigosEjemplares) {
            Ejemplar ejemplar = libroRepository.findEjemplarByCodigoBarras(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ejemplar", codigo));

            if (!ejemplar.isDisponible()) {
                throw new IllegalStateException(
                    "El ejemplar '" + codigo + "' ya está en estado PRESTADO.");
            }
            ejemplaresParaPrestar.add(ejemplar);
        }

        // 4. Actualizar estado de ejemplares a PRESTADO
        ejemplaresParaPrestar.forEach(e -> e.actualizarEstado(Ejemplar.ESTADO_PRESTADO));

        // 5. Crear y persistir el préstamo
        Prestamo prestamo = Prestamo.builder()
            .idPrestamo(UUID.randomUUID().toString())
            .usuario(usuario)
            .ejemplares(ejemplaresParaPrestar)
            .fechaEntrega(new Date())
            .fechaDevolucion(fechaDevolucion)
            .estado("ACTIVO")
            .build();

        Prestamo prestamoGuardado = prestamoRepository.save(prestamo);
        log.info("Préstamo creado: id=" + prestamoGuardado.getIdPrestamo()
            + ", usuario=" + usuario.getNombre());

        return prestamoGuardado;
    }

    /**
     * Procesa la devolución de un préstamo, calcula multa y notifica si aplica.
     *
     * @param idPrestamo el ID del préstamo a devolver.
     */
    public void procesarDevolucion(String idPrestamo) {
        log.info("Procesando devolución del préstamo: " + idPrestamo);

        // 1. Recuperar el préstamo
        Prestamo prestamo = prestamoRepository.findById(idPrestamo)
            .orElseThrow(() -> new RecursoNoEncontradoException("Préstamo", idPrestamo));

        // 2. Registrar la fecha real de devolución (hoy)
        prestamo.setFechaEntrega(new Date());

        // 3. Calcular la multa
        double multa = prestamo.calcularMulta(servicioCalendario);
        log.info("Multa calculada para préstamo " + idPrestamo + ": $" + multa);

        // 4. Si hay multa, notificar por todos los canales
        if (multa > 0) {
            String montoFormateado = NumberFormat.getCurrencyInstance(new Locale("es", "CO"))
                .format(multa);
            String mensajeMulta = String.format(
                "Estimado/a %s, su préstamo (ID: %s) tiene una multa de %s "
                + "por días hábiles de retraso en la devolución. "
                + "Por favor, liquide su deuda en la biblioteca para continuar usando el servicio.",
                prestamo.getUsuario().getNombre(),
                idPrestamo,
                montoFormateado
            );

            log.info("Enviando notificaciones de multa a " + notificadores.size() + " canales...");
            notificadores.forEach(n -> n.enviarNotificacion(mensajeMulta, prestamo.getUsuario()));
        } else {
            log.info("Devolución a tiempo. No se generó multa.");
        }

        // 5. Devolver ejemplares al estado DISPONIBLE
        prestamo.getEjemplares().forEach(e -> e.actualizarEstado(Ejemplar.ESTADO_DISPONIBLE));

        // 6. Marcar el préstamo como DEVUELTO
        prestamo.setEstado("DEVUELTO");
        prestamoRepository.save(prestamo);

        log.info("Devolución completada. Ejemplares disponibles nuevamente.");
    }
}
