package com.cudeca.repository;

import com.cudeca.model.negocio.Monedero;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MonederoRepository extends JpaRepository<Monedero, Long>{
    // Busca el monedero asociado a un usuario concreto
    Optional<Monedero> findByUsuarioId(Long usuarioId);
}
