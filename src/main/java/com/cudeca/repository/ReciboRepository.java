package com.cudeca.repository;

import com.cudeca.model.negocio.Recibo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la gestión de Recibos simples de compra.
 * Permite recuperar el comprobante asociado a una transacción.
 */
@Repository
public interface ReciboRepository extends JpaRepository<Recibo, Long> {

    /**
     * Busca el recibo asociado a una compra específica.
     * USO: Cuando el usuario entra en "Mis Pedidos" y hace clic en "Ver Recibo".
     *
     * @param compraId El ID de la compra padre.
     * @return Optional con el recibo (puede no existir si la compra está PENDIENTE o CANCELADA).
     */
    Optional<Recibo> findByCompra_Id(Long compraId);

    /**
     * Verifica si ya existe un recibo para una compra.
     * MEJOR PRÁCTICA: Antes de intentar generar un recibo nuevo, verificamos esto
     * para evitar una excepción de base de datos (Unique Constraint Violation),
     * ya que la relación es 1 a 1 estricta.
     */
    boolean existsByCompra_Id(Long compraId);
}
