package com.cudeca.repository;

import com.cudeca.model.negocio.EntradaEmitida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestionar entradas emitidas.
 */
@Repository
public interface EntradaEmitidaRepository extends JpaRepository<EntradaEmitida, Long> {

    /**
     * Encuentra una entrada por su código QR.
     */
    Optional<EntradaEmitida> findByCodigoQr(String codigoQr);

    /**
     * Cuenta entradas asociadas a un item de compra.
     */
    long countByItemCompra_Id(Long itemCompraId);
}
