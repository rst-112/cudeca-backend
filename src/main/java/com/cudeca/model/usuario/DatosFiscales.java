package com.cudeca.model.usuario;

import com.cudeca.model.negocio.CertificadoFiscal;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DATOS_FISCALES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatosFiscales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELACIÓN CON USUARIO (Dueño de la libreta) ---
    // SQL: usuario_id (Existe en la tabla, así que ESTA relación se queda)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @ToString.Exclude
    private Usuario usuario;

    // --- RELACIÓN CON CERTIFICADOS (Historial de uso) ---
    // ¡LO QUE PEDÍAS! Un dato fiscal puede generar muchos certificados.
    @OneToMany(mappedBy = "datosFiscales") // No cascade ALL, porque borrar un dato no debe borrar certificados legales
    @Builder.Default
    @ToString.Exclude
    private List<CertificadoFiscal> certificados = new ArrayList<>();

    // --- DATOS ---
    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(nullable = false, length = 20)
    private String nif;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false, length = 100)
    private String pais;
}