package ar.utn.hotel.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitacionReservaDTO {
    // Información de la habitación
    private Integer numeroHabitacion;
    private String tipoHabitacion;
    private Double costoNoche;
    private Integer piso;
    private Integer capacidad;

    // Información de la reserva
    private LocalDate fechaIngreso;
    private LocalDate fechaEgreso;
    private Integer cantidadNoches;
    private Double costoTotal;
}