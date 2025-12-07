package ar.utn.hotel.dao.interfaces;

import ar.utn.hotel.dto.HuespedDTO;
import ar.utn.hotel.model.Huesped;

import java.util.List;

public interface HuespedDAO {
    Huesped guardar(Huesped huesped);
    Huesped obtenerPorId(Long id);
    List<Huesped> obtenerTodos();
    void actualizar(Huesped huesped);
    void eliminar(Long id);
    boolean existePorDocumento(String numeroDocumento, String tipoDocumento);
    List<Huesped> buscarHuesped(HuespedDTO dto);
}