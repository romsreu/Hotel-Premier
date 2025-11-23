package utils;

import ar.utn.hotel.model.Huesped;
import java.util.List;

public class DataTransfer {
    private static List<Huesped> huespedesEnBusqueda;

    public static void setHuespedesEnBusqueda(List<Huesped> huespedes) {
        DataTransfer.huespedesEnBusqueda = huespedes;
    }

    public static List<Huesped> getHuespedesEnBusqueda() {
        return DataTransfer.huespedesEnBusqueda;
    }

    public static void limpiar() {
        DataTransfer.huespedesEnBusqueda = null;
    }
}