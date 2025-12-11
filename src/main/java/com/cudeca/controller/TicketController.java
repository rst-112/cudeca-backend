package com.cudeca.controller;

import com.cudeca.dto.TicketDTO;
import com.cudeca.model.negocio.EntradaEmitida;
import com.cudeca.repository.EntradaEmitidaRepository;
import com.cudeca.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Gestión segura de tickets")
public class TicketController {

    private final TicketService ticketService;
    private final EntradaEmitidaRepository entradaRepository;

    /**
     * Descarga el PDF de una entrada.
     * SEGURIDAD: Busca los datos en BD usando el ID, no acepta DTOs externos.
     */
    @GetMapping("/{entradaId}/pdf")
    @Operation(summary = "Descargar PDF oficial")
    public ResponseEntity<byte[]> descargarTicketPDF(@PathVariable Long entradaId) {
        try {
            // 1. Buscar datos reales en BD (Fuente de la verdad)
            EntradaEmitida entrada = entradaRepository.findById(entradaId)
                    .orElseThrow(() -> new IllegalArgumentException("Entrada no encontrada"));

            // 2. Mapear Entidad -> DTO (Lógica segura en el backend)
            TicketDTO ticketDTO = mapearEntradaADTO(entrada);

            // 3. Generar PDF
            byte[] pdfBytes = ticketService.generarTicketPdf(ticketDTO);

            String nombreArchivo = "ticket-" + entrada.getCodigoQR() + ".pdf";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(nombreArchivo, StandardCharsets.UTF_8).build());
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Endpoint administrativo para re-enviar correos perdidos
    @PostMapping("/{entradaId}/reenviar-email")
    @PreAuthorize("hasRole('ADMIN')") // Solo admins pueden forzar reenvío
    public ResponseEntity<?> reenviarEmail(@PathVariable Long entradaId) {
        try {
            EntradaEmitida entrada = entradaRepository.findById(entradaId)
                    .orElseThrow(() -> new IllegalArgumentException("Entrada no encontrada"));

            // Reconstruimos el DTO con datos fiables de la BD
            TicketDTO ticketDTO = mapearEntradaADTO(entrada);

            // Usamos el servicio existente
            ticketService.generarYEnviarTicket(ticketDTO);

            return ResponseEntity.ok().body("Correo reenviado correctamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("Entrada no encontrada");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error enviando correo");
        }
    }

    // Método auxiliar de mapeo (Esto extrae los datos reales de las relaciones JPA)
    private TicketDTO mapearEntradaADTO(EntradaEmitida entrada) {
        var articulo = entrada.getArticuloEntrada();
        var compra = articulo.getCompra();
        var evento = articulo.getTipoEntrada().getEvento();
        var usuario = compra.getUsuario();

        String nombreUser = (usuario != null) ? usuario.getNombre() : "Invitado";
        String emailUser = (usuario != null) ? usuario.getEmail() : compra.getEmailContacto();

        // Construir el DTO con datos de BD
        return TicketDTO.builder()
                .nombreEvento(evento.getNombre())
                .lugarEvento(evento.getLugar())
                .fechaEventoFormato(evento.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .descripcionEvento(evento.getDescripcion())
                .nombreUsuario(nombreUser)
                .emailUsuario(emailUser)
                .codigoAsiento(articulo.getAsiento() != null ? articulo.getAsiento().getCodigoEtiqueta() : "General")
                .fila(articulo.getAsiento() != null ? articulo.getAsiento().getFila() : null)
                .columna(articulo.getAsiento() != null ? articulo.getAsiento().getColumna() : null)
                .zonaRecinto(articulo.getAsiento() != null ? articulo.getAsiento().getZona().getNombre() : "General")
                .codigoQR(entrada.getCodigoQR())
                .tipoEntrada(articulo.getTipoEntrada().getNombre())
                .precio(articulo.getPrecioUnitario() + "€")
                .build();
    }
}