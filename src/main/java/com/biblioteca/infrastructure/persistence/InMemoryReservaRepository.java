package com.biblioteca.infrastructure.persistence;

import com.biblioteca.domain.model.Reserva;
import com.biblioteca.domain.port.ReservaRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación en memoria del {@link ReservaRepository}.
 */
@Repository
public class InMemoryReservaRepository implements ReservaRepository {

    private final Map<String, Reserva> almacen = new ConcurrentHashMap<>();

    @Override
    public Optional<Reserva> findById(String idReserva) {
        return Optional.ofNullable(almacen.get(idReserva));
    }

    @Override
    public Reserva save(Reserva reserva) {
        almacen.put(reserva.getIdReserva(), reserva);
        return reserva;
    }
}
