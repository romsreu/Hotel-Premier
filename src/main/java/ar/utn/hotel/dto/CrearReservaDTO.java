package ar.utn.hotel.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearReservaDTO {
    private Long idHuesped;
    private Integer numeroHabitacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer cantHuespedes;
    private Double descuento;
}