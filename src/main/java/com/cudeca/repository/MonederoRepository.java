package com.cudeca.repository;

import com.cudeca.model.negocio.Monedero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestionar los Monederos virtuales de los socios/compradores.
 * Permite consultar saldo y realizar operaciones financieras.
 */
@Repository // (1)
public interface MonederoRepository extends JpaRepository<Monedero, Long> { // (2)

    /**
     * Busca el monedero asociado a un Comprador específico.
     *
     * @param compradorId El ID del usuario/comprador (clave foránea usuario_id).
     * @return Optional con el monedero si el usuario tiene uno activo.
     */
    // (3) Spring Data interpreta esto como:
    // "Busca en la entidad Monedero, entra en el campo 'comprador', y compara su 'id'".
    Optional<Monedero> findByComprador_Id(Long compradorId);

    // NOTA: Si en tu entidad Monedero llamaste al campo 'usuario' en vez de 'comprador',
    // el método debería llamarse 'findByUsuario_Id'.
    // Como acordamos usar 'private Comprador comprador;', usamos la versión de arriba.
}