package ar.utn.hotel.model;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class TarjetaDebito {
    private int id_medio_pago;
    private String marca;
    private String banco_emisor;
}
