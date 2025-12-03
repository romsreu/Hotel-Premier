package ar.utn.hotel.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "habitacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habitacion {

    @Id
    @Column(name = "numero", nullable = false, unique = true)
    private Integer numero;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo_habitacion", nullable = false)
    private TipoHabitacion tipo;

    @Column(name = "piso")
    private Integer piso;

    @OneToMany(mappedBy = "habitacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RegistroEstadoHabitacion> historialEstados = new HashSet<>();

    @ManyToMany(mappedBy = "habitaciones")
    @Builder.Default
    private Set<Reserva> reservas = new HashSet<>();

    // Método auxiliar para obtener el estado actual
    public RegistroEstadoHabitacion getEstadoActual() {
        return historialEstados.stream()
                .filter(RegistroEstadoHabitacion::isActivo)
                .max(Comparator.comparing(RegistroEstadoHabitacion::getFechaDesde))
                .orElse(null);
    }

    // Método auxiliar para obtener el estado en una fecha específica
    public RegistroEstadoHabitacion getEstadoEn(LocalDate fecha) {
        return historialEstados.stream()
                .filter(registro -> registro.isVigenteEn(fecha))
                .findFirst()
                .orElse(null);
    }

    // Método auxiliar para agregar un nuevo estado
    public void agregarEstado(EstadoHabitacion estado, LocalDate fechaDesde) {
        // Cerrar el estado actual
        RegistroEstadoHabitacion estadoActual = getEstadoActual();
        if (estadoActual != null) {
            estadoActual.setFechaHasta(fechaDesde.minusDays(1));
        }

        // Crear y agregar el nuevo registro
        RegistroEstadoHabitacion nuevoRegistro = RegistroEstadoHabitacion.builder()
                .habitacion(this)
                .estado(estado)
                .fechaDesde(fechaDesde)
                .build();

        this.historialEstados.add(nuevoRegistro);
    }

    // Método auxiliar para obtener el costo por noche desde el tipo
    public Double getCostoNoche() {
        return tipo != null ? tipo.getCostoNoche() : null;
    }

    // Método para verificar disponibilidad en un rango de fechas
    public boolean isDisponibleEntre(LocalDate desde, LocalDate hasta) {
        return historialEstados.stream()
                .filter(registro -> {
                    boolean overlap = !registro.getFechaDesde().isAfter(hasta) &&
                            (registro.getFechaHasta() == null || !registro.getFechaHasta().isBefore(desde));
                    return overlap;
                })
                .allMatch(registro -> registro.getEstado().getEstado().toString().equals("DISPONIBLE"));
    }
}