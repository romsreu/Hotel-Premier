package controllers.ReservarHabitacion;

import ar.utn.hotel.HotelPremier;
import ar.utn.hotel.dto.HabitacionReservaDTO;
import controllers.PopUp.PopUpController;
import enums.PopUpType;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import utils.DataTransfer;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReservarHabitacionController1 {

    @FXML private GridPane gridPane;
    @FXML private Button btnCancelar;
    @FXML private Button btnVolver;
    @FXML private Button btnAceptar;

    private List<HabitacionReservaDTO> habitacionesSeleccionadas;

    @FXML
    public void initialize() {
        // Obtener datos del DataTransfer
        habitacionesSeleccionadas = DataTransfer.getHabitacionesSeleccionadas();

        if (habitacionesSeleccionadas == null || habitacionesSeleccionadas.isEmpty()) {
            PopUpController.mostrarPopUp(PopUpType.ERROR,
                    "No hay habitaciones seleccionadas");
            HotelPremier.cambiarA("menu");
            return;
        }

        cargarResumen();
    }

    private void cargarResumen() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Limpiar filas existentes (excepto la primera que son los encabezados)
        gridPane.getRowConstraints().clear();

        // Agregar constraint para la fila de encabezados
        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(40.0);
        headerRow.setPrefHeight(40.0);
        gridPane.getRowConstraints().add(headerRow);

        int fila = 1; // Empezamos desde 1 porque 0 son los encabezados

        for (HabitacionReservaDTO habitacion : habitacionesSeleccionadas) {
            // Agregar constraint para esta fila
            RowConstraints row = new RowConstraints();
            row.setMinHeight(40.0);
            row.setPrefHeight(40.0);
            gridPane.getRowConstraints().add(row);

            // Columna 0: Número de habitación
            Label lblNumero = crearLabel(habitacion.getNumeroHabitacion().toString());
            gridPane.add(lblNumero, 0, fila);

            // Columna 1: Tipo de habitación
            Label lblTipo = crearLabel(habitacion.getTipoHabitacion());
            gridPane.add(lblTipo, 1, fila);

            // Columna 2: Fecha de ingreso
            Label lblIngreso = crearLabel(habitacion.getFechaIngreso().format(formato));
            gridPane.add(lblIngreso, 2, fila);

            // Columna 3: Fecha de egreso
            Label lblEgreso = crearLabel(habitacion.getFechaEgreso().format(formato));
            gridPane.add(lblEgreso, 3, fila);

            fila++;
        }
    }

    private Label crearLabel(String texto) {
        Label label = new Label(texto);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setFont(Font.font("Lucida Bright", 13));
        label.setStyle("-fx-padding: 10; -fx-text-fill: #2c3e50;");
        return label;
    }

    @FXML
    private void onCancelarClicked() {
        PopUpController.mostrarPopUpConCallback(
                PopUpType.CONFIRMATION,
                "¿Está seguro de cancelar la reserva?",
                confirmado -> {
                    if (confirmado) {
                        DataTransfer.limpiar();
                        HotelPremier.cambiarA("menu");
                    }
                }
        );
    }

    @FXML
    private void onVolverClicked() {
        // Volver a la pantalla de selección de habitaciones
        HotelPremier.cambiarA("estado_habs2");
    }

    @FXML
    private void onAceptarClicked() {
        // Ir a la siguiente pantalla (reservar_hab2) donde se selecciona la persona
        HotelPremier.cambiarA("reservar_hab2");
    }
}