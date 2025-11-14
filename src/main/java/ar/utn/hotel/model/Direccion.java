package ar.utn.hotel.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "direccion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direccion")
    private Long id;

    private String calle;
    private String numero;
    private String departamento;
    private String piso;
    private String codPostal;
    private String localidad;
    private String provincia;
    private String pais;
}
