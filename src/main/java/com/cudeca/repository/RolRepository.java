package com.cudeca.repository;
import com.cudeca.model.usuario.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface RolRepository extends JpaRepository<Rol, Long>{
    // Busca un rol por su nombre (ej: "ROLE_USER")
    Optional<Rol> findByNombre(String nombre);
}
