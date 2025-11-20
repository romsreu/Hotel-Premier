package ar.utn.hotel.model;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class EstadoHabitacion {
    private int idEstado;
    private Date fechaDesde;
    private Date fechaHasta;
    private String estado;
}
