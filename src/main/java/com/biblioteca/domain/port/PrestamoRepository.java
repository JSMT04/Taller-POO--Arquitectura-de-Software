package com.biblioteca.domain.port;

import com.biblioteca.domain.model.Prestamo;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de repositorio para la gestión de préstamos.
 */
public interface PrestamoRepository {

    /**
     * Busca un préstamo por su identificador.
     *
     * @param idPrestamo el ID del préstamo.
     * @return un {@link Optional} con el préstamo si existe.
     */
    Optional<Prestamo> findById(String idPrestamo);

    /**
     * Retorna todos los préstamos activos de un usuario específico.
     *
     * @param idUsuario el ID del usuario.
     * @return lista (posiblemente vacía) de préstamos activos del usuario.
     */
    List<Prestamo> findActivosByUsuarioId(String idUsuario);

    /**
     * Guarda o actualiza un préstamo en el repositorio.
     *
     * @param prestamo el préstamo a persistir.
     * @return el préstamo persistido.
     */
    Prestamo save(Prestamo prestamo);

    /**
     * Retorna todos los préstamos registrados.
     *
     * @return lista de todos los préstamos.
     */
    List<Prestamo> findAll();
}
