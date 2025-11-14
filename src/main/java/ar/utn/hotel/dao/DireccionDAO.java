package ar.utn.hotel.dao;

import ar.utn.hotel.model.Direccion;

public interface DireccionDAO {

    Direccion guardar(Direccion direccion);

    Direccion buscarPorDatos(
            String calle, String numero, String depto, String piso,
            String codPostal, String localidad, String provincia, String pais
    );
}