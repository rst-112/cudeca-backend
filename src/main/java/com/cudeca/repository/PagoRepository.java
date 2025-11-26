package com.cudeca.repository;


import com.cudeca.model.negocio.Pago;
import com.cudeca.model.enums.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    // Fundamental: Buscar pago por el ID de la pasarela (ej: "PAYPAL-12345")
    Optional<Pago> findByIdTransaccionExterna(String idTransaccionExterna);

    // Buscar todos los pagos de una compra específica
    List<Pago> findByCompraId(Long compraId);

    // Auditoría: Ver pagos rechazados o pendientes
    List<Pago> findByEstado(EstadoPago estado);
}