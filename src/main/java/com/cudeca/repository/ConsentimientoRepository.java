package com.cudeca.repository;

import com.cudeca.model.negocio.Consentimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar los consentimientos legales (RGPD) asociados a las compras.
 * Permite verificar qué aceptó el usuario y en qué versión.
 */
@Repository // (1)
public interface ConsentimientoRepository extends JpaRepository<Consentimiento, Long> { // (2)

    /**
     * Recupera todos los consentimientos firmados en una compra.
     * USO: Cuando se consulta el detalle de un pedido, para mostrar:
     * "Términos aceptados: Sí | Marketing: No".
     *
     * @param compraId ID de la compra.
     * @return Lista de consentimientos.
     */
    List<Consentimiento> findByCompra_Id(Long compraId); // (3)

    /**
     * Busca un consentimiento específico por tipo dentro de una compra.
     * USO: Lógica de Negocio. "¿Aceptó este usuario el envío de publicidad en esta compra?".
     * SQL: SELECT * FROM consentimientos WHERE compra_id = ? AND tipo = ?
     *
     * @param compraId ID de la compra.
     * @param tipo El tipo de consentimiento (ej: "MARKETING").
     * @return Optional (porque puede que no exista ese registro para esa compra).
     */
    Optional<Consentimiento> findByCompra_IdAndTipo(Long compraId, String tipo); // (4)

    /**
     * AUDITORÍA: Cuenta cuántos usuarios han aceptado una versión específica.
     * USO: El departamento legal pregunta: "¿Cuánta gente ha aceptado ya la v2.0 de la Política?".
     */
    long countByTipoAndVersionAndOtorgadoTrue(String tipo, String version); // (5)
}