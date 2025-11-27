package com.cudeca.repository;

import com.cudeca.model.negocio.MovimientoMonedero;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar el historial de transacciones del monedero.
 * Permite consultar abonos, cargos y retiros.
 */
@Repository // (1)
public interface MovimientoMonederoRepository extends JpaRepository<MovimientoMonedero, Long> { // (2)

    /**
     * Recupera todos los movimientos de un monedero concreto.
     * Útil para exportaciones a Excel o vistas completas.
     *
     * @param monederoId ID del monedero (clave foránea).
     * @return Lista completa de movimientos.
     */
    List<MovimientoMonedero> findByMonedero_Id(Long monederoId); // (3)

    /**
     * MEJOR PRÁCTICA: Recupera los movimientos PAGINADOS.
     * Evita colapsar la memoria si un usuario tiene miles de movimientos.
     * Se usa pasando un objeto PageRequest.of(pagina, tamaño, orden).
     *
     * @param monederoId ID del monedero.
     * @param pageable Configuración de paginación (ej: página 0, 10 elementos, orden desc fecha).
     * @return Una página de movimientos.
     */
    Page<MovimientoMonedero> findByMonedero_Id(Long monederoId, Pageable pageable); // (4)
}