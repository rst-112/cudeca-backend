package com.cudeca.repository;

import com.cudeca.model.negocio.SolicitudRetiro;
import com.cudeca.model.enums.EstadoRetiro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SolicitudRetiroRepository extends JpaRepository<SolicitudRetiro, Long> {
    // Devuelve una lista de todas las solicitudes en un estado (ej: PENDIENTE)
    List<SolicitudRetiro> findByEstado(EstadoRetiro estado);

    // Ver las solicitudes de un usuario específico
    List<SolicitudRetiro> findByUsuarioId(Long usuarioId);
}