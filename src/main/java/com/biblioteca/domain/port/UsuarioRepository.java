package com.biblioteca.domain.port;

import com.biblioteca.domain.model.Usuario;

import java.util.Optional;

/**
 * Puerto de repositorio para la gestión de usuarios del sistema de biblioteca.
 * Define las operaciones de persistencia sin acoplarse a ninguna tecnología concreta.
 */
public interface UsuarioRepository {

    /**
     * Busca un usuario por su identificador único.
     *
     * @param idUsuario el ID del usuario a buscar.
     * @return un {@link Optional} con el usuario si existe, o vacío si no.
     */
    Optional<Usuario> findById(String idUsuario);

    /**
     * Guarda o actualiza un usuario en el repositorio.
     *
     * @param usuario el usuario a persistir.
     * @return el usuario persistido.
     */
    Usuario save(Usuario usuario);

    /**
     * Retorna todos los usuarios registrados.
     *
     * @return lista de todos los usuarios.
     */
    java.util.List<Usuario> findAll();
}
