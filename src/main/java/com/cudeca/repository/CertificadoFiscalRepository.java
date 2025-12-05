package com.cudeca.repository;

import com.cudeca.model.negocio.CertificadoFiscal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * Repositorio para gestionar certificados fiscales.
 */
@Repository
public interface CertificadoFiscalRepository extends JpaRepository<CertificadoFiscal, Long> {

    /**
     * Encuentra un certificado por número de serie.
     */
    Optional<CertificadoFiscal> findByNumeroSerie(String numeroSerie);

    /**
     * Encuentra certificados emitidos en un rango de fechas.
     */
    Page<CertificadoFiscal> findByFechaEmisionBetween(Instant fechaInicio, Instant fechaFin, Pageable pageable);

    /**
     * Encuentra certificados de un usuario.
     */
    Page<CertificadoFiscal> findByDatosFiscales_Usuario_Id(Long usuarioId, Pageable pageable);

    /**
     * Cuenta certificados de un usuario.
     */
    long countByDatosFiscales_Usuario_Id(Long usuarioId);
}
