package controllers.EstadoHabitaciones;

import ar.utn.hotel.HotelPremier;
import enums.ContextoEstadoHabitaciones; // Asegúrate de importar esto
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import utils.DataTransfer;

import java.time.LocalDate;

public class EstadoHabitacionesController1 {
    @FXML
    private DatePicker dpFechaDesde;
    @FXML
    private DatePicker dpFechaHasta;

    @FXML
    private void initialize() {
        // 1. Limpieza de seguridad:
        // Aseguramos que no haya una reserva "colgada" de una operación anterior,
        // pero NO usamos DataTransfer.limpiar() porque eso borraría el CONTEXTO (Reservar/Ocupar)
        // que viene desde el Menú Principal.
        DataTransfer.setReservaPendiente(null);

        configurarDatePickers();
    }

    public void onMostrarClicked(ActionEvent actionEvent) {
        // 2. Guardamos las fechas seleccionadas en el DataTransfer
        // La pantalla 2 (Grilla) usará estas fechas para cargar los estados.
        DataTransfer.setRangoFechasEstadoHabitaciones(
                dpFechaDesde.getValue(),
                dpFechaHasta.getValue()
        );

        // 3. Validación de seguridad (Opcional pero recomendada):
        // Si el contexto es nulo (por error de programación en el menú), asumimos MOSTRAR.
        if (DataTransfer.getContextoEstadoHabitaciones() == null) {
            DataTransfer.setContextoEstadoHabitaciones(ContextoEstadoHabitaciones.MOSTRAR);
        }

        // 4. Cambiamos a la pantalla de la grilla
        HotelPremier.cambiarA("estado_habs2");
    }

    @FXML
    private void onCancelarClicked(){
        // Al cancelar, aquí sí limpiamos TODO (incluido el contexto)
        DataTransfer.limpiar();
        HotelPremier.cambiarA("menu");
    }

    private void configurarDatePickers() {
        LocalDate fechaActual = LocalDate.now();
        LocalDate fechaMaxima = fechaActual.plusYears(1);

        // Configurar valores iniciales
        dpFechaDesde.setValue(fechaActual);
        dpFechaHasta.setValue(fechaActual);

        // Configurar restricciones iniciales
        bloquearFechasInvalidasDesde(fechaActual, fechaMaxima);
        bloquearFechasInvalidasHasta(fechaActual, fechaMaxima);

        // Listeners para sincronización automática
        dpFechaDesde.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpFechaHasta.getValue() != null) {
                if (newVal.isAfter(dpFechaHasta.getValue())) {
                    dpFechaHasta.setValue(newVal);
                }
            }
            bloquearFechasInvalidasHasta(fechaActual, fechaMaxima);
        });

        dpFechaHasta.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpFechaDesde.getValue() != null) {
                if (newVal.isBefore(dpFechaDesde.getValue())) {
                    dpFechaDesde.setValue(newVal);
                }
            }
            bloquearFechasInvalidasDesde(fechaActual, fechaMaxima);
        });
    }

    private void bloquearFechasInvalidasDesde(LocalDate fechaMinima, LocalDate fechaMaxima) {
        dpFechaDesde.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate fechaHasta = dpFechaHasta.getValue();
                boolean debeDeshabilitarse = empty ||
                        date.isBefore(fechaMinima) ||
                        date.isAfter(fechaMaxima) ||
                        (fechaHasta != null && date.isAfter(fechaHasta));
                setDisable(debeDeshabilitarse);
            }
        });
    }

    private void bloquearFechasInvalidasHasta(LocalDate fechaMinima, LocalDate fechaMaxima) {
        dpFechaHasta.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate fechaDesde = dpFechaDesde.getValue();
                boolean debeDeshabilitarse = empty ||
                        date.isBefore(fechaMinima) ||
                        date.isAfter(fechaMaxima) ||
                        (fechaDesde != null && date.isBefore(fechaDesde));
                setDisable(debeDeshabilitarse);
            }
        });
    }
}