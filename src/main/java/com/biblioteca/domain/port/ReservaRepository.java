package com.biblioteca.domain.port;

import com.biblioteca.domain.model.Reserva;

import java.util.Optional;

/**
 * Puerto de repositorio para la gestión de reservas.
 */
public interface ReservaRepository {

    /**
     * Busca una reserva por su identificador.
     *
     * @param idReserva el ID de la reserva.
     * @return un {@link Optional} con la reserva si existe.
     */
    Optional<Reserva> findById(String idReserva);

    /**
     * Guarda o actualiza una reserva en el repositorio.
     *
     * @param reserva la reserva a persistir.
     * @return la reserva persistida.
     */
    Reserva save(Reserva reserva);

    /**
     * Retorna todas las reservas registradas.
     *
     * @return lista de todas las reservas.
     */
    java.util.List<Reserva> findAll();
}
