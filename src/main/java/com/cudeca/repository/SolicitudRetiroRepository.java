package com.cudeca.repository;

import com.cudeca.model.negocio.SolicitudRetiro;
import com.cudeca.enums.EstadoRetiro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar las solicitudes de retirada de fondos del monedero.
 * Esencial para el panel de administración financiera.
 */
@Repository // (1)
public interface SolicitudRetiroRepository extends JpaRepository<SolicitudRetiro, Long> { // (2)

    /**
     * Recupera solicitudes filtradas por estado.
     * USO PRINCIPAL: El Admin necesita ver todas las que están 'PENDIENTE' para procesarlas.
     *
     * @param estado El estado a buscar (ej: PENDIENTE).
     * @return Lista de solicitudes que coinciden.
     */
    List<SolicitudRetiro> findByEstado(EstadoRetiro estado); // (3)

    /**
     * Recupera el historial de solicitudes de un usuario específico.
     * USO PRINCIPAL: El usuario entra en "Mi Cuenta -> Mis Retiros".
     *
     * @param usuarioId ID del usuario solicitante.
     * @return Lista de solicitudes de ese usuario.
     */
    List<SolicitudRetiro> findByUsuario_Id(Long usuarioId); // (4)

    /**
     * MEJOR PRÁCTICA: Versión paginada para el Back-Office.
     * Permite al admin navegar por el historial de miles de solicitudes procesadas/rechazadas
     * sin cargar todo la base de datos en memoria.
     *
     * @param estado El estado (ej: PROCESADA).
     * @param pageable Configuración de página (página 1, tamaño 50).
     * @return Página de resultados.
     */
    Page<SolicitudRetiro> findByEstado(EstadoRetiro estado, Pageable pageable); // (5)
}