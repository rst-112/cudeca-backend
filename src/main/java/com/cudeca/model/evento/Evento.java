package com.cudeca.model.evento;

import com.cudeca.model.evento.enums.EstadoEvento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String descripcion;
    @Temporal(TemporalType.DATE) //cambiar a el que se eliga al final
    private Date fechaInicio;
    @Temporal(TemporalType.DATE)
    private Date fechaFin;
    private String lugar;
    @Enumerated(EnumType.STRING)
    private EstadoEvento estado;
    private BigDecimal objetivoRecaudacion;
    private String imagenUrl;

    //relaciones

    @OneToMany(mappedBy = "eventoAsociado")
    private List<TipoEntrada> tiposDeEntradas;



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
