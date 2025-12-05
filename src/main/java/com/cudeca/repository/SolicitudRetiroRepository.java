package com.cudeca.repository;

import com.cudeca.model.enums.EstadoRetiro;
import com.cudeca.model.negocio.SolicitudRetiro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudRetiroRepository extends JpaRepository<SolicitudRetiro, Long> {

    List<SolicitudRetiro> findByEstado(EstadoRetiro estado);

    List<SolicitudRetiro> findByUsuario_Id(Long usuarioId);

    Page<SolicitudRetiro> findByEstado(EstadoRetiro estado, Pageable pageable);
}
