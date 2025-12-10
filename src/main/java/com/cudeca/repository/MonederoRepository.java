package com.cudeca.repository;

import com.cudeca.model.negocio.Monedero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonederoRepository extends JpaRepository<Monedero, Long> {

    Optional<Monedero> findByUsuario_Id(Long usuarioId);
}
