package com.cudeca.model.evento;

import com.cudeca.enums.EstadoEvento;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String descripcion;
    @Temporal(TemporalType.DATE) //cambiar a el que se eliga al final
    private LocalDate fechaInicio;
    @Temporal(TemporalType.DATE)
    private LocalDate fechaFin;
    private String lugar;
    @Enumerated(EnumType.STRING)
    private EstadoEvento estado;
    private BigDecimal objetivoRecaudacion;
    private String imagenUrl;

    //relaciones

    @OneToMany(mappedBy = "eventoAsociado")
    private List<TipoEntrada> tiposDeEntradas;

    @OneToMany(mappedBy = "evento")
    private List<ReglaPrecio> reglasDePrecios;

    @OneToMany(mappedBy = "eventoAsociadoAZonaRecinto")
    private List<ZonaRecinto> zonasRecinto;

    @OneToMany(mappedBy = "eventoAsociadoAImagenes")
    private List<ImagenEvento> imagenesEvento;



    //crear metodos

    public void publicar(){
        this.estado = EstadoEvento.PUBLICADO;
    }

    public void cancelar(){
        this.estado = EstadoEvento.CANCELADO;
    }

    public void actualizarRecaudacion(){
        BigDecimal recaudacionTotal = BigDecimal.ZERO;
        for(TipoEntrada tipoEntrada : tiposDeEntradas){
            recaudacionTotal= recaudacionTotal.add(tipoEntrada.getPrecioTotal());
        }
        // Aquí podrías almacenar o utilizar la recaudación total según sea necesario
    }


}
