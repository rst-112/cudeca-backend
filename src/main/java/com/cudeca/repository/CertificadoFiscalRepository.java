package com.cudeca.repository;

import com.cudeca.model.negocio.CertificadoFiscal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar los Certificados de Donación.
 * Crítico para el cumplimiento fiscal y la deducción de impuestos de los socios.
 */
@Repository // (1)
public interface CertificadoFiscalRepository extends JpaRepository<CertificadoFiscal, Long> { // (2)

    /**
     * Busca el certificado asociado a una compra concreta.
     * USO: Botón "Descargar Certificado" en el detalle del pedido.
     *
     * @param compraId ID de la compra.
     * @return Optional (puede que la compra no tenga donación y por tanto no tenga certificado).
     */
    Optional<CertificadoFiscal> findByCompra_Id(Long compraId); // (3)

    /**
     * Busca un certificado por su código único oficial.
     * USO: Auditoría o validación de autenticidad (QR en el PDF).
     *
     * @param numeroSerie El código único (ej: "2025-DON-999").
     * @return Optional con el documento.
     */
    Optional<CertificadoFiscal> findByNumeroSerie(String numeroSerie); // (4)

    /**
     * Recupera todos los certificados emitidos para un perfil fiscal concreto.
     * USO: "Mi Cuenta -> Mis Certificados". El usuario quiere ver todo lo que ha donado
     * con el NIF "12345678Z".
     *
     * @param datosFiscalesId ID de los datos fiscales.
     * @return Lista de certificados.
     */
    List<CertificadoFiscal> findByDatosFiscales_Id(Long datosFiscalesId); // (5)
}