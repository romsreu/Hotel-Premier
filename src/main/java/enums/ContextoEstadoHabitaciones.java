package enums;

public enum ContextoEstadoHabitaciones {
    MOSTRAR("Mostrar Estado de Habitaciones"),
    RESERVAR("Reservar Habitación"),
    OCUPAR("Ocupar Habitación");

    private final String descripcion;

    ContextoEstadoHabitaciones(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}