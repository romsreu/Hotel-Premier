package enums;

public enum EstadoHabitacion {
    DISPONIBLE("Disponible"),
    OCUPADA("Ocupada"),
    MANTENIMIENTO("En Mantenimiento"),
    RESERVADA("Reservada");

    private final String descripcion;

    EstadoHabitacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}