package ar.utn.hotel.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HuespedDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String numeroDocumento;
    private String tipoDocumento;
    private String telefono;
    private String email;
    private String cuit;
    private String posicionIVA;
    private String fechaNacimiento;
    private String ocupacion;
    private String nacionalidad;
    private Long idDireccion;
}