package ar.utn.hotel.model;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class DetalleReserva {
    private int id_detalle_reserva;
    private Date fecha_inicio;
    private Date fecha_fin;
    private int descuento;
}
