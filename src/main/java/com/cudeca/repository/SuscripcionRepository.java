package com.cudeca.repository;

import com.cudeca.model.negocio.Suscripcion;
import com.cudeca.enums.EstadoSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de membresías (Socios).
 * Clave para la financiación recurrente de la fundación.
 */
@Repository // (1)
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> { // (2)

    /**
     * Busca la suscripción activa (o inactiva) de un comprador.
     * USO: Cuando el usuario entra a "Mi Cuenta -> Mi Suscripción".
     * Nota: Usamos 'findByComprador_Id' porque en la entidad Suscripcion
     * llamamos al campo 'private Comprador comprador'.
     *
     * @param compradorId ID del usuario/comprador.
     * @return Optional con la suscripción (un usuario suele tener solo una).
     */
    Optional<Suscripcion> findByComprador_Id(Long compradorId); // (3)

    /**
     * AUTOMATIZACIÓN: Busca suscripciones que caducan antes de una fecha.
     * USO: Un proceso nocturno (Batch) llama a esto cada madrugada:
     * "Dame todas las suscripciones activas que caducan mañana para cobrarles".
     *
     * @param estado Estado actual (ej: ACTIVA).
     * @param fecha Límite de fecha (ej: Instant.now()).
     * @return Lista de candidatos a renovación.
     */
    List<Suscripcion> findByEstadoAndFechaFinBefore(EstadoSuscripcion estado, Instant fecha); // (4)

    /**
     * Busca suscripciones por estado y renovación automática.
     * USO: Para estadísticas o procesos masivos.
     * Ej: "Dime cuántos socios activos tenemos con renovación automática activada".
     */
    long countByEstadoAndRenovacionAutomaticaTrue(EstadoSuscripcion estado); // (5)
}