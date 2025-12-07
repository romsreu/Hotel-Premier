package ar.utn.hotel.model;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class DetalleFactura {
    private int id_detalle;
    private String concepto;
    private int cantidad;
    private double precio_unitario;
    private double precio_total;

}
