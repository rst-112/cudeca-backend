package com.cudeca.model.evento;

import com.cudeca.enums.EstadoAsiento;
// Asegúrate de importar el Enum correcto. Si está en model.enums, cámbialo.
// import com.cudeca.model.enums.EstadoAsiento;

import com.cudeca.model.negocio.ArticuloEntrada;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ASIENTOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Añadido para poder usar .builder() como en el resto del proyecto
public class Asiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigoEtiqueta;
    private Integer fila;
    private Integer columna;

    @Enumerated(EnumType.STRING)
    private EstadoAsiento estado;

    // --- RELACIONES ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id") // <--- ¡ESTE ES EL CAMBIO! (Antes Hibernate buscaba zona_recinto_id)
    @ToString.Exclude
    private ZonaRecinto zonaRecinto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_entrada_id") // Nombre explícito de la columna en BD
    @ToString.Exclude
    private TipoEntrada tipoEntradaAsignada;

    // --- CORRECCIÓN CRÍTICA AQUÍ ---
    // Antes: @ManyToMany -> ERROR (No casa con el @ManyToOne de ArticuloEntrada)
    // Ahora: @OneToMany -> CORRECTO
    @OneToMany(mappedBy = "asiento", cascade = CascadeType.ALL)
    @Builder.Default // Para quitar el warning amarillo de Lombok
    @ToString.Exclude // Para evitar StackOverflow
    private List<ArticuloEntrada> articulosEntrada = new ArrayList<>();

    // --- MÉTODOS ---

    public void reservar(){
        this.estado = EstadoAsiento.RESERVADO;
    }

    public void liberar(){
        this.estado = EstadoAsiento.DISPONIBLE;
    }

    // Método helper opcional para mantener la coherencia
    public void addArticuloEntrada(ArticuloEntrada articulo) {
        this.articulosEntrada.add(articulo);
        articulo.setAsiento(this);
    }
}