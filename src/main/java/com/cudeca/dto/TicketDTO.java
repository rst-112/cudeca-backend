package com.cudeca.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para transferencia de datos necesarios para generar un ticket PDF.
 * Contiene información del evento, usuario y asiento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDTO {

    // --- DATOS DEL EVENTO ---
    private String nombreEvento;
    private String lugarEvento;
    private String fechaEventoFormato; // Formato: "dd/MM/yyyy HH:mm"
    private String descripcionEvento;
    private String imagenEventoUrl;

    // --- DATOS DEL USUARIO ---
    private String nombreUsuario;
    private String emailUsuario;

    // --- DATOS DEL ASIENTO ---
    private String codigoAsiento;
    private Integer fila;
    private Integer columna;
    private String zonaRecinto;

    // --- DATOS PARA EL QR ---
    // Código único que irá en el QR (puede ser un token o ID único de la entrada)
    private String codigoQR;

    // --- OPCIONAL: INFORMACIÓN ADICIONAL ---
    private String tipoEntrada;
    private String precio;
}

