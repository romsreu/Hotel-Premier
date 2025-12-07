package enums;

public enum TipoHabitacion {
    INDIVIDUAL_ESTANDAR("Individual Estándar", 50800.0),
    DOBLE_ESTANDAR("Doble Estándar", 70230.0),
    DOBLE_SUPERIOR("Doble Superior", 90560.0),
    SUPERIOR_FAMILY_PLAN("Superior Family Plan", 110500.0),
    SUITE_DOBLE("Suite Doble", 128600.0);

    private final String descripcion;
    private final double costoNoche;

    TipoHabitacion(String descripcion, double costoNoche) {
        this.descripcion = descripcion;
        this.costoNoche = costoNoche;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getCostoNoche() {
        return costoNoche;
    }

}