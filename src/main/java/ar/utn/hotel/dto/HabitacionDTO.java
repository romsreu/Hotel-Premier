package ar.utn.hotel.dto;

import enums.EstadoHabitacion;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitacionDTO {

    private Integer numero;
    private String tipo;
    private Double costoNoche;
    private Integer capacidad;
    private String descripcion;
    private String tipoDescripcion;


    @Builder.Default
    private EstadoHabitacion estado = EstadoHabitacion.DISPONIBLE;

    @Override
    public String toString() {
        return "HabitacionDTO{" +
                "numero=" + numero +
                ", tipo='" + tipo + '\'' +
                ", costoNoche=" + costoNoche +
                ", tipoDescripcion=" + tipoDescripcion +
                '}';
    }
}