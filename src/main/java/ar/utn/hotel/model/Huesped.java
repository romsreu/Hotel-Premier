package ar.utn.hotel.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "huesped")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyJoinColumn(name = "id_persona")
public class Huesped extends Persona {

    private String numeroDocumento;
    private String tipoDocumento;
    private String posicionIVA;
    private String fechaNacimiento;
    private String ocupacion;
    private String nacionalidad;
    private String email;
    private String cuit;

    // Constructor para clonar Persona
    public Huesped(Persona p) {
        super(p.getId(), p.getNombre(), p.getApellido(), p.getTelefono(), p.getDireccion());
    }
}
