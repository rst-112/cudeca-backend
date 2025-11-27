package com.cudeca.repository;

import com.cudeca.model.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestionar la entidad Usuario.
 * Extiende de JpaRepository para obtener operaciones CRUD básicas automáticamente.
 */
@Repository // (1)
public interface UsuarioRepository extends JpaRepository<Usuario, Long> { // (2)

    /**
     * Busca un usuario por su email exacto.
     * Spring Data implementa la consulta: SELECT * FROM usuarios WHERE email = ?
     * * @param email El correo a buscar.
     * @return Un Optional que contiene el usuario si existe, o vacío si no.
     */
    Optional<Usuario> findByEmail(String email); // (3)

    /**
     * Verifica si existe algún usuario con este email.
     * Es mucho más eficiente que traer el objeto entero si solo queremos validar.
     * Spring Data implementa: SELECT COUNT(*) > 0 FROM usuarios WHERE email = ?
     */
    boolean existsByEmail(String email); // (4)
}