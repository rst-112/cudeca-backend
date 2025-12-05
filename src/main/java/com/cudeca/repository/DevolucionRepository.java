package com.cudeca.repository;

import com.cudeca.model.enums.TipoDevolucion;
import com.cudeca.model.negocio.Devolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la gestión de reembolsos (Devoluciones).
 * Soporta tanto devoluciones a pasarela (tarjeta) como a monedero interno.
 */
@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {

    /**
     * Recupera todas las devoluciones asociadas a una compra.
     * USO: Cuando se visualiza el detalle de un pedido, para saber si ha sido reembolsado
     * parcial o totalmente.
     *
     * @param compraId ID de la compra padre.
     * @return Lista de devoluciones (puede haber varias parciales).
     */
    List<Devolucion> findByCompra_Id(Long compraId);

    /**
     * Recupera las devoluciones asociadas a un pago específico de pasarela.
     * USO: Conciliación bancaria. "Este cargo en Stripe de 50€, ¿se devolvió?".
     *
     * @param pagoId ID del pago original.
     * @return Lista de devoluciones asociadas a ese pago.
     */
    List<Devolucion> findByPago_Id(Long pagoId);

    /**
     * Filtra devoluciones por tipo (PASARELA o MONEDERO).
     * USO: Informes contables. El admin quiere saber cuánto dinero ha salido "realmente"
     * de la caja (PASARELA) vs cuánto se ha quedado en saldo interno (MONEDERO).
     */
    List<Devolucion> findByTipo(TipoDevolucion tipo);
}
