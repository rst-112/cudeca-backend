package com.cudeca.service.impl;

import com.cudeca.model.usuario.DatosFiscales;
import com.cudeca.model.usuario.Usuario;
import com.cudeca.repository.DatosFiscalesRepository;
import com.cudeca.repository.UsuarioRepository;
import com.cudeca.service.DatosFiscalesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Implementación del servicio de gestión de datos fiscales.
 * Maneja la libreta de direcciones de facturación de los usuarios.
 */
@Service
@Transactional
public class DatosFiscalesServiceImpl implements DatosFiscalesService {

    private static final Logger log = LoggerFactory.getLogger(DatosFiscalesServiceImpl.class);

    // Patrón básico para validar NIF español (DNI/NIE/CIF)
    private static final Pattern NIF_PATTERN = Pattern.compile(
            "^[0-9XYZ][0-9]{7}[A-Z]$|^[A-Z][0-9]{7}[0-9A-Z]$"
    );

    private final DatosFiscalesRepository datosFiscalesRepository;
    private final UsuarioRepository usuarioRepository;

    public DatosFiscalesServiceImpl(
            DatosFiscalesRepository datosFiscalesRepository,
            UsuarioRepository usuarioRepository) {
        this.datosFiscalesRepository = datosFiscalesRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public DatosFiscales crearDatosFiscales(DatosFiscales datosFiscales, Long usuarioId) {
        log.info("Creando datos fiscales para usuario ID: {}", usuarioId);

        // Validar datos
        validarDatosFiscales(datosFiscales);

        // Verificar que el usuario existe
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));

        // Asociar usuario
        datosFiscales.setUsuario(usuario);

        // Guardar
        DatosFiscales guardado = datosFiscalesRepository.save(datosFiscales);
        if (log.isInfoEnabled()) {
            log.info("Datos fiscales creados con ID: {}", guardado.getId());
        }

        return guardado;
    }

    @Override
    public DatosFiscales actualizarDatosFiscales(Long id, DatosFiscales datosFiscales, Long usuarioId) {
        log.info("Actualizando datos fiscales ID: {} para usuario ID: {}", id, usuarioId);

        // Buscar datos fiscales existentes
        DatosFiscales existente = datosFiscalesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Datos fiscales no encontrados: " + id));

        // Verificar propiedad
        if (!existente.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("Los datos fiscales no pertenecen al usuario indicado");
        }

        // Validar nuevos datos
        validarDatosFiscales(datosFiscales);

        // Actualizar campos
        existente.setNombreCompleto(datosFiscales.getNombreCompleto());
        existente.setNif(datosFiscales.getNif());
        existente.setDireccion(datosFiscales.getDireccion());
        existente.setPais(datosFiscales.getPais());

        // Guardar
        DatosFiscales actualizado = datosFiscalesRepository.save(existente);
        log.info("Datos fiscales actualizados ID: {}", id);

        return actualizado;
    }

    @Override
    public boolean eliminarDatosFiscales(Long id, Long usuarioId) {
        log.info("Eliminando datos fiscales ID: {} para usuario ID: {}", id, usuarioId);

        // Buscar datos fiscales
        DatosFiscales datosFiscales = datosFiscalesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Datos fiscales no encontrados: " + id));

        // Verificar propiedad
        if (!datosFiscales.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("Los datos fiscales no pertenecen al usuario indicado");
        }

        // Eliminar
        datosFiscalesRepository.delete(datosFiscales);
        log.info("Datos fiscales eliminados ID: {}", id);

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DatosFiscales> obtenerDatosFiscalesPorUsuario(Long usuarioId) {
        log.debug("Obteniendo datos fiscales para usuario ID: {}", usuarioId);
        return datosFiscalesRepository.findByUsuario_Id(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DatosFiscales> obtenerDatosFiscalesPorId(Long id, Long usuarioId) {
        log.debug("Obteniendo datos fiscales ID: {} para usuario ID: {}", id, usuarioId);

        return datosFiscalesRepository.findById(id)
                .filter(df -> df.getUsuario().getId().equals(usuarioId));
    }

    @Override
    public boolean validarNIF(String nif) {
        if (nif == null || nif.isBlank()) {
            return false;
        }

        // Normalizar: convertir a mayúsculas y eliminar espacios
        String nifNormalizado = nif.trim().toUpperCase();

        // Validar formato básico
        if (!NIF_PATTERN.matcher(nifNormalizado).matches()) {
            return false;
        }

        // Validación adicional de letra de control para DNI/NIE
        return validarLetraControlDNI(nifNormalizado);
    }

    // --- MÉTODOS PRIVADOS DE APOYO ---

    private void validarDatosFiscales(DatosFiscales datosFiscales) {
        if (datosFiscales == null) {
            throw new IllegalArgumentException("Los datos fiscales no pueden ser nulos");
        }

        if (datosFiscales.getNombreCompleto() == null || datosFiscales.getNombreCompleto().isBlank()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio");
        }

        if (datosFiscales.getNif() == null || datosFiscales.getNif().isBlank()) {
            throw new IllegalArgumentException("El NIF es obligatorio");
        }

        if (!validarNIF(datosFiscales.getNif())) {
            throw new IllegalArgumentException("El NIF no es válido");
        }

        if (datosFiscales.getDireccion() == null || datosFiscales.getDireccion().isBlank()) {
            throw new IllegalArgumentException("La dirección es obligatoria");
        }

        if (datosFiscales.getPais() == null || datosFiscales.getPais().isBlank()) {
            throw new IllegalArgumentException("El país es obligatorio");
        }
    }

    private boolean validarLetraControlDNI(String nif) {
        // Tabla de letras de control para DNI
        String letras = "TRWAGMYFPDXBNJZSQVHLCKE";

        try {
            // Extraer número y letra
            String numero;
            char letraProporcionada;

            if (nif.matches("^\\d{8}[A-Z]$")) {
                // DNI estándar
                numero = nif.substring(0, 8);
                letraProporcionada = nif.charAt(8);
                log.debug("Validando DNI: nif={}, numero={}, letra={}", nif, numero, letraProporcionada);
            } else if (nif.matches("^[XYZ]\\d{7}[A-Z]$")) {
                // NIE: Se reemplaza X=0, Y=1, Z=2 y se calcula con los 7 dígitos + letra
                String primeraLetra = nif.substring(0, 1);
                // Extraer los 7 dígitos del NIE (posiciones 1-7, total 7 dígitos)
                String digitosNIE = nif.substring(1, 8);

                // Convertir primera letra a número: X=0, Y=1, Z=2
                String digitoInicial = switch (primeraLetra) {
                    case "X" -> "0";
                    case "Y" -> "1";
                    case "Z" -> "2";
                    default -> "0";
                };

                // Formar el número completo: digitoInicial (1 dígito) + digitosNIE (7 dígitos) = 8 dígitos
                numero = digitoInicial + digitosNIE;
                letraProporcionada = nif.charAt(8);
                log.debug("Validando NIE: nif={}, digitosNIE={}, numero={}, letra={}",
                         nif, digitosNIE, numero, letraProporcionada);
            } else {
                // CIF u otro formato - aceptar sin validación adicional
                log.debug("Validando CIF u otro formato: nif={}", nif);
                return true;
            }

            // Calcular letra correcta
            int numeroEntero = Integer.parseInt(numero);
            int resto = numeroEntero % 23;
            char letraCorrecta = letras.charAt(resto);
            boolean valido = letraCorrecta == letraProporcionada;

            log.info("VALIDACIÓN NIE/DNI: nif={}, numero={}, numeroEntero={}, resto={}, letraCorrecta={}, letraProporcionada={}, valido={}",
                     nif, numero, numeroEntero, resto, letraCorrecta, letraProporcionada, valido);

            return valido;

        } catch (Exception e) {
            log.warn("Error al validar letra de control del NIF: {}", nif, e);
            return false;
        }
    }
}

