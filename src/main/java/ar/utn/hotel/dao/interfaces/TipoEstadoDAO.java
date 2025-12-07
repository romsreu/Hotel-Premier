package ar.utn.hotel.dao.interfaces;

import ar.utn.hotel.model.TipoEstado;
import enums.EstadoHab;
import java.util.List;

public interface TipoEstadoDAO {
    TipoEstado guardar(TipoEstado tipoEstado);
    TipoEstado buscarPorId(Integer id);
    TipoEstado buscarPorEstado(EstadoHab estado);
    List<TipoEstado> listarTodos();
    void actualizar(TipoEstado tipoEstado);
    void eliminar(Integer id);
    boolean existeEstado(EstadoHab estado);
}