package com.cudeca.repository;

import com.cudeca.model.negocio.ValidacionEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ValidacionEntradaRepository extends JpaRepository<ValidacionEntrada, Long> {

    List<ValidacionEntrada> findByEntradaEmitida_Id(Long entradaEmitidaId);

    List<ValidacionEntrada> findByEntradaEmitida_IdAndRevertidaFalse(Long entradaEmitidaId);

    List<ValidacionEntrada> findByPersonalValidador_IdAndFechaHoraBetween(Long personalValidadorId, OffsetDateTime inicio, OffsetDateTime fin);
}
