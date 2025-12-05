package com.cudeca.repository;

import com.cudeca.model.usuario.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repositorio para gestionar registros de auditoría.
 */
@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    /**
     * Encuentra auditorías de un usuario.
     */
    List<Auditoria> findByUsuario_Id(Long usuarioId);

    /**
     * Encuentra auditorías de una acción específica.
     */
    List<Auditoria> findByAccion(String accion);

    /**
     * Encuentra auditorías en un rango de fechas.
     */
    Page<Auditoria> findByFechaBetween(Instant inicio, Instant fin, Pageable pageable);

    /**
     * Encuentra auditorías de una entidad específica.
     */
    List<Auditoria> findByEntidadAndEntidadId(String entidad, String entidadId);
}
