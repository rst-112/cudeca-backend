package com.cudeca.repository;

import com.cudeca.model.enums.EstadoPago;
import com.cudeca.model.negocio.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de transacciones económicas (Pagos).
 * Fundamental para la conciliación bancaria y la integración con pasarelas.
 */
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    /**
     * Busca un pago por el ID de la transacción de la pasarela externa.
     * CRÍTICO: Cuando PayPal/Redsys llama al Webhook, envía ESTE código (ej: "PAY-12345").
     * Necesitamos encontrar el pago rápidamente para marcarlo como APROBADO.
     *
     * @param idTransaccionExterna El ID proporcionado por la pasarela.
     * @return Optional con el pago (debe ser único).
     */
    Optional<Pago> findByIdTransaccionExterna(String idTransaccionExterna);

    /**
     * Recupera todos los pagos asociados a una compra.
     * Útil porque una compra podría tener varios intentos de pago fallidos antes del bueno.
     *
     * @param compraId ID de la compra.
     * @return Lista de intentos de pago.
     */
    List<Pago> findByCompra_Id(Long compraId);

    /**
     * Recupera el historial de pagos de una suscripción recurrente.
     * Útil para que el socio vea: "Cuota Enero: Pagada", "Cuota Febrero: Rechazada".
     *
     * @param suscripcionId ID de la suscripción.
     * @return Lista de cuotas pagadas (o intentadas).
     */
    List<Pago> findBySuscripcion_Id(Long suscripcionId);

    /**
     * Busca pagos por su estado.
     * Útil para procesos batch (tareas programadas) que buscan pagos "PENDIENTE"
     * que se han quedado colgados para cancelarlos por timeout.
     */
    List<Pago> findByEstado(EstadoPago estado);
}
