package com.cudeca.service;

import com.cudeca.dto.DatosFiscalesDTO;

import java.util.List;

public interface DatosFiscalesService {

    List<DatosFiscalesDTO> obtenerDatosFiscalesPorUsuario(Long usuarioId);

    DatosFiscalesDTO obtenerPorId(Long id, Long usuarioId);

    DatosFiscalesDTO crearDatosFiscales(Long usuarioId, DatosFiscalesDTO dto);

    DatosFiscalesDTO actualizarDatosFiscales(Long id, Long usuarioId, DatosFiscalesDTO dto);

    void eliminarDatosFiscales(Long id, Long usuarioId);

    boolean validarNIF(String nif);
}