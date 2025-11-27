package com.cudeca.repository;

import com.cudeca.model.negocio.Compra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio central para la gestión de Compras (Pedidos).
 * Es el corazón del historial de ventas y la analítica.
 */
@Repository // (1)
public interface CompraRepository extends JpaRepository<Compra, Long> { // (2)

    /**
     * Recupera el historial de compras de un usuario registrado.
     * USO: "Mi Cuenta -> Mis Pedidos".
     *
     * @param usuarioId ID del usuario (clave foránea).
     * @return Lista de compras.
     */
    List<Compra> findByUsuario_Id(Long usuarioId); // (3)

    /**
     * MEJOR PRÁCTICA: Versión paginada del historial.
     * Si un usuario es muy fiel y tiene 100 compras, no queremos traerlas todas de golpe.
     * Traemos las 10 últimas ordenadas por fecha.
     */
    Page<Compra> findByUsuario_Id(Long usuarioId, Pageable pageable); // (4)

    /**
     * Busca compras por email de contacto (útil para Invitados).
     * USO: "Recuperar mis entradas" (cuando no tienes cuenta).
     */
    List<Compra> findByEmailContacto(String emailContacto); // (5)

    /**
     * SEGURIDAD: Busca una compra específica asegurando que pertenece a quien la pide.
     * Evita que el usuario con ID=1 acceda a la compra del ID=5 cambiando la URL.
     * SQL: SELECT * FROM compras WHERE id = ? AND usuario_id = ?
     */
    Optional<Compra> findByIdAndUsuario_Id(Long id, Long usuarioId); // (6)

    /**
     * REPORTING: Busca compras en un rango de fechas.
     * USO: Panel de Admin -> "Ver ventas de la semana pasada".
     */
    List<Compra> findByFechaBetween(Instant inicio, Instant fin); // (7)
}