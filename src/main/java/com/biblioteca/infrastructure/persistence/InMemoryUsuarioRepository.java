package com.biblioteca.infrastructure.persistence;

import com.biblioteca.domain.model.Usuario;
import com.biblioteca.domain.port.UsuarioRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación en memoria del {@link UsuarioRepository}.
 * Utiliza un {@link ConcurrentHashMap} thread-safe como almacenamiento temporal.
 * Adecuado para demos, pruebas y entornos sin base de datos configurada.
 */
@Repository
public class InMemoryUsuarioRepository implements UsuarioRepository {

    private final Map<String, Usuario> almacen = new ConcurrentHashMap<>();

    @Override
    public Optional<Usuario> findById(String idUsuario) {
        return Optional.ofNullable(almacen.get(idUsuario));
    }

    @Override
    public Usuario save(Usuario usuario) {
        almacen.put(usuario.getIdUsuario(), usuario);
        return usuario;
    }

    @Override
    public java.util.List<Usuario> findAll() {
        return new java.util.ArrayList<>(almacen.values());
    }
}
