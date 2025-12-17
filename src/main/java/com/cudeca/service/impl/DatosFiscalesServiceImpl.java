package com.cudeca.service.impl;

import com.cudeca.dto.DatosFiscalesDTO;
import com.cudeca.model.usuario.DatosFiscales;
import com.cudeca.model.usuario.Usuario;
import com.cudeca.repository.DatosFiscalesRepository;
import com.cudeca.repository.UsuarioRepository;
import com.cudeca.service.DatosFiscalesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DatosFiscalesServiceImpl implements DatosFiscalesService {

    private final DatosFiscalesRepository datosFiscalesRepository;
    private final UsuarioRepository usuarioRepository;

    // Patrón para validación extra si es necesario
    private static final Pattern NIF_PATTERN = Pattern.compile("^[0-9XYZ][0-9]{7}[A-Z]$|^[A-Z][0-9]{7}[0-9A-Z]$");

    @Override
    @Transactional(readOnly = true)
    public List<DatosFiscalesDTO> obtenerDatosFiscalesPorUsuario(Long usuarioId) {
        log.debug("Obteniendo datos fiscales para usuario ID: {}", usuarioId);
        return datosFiscalesRepository.findByUsuario_Id(usuarioId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DatosFiscalesDTO obtenerPorId(Long id, Long usuarioId) {
        // CORRECCIÓN: Usar 'datosFiscalesRepository' en lugar de 'repository'
        DatosFiscales entidad = datosFiscalesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada"));

        // Seguridad: Validar que el dato pertenece al usuario que lo pide
        if (!entidad.getUsuario().getId().equals(usuarioId)) {
            throw new SecurityException("No tienes permiso para acceder a este dato fiscal");
        }

        return convertirADTO(entidad);
    }

    @Override
    public DatosFiscalesDTO crearDatosFiscales(Long usuarioId, DatosFiscalesDTO dto) {
        log.info("Creando datos fiscales para usuario ID: {}", usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!validarNIF(dto.getNif())) {
            throw new IllegalArgumentException("El NIF/CIF proporcionado no es válido: " + dto.getNif());
        }

        DatosFiscales entidad = new DatosFiscales();
        entidad.setUsuario(usuario);
        // Mapeo de campos nuevos y existentes
        mapearDatos(entidad, dto);

        return convertirADTO(datosFiscalesRepository.save(entidad));
    }

    @Override
    public DatosFiscalesDTO actualizarDatosFiscales(Long id, Long usuarioId, DatosFiscalesDTO dto) {
        log.info("Actualizando datos fiscales ID: {}", id);

        DatosFiscales entidad = datosFiscalesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada"));

        if (!entidad.getUsuario().getId().equals(usuarioId)) {
            throw new SecurityException("No tienes permiso para modificar esta dirección");
        }

        if (!validarNIF(dto.getNif())) {
            throw new IllegalArgumentException("El NIF/CIF proporcionado no es válido");
        }

        mapearDatos(entidad, dto);

        return convertirADTO(datosFiscalesRepository.save(entidad));
    }

    @Override
    public void eliminarDatosFiscales(Long id, Long usuarioId) {
        log.info("Eliminando datos fiscales ID: {}", id);

        DatosFiscales entidad = datosFiscalesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada"));

        if (!entidad.getUsuario().getId().equals(usuarioId)) {
            throw new SecurityException("No tienes permiso para eliminar esta dirección");
        }

        datosFiscalesRepository.delete(entidad);
    }

    // --- MAPEO Y UTILIDADES ---

    private void mapearDatos(DatosFiscales entidad, DatosFiscalesDTO dto) {
        entidad.setNombreCompleto(dto.getNombreCompleto());
        entidad.setNif(dto.getNif().toUpperCase(java.util.Locale.ROOT));
        entidad.setDireccion(dto.getDireccion());
        entidad.setCiudad(dto.getCiudad());
        entidad.setCodigoPostal(dto.getCodigoPostal());
        entidad.setPais(dto.getPais());
        entidad.setAlias(dto.getAlias());
    }

    private DatosFiscalesDTO convertirADTO(DatosFiscales e) {
        DatosFiscalesDTO dto = new DatosFiscalesDTO();
        dto.setId(e.getId());
        dto.setNombreCompleto(e.getNombreCompleto());
        dto.setNif(e.getNif());
        dto.setDireccion(e.getDireccion());
        dto.setCiudad(e.getCiudad());
        dto.setCodigoPostal(e.getCodigoPostal());
        dto.setPais(e.getPais());
        dto.setAlias(e.getAlias());
        return dto;
    }

    @Override
    public boolean validarNIF(String nif) {
        if (nif == null || nif.isBlank()) return false;
        String nifNorm = nif.trim().toUpperCase(java.util.Locale.ROOT);
        return NIF_PATTERN.matcher(nifNorm).matches() || nifNorm.length() >= 5;
    }
}