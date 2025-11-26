package com.cudeca.repository;
import com.cudeca.model.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Spring sabe que buscas por el campo 'email' solo con ver el nombre
    Optional<Usuario> findByEmail(String email);

    // Para validar que no se repitan correos
    boolean existsByEmail(String email);
}
