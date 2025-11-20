package ar.utn.hotel.model;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class NotaCredito {
    private int id_nota_credito;
    private int numero;
    private Date fecha;
}
