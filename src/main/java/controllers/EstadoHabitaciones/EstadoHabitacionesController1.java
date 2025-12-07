package controllers.EstadoHabitaciones;

import ar.utn.hotel.HotelPremier;
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


    public void onMostrarClicked(ActionEvent actionEvent) {
        DataTransfer.setRangoFechasEstadoHabitaciones(
                dpFechaDesde.getValue(),
                dpFechaHasta.getValue()
        );
        HotelPremier.cambiarA("estado_habs2");
    }

    @FXML
    private void onCancelarClicked(){
        DataTransfer.limpiar();
        HotelPremier.cambiarA("menu");
    }

    @FXML
    private void initialize() {
        configurarDatePickers();
    }

    private void configurarDatePickers() {
        LocalDate fechaActual = LocalDate.now();
        LocalDate fechaMaxima = fechaActual.plusYears(1);

        // Configurar valores iniciales
        dpFechaDesde.setValue(fechaActual);
        dpFechaHasta.setValue(fechaActual);

        // Configurar restricciones para dpFechaDesde
        bloquearFechasInvalidasDesde(fechaActual, fechaMaxima);

        // Configurar restricciones para dpFechaHasta
        bloquearFechasInvalidasHasta(fechaActual, fechaMaxima);

        // Agregar listeners para sincronizar las fechas
        dpFechaDesde.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpFechaHasta.getValue() != null) {
                if (newVal.isAfter(dpFechaHasta.getValue())) {
                    dpFechaHasta.setValue(newVal);
                }
            }
            // Reconfigurar restricciones de dpFechaHasta
            bloquearFechasInvalidasHasta(fechaActual, fechaMaxima);
        });

        dpFechaHasta.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpFechaDesde.getValue() != null) {
                if (newVal.isBefore(dpFechaDesde.getValue())) {
                    dpFechaDesde.setValue(newVal);
                }
            }
            // Reconfigurar restricciones de dpFechaDesde
            bloquearFechasInvalidasDesde(fechaActual, fechaMaxima);
        });
    }

    private void bloquearFechasInvalidasDesde(LocalDate fechaMinima, LocalDate fechaMaxima) {
        dpFechaDesde.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate fechaHasta = dpFechaHasta.getValue();

                // Deshabilitar si:
                // - La fecha es anterior a hoy
                // - La fecha es posterior a un año desde hoy
                // - La fecha es posterior a fechaHasta (si está definida)
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

                // Deshabilitar si:
                // - La fecha es anterior a hoy
                // - La fecha es posterior a un año desde hoy
                // - La fecha es anterior a fechaDesde (si está definida)
                boolean debeDeshabilitarse = empty ||
                        date.isBefore(fechaMinima) ||
                        date.isAfter(fechaMaxima) ||
                        (fechaDesde != null && date.isBefore(fechaDesde));

                setDisable(debeDeshabilitarse);
            }
        });
    }


}