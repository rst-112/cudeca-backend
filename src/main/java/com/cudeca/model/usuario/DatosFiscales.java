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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @ToString.Exclude
    private Usuario usuario;

    // --- RELACIÓN CON CERTIFICADOS (Historial de uso) ---
    @OneToMany(mappedBy = "datosFiscales")
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
