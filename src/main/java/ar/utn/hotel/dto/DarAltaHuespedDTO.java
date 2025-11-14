package ar.utn.hotel.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DarAltaHuespedDTO {

    private String nombre;
    private String apellido;
    private String telefono;

    private String numeroDocumento;
    private String tipoDocumento;
    private String posicionIVA;
    private String fechaNacimiento;
    private String ocupacion;
    private String nacionalidad;
    private String email;
    private String cuit;

    private String calle;
    private String numero;
    private String depto;
    private String piso;
    private String codPostal;
    private String localidad;
    private String provincia;
    private String pais;
}
