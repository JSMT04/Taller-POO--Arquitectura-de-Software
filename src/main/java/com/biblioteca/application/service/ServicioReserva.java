package com.biblioteca.application.service;

import com.biblioteca.domain.exception.LibroDisponibleException;
import com.biblioteca.domain.exception.RecursoNoEncontradoException;
import com.biblioteca.domain.model.Libro;
import com.biblioteca.domain.model.Reserva;
import com.biblioteca.domain.model.Usuario;
import com.biblioteca.domain.port.LibroRepository;
import com.biblioteca.domain.port.ReservaRepository;
import com.biblioteca.domain.port.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Servicio de aplicación que orquesta el caso de uso de reservas.
 *
 * <p>Regla central: solo se puede reservar cuando el libro NO tiene disponibilidad.</p>
 */
@Service
public class ServicioReserva {

    private static final Logger log = Logger.getLogger(ServicioReserva.class.getName());

    private final UsuarioRepository usuarioRepository;
    private final LibroRepository   libroRepository;
    private final ReservaRepository reservaRepository;

    public ServicioReserva(UsuarioRepository usuarioRepository,
                           LibroRepository libroRepository,
                           ReservaRepository reservaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.libroRepository   = libroRepository;
        this.reservaRepository = reservaRepository;
    }

    /**
     * Crea una reserva para el usuario sobre el libro (ISBN), solo si no hay disponibilidad.
     *
     * @param usuarioId ID del usuario que solicita la reserva.
     * @param isbn      ISBN del libro a reservar.
     * @return la {@link Reserva} creada y persistida.
     * @throws RecursoNoEncontradoException si el usuario o libro no existen.
     * @throws LibroDisponibleException     si el libro tiene ejemplares disponibles.
     */
    public Reserva crearReserva(String usuarioId, String isbn) {
        log.info("Solicitando reserva: usuario=" + usuarioId + ", isbn=" + isbn);

        // 1. Verificar existencia del usuario
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", usuarioId));

        // 2. Verificar existencia del libro
        Libro libro = libroRepository.findByIsbn(isbn)
            .orElseThrow(() -> new RecursoNoEncontradoException("Libro (ISBN)", isbn));

        // 3. Verificar disponibilidad — si tiene ejemplares disponibles, NO se puede reservar
        if (libro.estaDisponible()) {
            log.warning("Reserva rechazada: el libro '" + libro.getTitulo()
                + "' tiene ejemplares disponibles.");
            throw new LibroDisponibleException(isbn);
        }

        // 4. Crear y persistir la reserva
        Reserva reserva = Reserva.builder()
            .idReserva(UUID.randomUUID().toString())
            .usuario(usuario)
            .libro(libro)
            .fechaReserva(new Date())
            .estado(Reserva.ESTADO_PENDIENTE)
            .build();

        Reserva reservaGuardada = reservaRepository.save(reserva);
        log.info("Reserva creada: id=" + reservaGuardada.getIdReserva()
            + ", usuario=" + usuario.getNombre()
            + ", libro='" + libro.getTitulo() + "'");

        return reservaGuardada;
    }
}
