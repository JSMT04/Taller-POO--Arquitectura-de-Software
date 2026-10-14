package com.biblioteca.infrastructure.persistence;

import com.biblioteca.domain.model.Prestamo;
import com.biblioteca.domain.port.PrestamoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementación en memoria del {@link PrestamoRepository}.
 */
@Repository
public class InMemoryPrestamoRepository implements PrestamoRepository {

    private final Map<String, Prestamo> almacen = new ConcurrentHashMap<>();

    @Override
    public Optional<Prestamo> findById(String idPrestamo) {
        return Optional.ofNullable(almacen.get(idPrestamo));
    }

    @Override
    public List<Prestamo> findActivosByUsuarioId(String idUsuario) {
        return almacen.values().stream()
            .filter(p -> "ACTIVO".equals(p.getEstado()))
            .filter(p -> p.getUsuario() != null
                      && idUsuario.equals(p.getUsuario().getIdUsuario()))
            .collect(Collectors.toList());
    }

    @Override
    public Prestamo save(Prestamo prestamo) {
        almacen.put(prestamo.getIdPrestamo(), prestamo);
        return prestamo;
    }
}
