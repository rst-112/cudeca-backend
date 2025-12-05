package com.cudeca.repository;

import com.cudeca.model.negocio.BeneficioSocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar los beneficios activos de una suscripción.
 * Permite aplicar reglas de negocio (descuentos, prioridades) en tiempo real.
 */
@Repository
public interface BeneficioSocioRepository extends JpaRepository<BeneficioSocio, Long> {

    /**
     * Recupera todos los beneficios asociados a una suscripción concreta.
     * USO: Pantalla "Mi Suscripción" -> "Tus ventajas actuales son: ..."
     *
     * @param suscripcionId ID de la suscripción del usuario.
     * @return Lista de beneficios activos.
     */
    List<BeneficioSocio> findBySuscripcion_Id(Long suscripcionId);

    /**
     * Busca un beneficio específico por tipo dentro de una suscripción.
     * USO: En el Checkout (Compra), para verificar si aplica un descuento.
     * Ej: repo.findBySuscripcion_IdAndTipo(id, "DESCUENTO_TIENDA")
     *
     * @param suscripcionId ID de la suscripción.
     * @param tipo          El código del beneficio (ej: "PRIORIDAD_VENTA").
     * @return Optional con el beneficio (si lo tiene).
     */
    Optional<BeneficioSocio> findBySuscripcion_IdAndTipo(Long suscripcionId, String tipo);
}
