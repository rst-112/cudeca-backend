package com.cudeca.repository;

import com.cudeca.model.usuario.DatosFiscales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la gestión de los Datos Fiscales (Libreta de Direcciones).
 * Permite a los usuarios guardar múltiples perfiles de facturación (NIFs).
 */
@Repository
public interface DatosFiscalesRepository extends JpaRepository<DatosFiscales, Long> {

    /**
     * Recupera toda la "libreta de direcciones" de un usuario concreto.
     * Se usa en el Checkout para mostrar un desplegable con los NIFs guardados.
     *
     * @param usuarioId El ID del usuario propietario.
     * @return Una lista de perfiles fiscales.
     */
    List<DatosFiscales> findByUsuario_Id(Long usuarioId);
}
