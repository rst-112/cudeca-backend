package com.cudeca.repository;

import com.cudeca.model.evento.ImagenEvento;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar imágenes asociadas a eventos.
 */
@Repository
public interface ImagenEventoRepository extends JpaRepository<ImagenEvento, Long> {

    /**
     * Encuentra todas las imágenes de un evento, ordenadas por orden.
     */
    List<ImagenEvento> findByEvento_Id(Long eventoId, Sort sort);

    /**
     * Encuentra la imagen principal de un evento.
     */
    List<ImagenEvento> findByEvento_IdAndEsPrincipal(Long eventoId, Boolean esPrincipal);

    /**
     * Encuentra imágenes de resumen de un evento.
     */
    List<ImagenEvento> findByEvento_IdAndEsResumen(Long eventoId, Boolean esResumen);
}

