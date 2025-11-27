package com.cudeca.repository;

import com.cudeca.model.negocio.Notificacion;
import com.cudeca.enums.EstadoNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la cola de correos electrónicos.
 * Permite gestionar el envío asíncrono y los reintentos ante fallos.
 */
@Repository // (1)
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> { // (2)

    /**
     * Recupera notificaciones por estado.
     * USO CRÍTICO: Un proceso en segundo plano (Cron Job) ejecuta esto cada minuto:
     * "Dame todas las notificaciones que estén PENDIENTE o en ERROR para intentar enviarlas".
     *
     * @param estado Estado del envío.
     * @return Lista de emails en cola.
     */
    List<Notificacion> findByEstado(EstadoNotificacion estado); // (3)

    /**
     * Recupera el historial de notificaciones de un usuario.
     * USO: Soporte técnico. "¿Le llegó el correo de bienvenida a Pepito?".
     *
     * @param usuarioId ID del usuario.
     * @return Historial de comunicaciones.
     */
    List<Notificacion> findByUsuario_Id(Long usuarioId); // (4)

    /**
     * Recupera notificaciones asociadas a una compra.
     * USO: Verificar si ya se envió el correo con las entradas.
     *
     * @param compraId ID de la compra.
     * @return Emails relacionados con ese pedido.
     */
    List<Notificacion> findByCompra_Id(Long compraId); // (5)
}