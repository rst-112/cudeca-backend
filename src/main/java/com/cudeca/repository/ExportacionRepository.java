package com.cudeca.repository;

import com.cudeca.model.enums.FormatoExportacion;
import com.cudeca.model.usuario.Exportacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ExportacionRepository extends JpaRepository<Exportacion, Long> {

    List<Exportacion> findByUsuario_Id(Long usuarioId);

    Page<Exportacion> findByUsuario_Id(Long usuarioId, Pageable pageable);

    List<Exportacion> findByUsuario_IdAndGeneradoEnBetween(Long usuarioId, Instant inicio, Instant fin);

    List<Exportacion> findByFormato(FormatoExportacion formato);

    List<Exportacion> findByUsuario_IdAndFormato(Long usuarioId, FormatoExportacion formato);

    long countByUsuario_Id(Long usuarioId);
}
