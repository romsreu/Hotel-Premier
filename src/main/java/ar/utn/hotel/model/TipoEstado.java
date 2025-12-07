package ar.utn.hotel.model;

import enums.EstadoHab;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_estado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, unique = true)
    private EstadoHab estado;
}