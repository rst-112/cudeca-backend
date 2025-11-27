package com.cudeca.repository;

import com.cudeca.model.negocio.ArticuloCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar las líneas de pedido (Artículos).
 * Maneja POLIMORFISMO: Sirve para ArticuloEntrada, ArticuloDonacion y ArticuloSorteo.
 */
@Repository // (1)
public interface ArticuloCompraRepository extends JpaRepository<ArticuloCompra, Long> { // (2)

    /**
     * Recupera todos los artículos asociados a una compra específica.
     * USO: Cuando cargas una compra, quieres ver qué tiene dentro.
     * Aunque la clase Compra ya tiene la lista, a veces es útil buscar desde aquí por rendimiento.
     *
     * @param compraId ID de la compra padre.
     * @return Lista de artículos (mezclados: entradas, donaciones...).
     */
    List<ArticuloCompra> findByCompra_Id(Long compraId); // (3)

    /**
     * MEJOR PRÁCTICA: Consulta JPQL para filtrar por tipo concreto.
     * USO: Panel de Admin -> "¿Cuántas DONACIONES se han hecho hoy?".
     * "TYPE(a)" es una función específica de JPA para leer la columna discriminadora.
     */
    @Query("SELECT a FROM ArticuloCompra a WHERE TYPE(a) = ArticuloDonacion")
    List<ArticuloCompra> findAllDonaciones(); // (4)

    /**
     * MEJOR PRÁCTICA: Consulta para contar entradas vendidas.
     * USO: Control de aforo o estadísticas.
     */
    @Query("SELECT SUM(a.cantidad) FROM ArticuloEntrada a")
    Long countTotalEntradasVendidas(); // (5)
}