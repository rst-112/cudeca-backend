package com.cudeca.repository;

import com.cudeca.model.negocio.AjustePrecio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar los descuentos, recargos y modificaciones de precio.
 * Permite auditar por qué cambió el precio final de una compra.
 */
@Repository
public interface AjustePrecioRepository extends JpaRepository<AjustePrecio, Long> {

    /**
     * Recupera todos los ajustes aplicados a una compra completa.
     * USO: En el detalle del pedido, para mostrar el desglose:
     * "Subtotal: 50€ | Descuento Navidad: -5€ | Total: 45€".
     *
     * @param compraId ID de la compra.
     * @return Lista de ajustes (globales y por ítem).
     */
    List<AjustePrecio> findByCompra_Id(Long compraId);

    /**
     * Recupera los ajustes específicos de un artículo concreto.
     * USO: Si una entrada tenía precio especial (ej: "Entrada Joven"), el ajuste
     * está vinculado a esa línea concreta, no a toda la compra.
     *
     * @param articuloId ID del artículo (item_id).
     * @return Lista de ajustes de ese ítem.
     */
    List<AjustePrecio> findByArticuloCompra_Id(Long articuloId);

    /**
     * REPORTING: Busca ajustes por tipo.
     * USO: El departamento de marketing quiere saber cuántas veces se ha usado
     * el descuento "PROMO_VERANO_2025".
     */
    List<AjustePrecio> findByTipo(String tipo);
}
