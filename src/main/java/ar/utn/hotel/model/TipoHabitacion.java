package ar.utn.hotel.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipos_habitacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoHabitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre; // Ej: "INDIVIDUAL_ESTANDAR"

    @Column(nullable = false, length = 100)
    private String descripcion; // Ej: "Individual Estándar"

    @Column(nullable = false)
    private Double costoNoche;

    @Override
    public String toString() {
        return descripcion;
    }
    @Column(nullable = false)
    private Integer capacidadMaxima;
}