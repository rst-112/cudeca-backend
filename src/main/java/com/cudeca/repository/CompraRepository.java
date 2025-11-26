package com.cudeca.repository;

import com.cudeca.model.negocio.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    // Devuelve todas las compras de un usuario
    List<Compra> findByUsuarioId(Long usuarioId);
}
