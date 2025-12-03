package com.cudeca.repository;

import com.cudeca.model.usuario.Exportacion;
import com.cudeca.model.enums.FormatoExportacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repositorio para gestionar exportaciones realizadas por administradores.
 */
@Repository
public interface ExportacionRepository extends JpaRepository<Exportacion, Long> {

    /**
     * Encuentra todas las exportaciones de un administrador.
     */
    List<Exportacion> findByAdministrador_Id(Long administradorId);

    /**
     * Encuentra exportaciones por administrador con paginación.
     */
    Page<Exportacion> findByAdministrador_Id(Long administradorId, Pageable pageable);

    /**
     * Encuentra exportaciones de un administrador en un rango de fechas.
     */
    List<Exportacion> findByAdministrador_IdAndGeneradoEnBetween(Long administradorId, Instant inicio, Instant fin);

    /**
     * Encuentra exportaciones por formato.
     */
    List<Exportacion> findByFormato(FormatoExportacion formato);

    /**
     * Encuentra exportaciones de un administrador por formato específico.
     */
    List<Exportacion> findByAdministrador_IdAndFormato(Long administradorId, FormatoExportacion formato);

    /**
     * Cuenta el total de exportaciones de un administrador.
     */
    long countByAdministrador_Id(Long administradorId);
}

