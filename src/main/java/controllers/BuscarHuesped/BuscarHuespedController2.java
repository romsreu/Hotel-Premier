package controllers.BuscarHuesped;

import ar.utn.hotel.HotelPremier;
import ar.utn.hotel.model.Huesped;
import controllers.PopUpController;
import enums.PopUpType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import utils.DataTransfer;

import java.util.List;

public class BuscarHuespedController2 {

    @FXML private VBox containerHuespedes;
    @FXML private Button btnCancelar;
    @FXML private Button btnSiguiente;

    private Huesped huespedSeleccionado;
    private ObservableList<Huesped> listaHuespedes;
    private GridPane filaSeleccionada;

    public void initialize() {
        cargarHuespedesDesdeTransfer();
        mostrarHuespedes();
        configurarBotones();
    }

    private void cargarHuespedesDesdeTransfer() {
        List<Huesped> huespedes = DataTransfer.getHuespedesEnBusqueda();

        if (huespedes != null) {
            listaHuespedes = FXCollections.observableArrayList(huespedes);
        } else {
            listaHuespedes = FXCollections.observableArrayList();
        }
    }

    private void mostrarHuespedes() {
        if (listaHuespedes == null || listaHuespedes.isEmpty()) {
            Label lblVacio = new Label("No se encontraron huéspedes");
            lblVacio.setStyle("-fx-text-fill: #999999; -fx-font-size: 14;");
            lblVacio.setPadding(new Insets(20, 10, 10, 10));
            containerHuespedes.getChildren().add(lblVacio);
            return;
        }

        for (Huesped huesped : listaHuespedes) {
            GridPane filaHuesped = crearFilaHuesped(huesped);
            containerHuespedes.getChildren().add(filaHuesped);
        }
    }

    private GridPane crearFilaHuesped(Huesped huesped) {
        GridPane gridRow = new GridPane();
        gridRow.setPrefHeight(50.0);
        gridRow.setMinHeight(50.0);
        gridRow.setMaxHeight(50.0);

        // NO establecer ancho fijo - dejar que se expanda como el header
        // gridRow.setPrefWidth(850.0);

        // IMPORTANTE: Sin gaps ni padding
        gridRow.setHgap(0);
        gridRow.setVgap(0);

        // Configurar exactamente igual que el FXML
        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
        col1.setMinWidth(100.0);
        col1.setPrefWidth(150.0);

        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
        col2.setMinWidth(100.0);
        col2.setPrefWidth(150.0);

        javafx.scene.layout.ColumnConstraints col3 = new javafx.scene.layout.ColumnConstraints();
        col3.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
        col3.setMinWidth(100.0);
        col3.setPrefWidth(150.0);

        javafx.scene.layout.ColumnConstraints col4 = new javafx.scene.layout.ColumnConstraints();
        col4.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
        col4.setMinWidth(100.0);
        col4.setPrefWidth(150.0);

        javafx.scene.layout.ColumnConstraints col5 = new javafx.scene.layout.ColumnConstraints();
        col5.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
        col5.setMinWidth(100.0);
        col5.setPrefWidth(250.0);

        gridRow.getColumnConstraints().addAll(col1, col2, col3, col4, col5);

        // Row constraint
        javafx.scene.layout.RowConstraints row = new javafx.scene.layout.RowConstraints();
        row.setMinHeight(50.0);
        row.setPrefHeight(50.0);
        row.setVgrow(javafx.scene.layout.Priority.SOMETIMES);
        gridRow.getRowConstraints().add(row);

        gridRow.setStyle("-fx-background-color: #fdfaf2; -fx-border-color: #e8dcc4; -fx-border-width: 0 0 1 0;");

        Label lblNombre = crearLabel(huesped.getNombre(), 0);
        Label lblApellido = crearLabel(huesped.getApellido(), 1);
        Label lblTipoDoc = crearLabel(huesped.getTipoDocumento(), 2);
        Label lblNumDoc = crearLabel(huesped.getNumeroDocumento(), 3);
        Label lblEmail = crearLabel(huesped.getEmail(), 4);

        gridRow.getChildren().addAll(lblNombre, lblApellido, lblTipoDoc, lblNumDoc, lblEmail);

        gridRow.setOnMouseClicked(e -> seleccionarFila(gridRow, huesped));

        gridRow.setOnMouseEntered(e -> {
            if (filaSeleccionada != gridRow) {
                gridRow.setStyle("-fx-background-color: #f5ecd4; -fx-border-color: #e0cfa8; -fx-border-width: 0 0 1 0;");
            }
        });

        gridRow.setOnMouseExited(e -> {
            if (filaSeleccionada != gridRow) {
                gridRow.setStyle("-fx-background-color: #fdfaf2; -fx-border-color: #e8dcc4; -fx-border-width: 0 0 1 0;");
            }
        });

        gridRow.setCursor(Cursor.HAND);

        return gridRow;
    }

    private void seleccionarFila(GridPane fila, Huesped huesped) {
        if (filaSeleccionada != null) {
            filaSeleccionada.setStyle("-fx-background-color: #fdfaf2; -fx-border-color: #e8dcc4; -fx-border-width: 0 0 1 0;");
            filaSeleccionada.getChildren().forEach(node -> {
                if (node instanceof Label) {
                    ((Label) node).setStyle("-fx-text-fill: #2E1D0E;");
                }
            });
        }

        filaSeleccionada = fila;
        huespedSeleccionado = huesped;

        fila.setStyle("-fx-background-color: linear-gradient(to right, #e7c375, #f5d68a); -fx-border-color: #c89b3c; -fx-border-width: 0 0 2 0;");
        fila.getChildren().forEach(node -> {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-text-fill: #2E1D0E; -fx-font-weight: bold;");
            }
        });
    }

    private Label crearLabel(String texto, int columnIndex) {
        Label label = new Label(texto != null ? texto : "");
        label.setFont(Font.font("Lucida Bright", 14.0));
        label.setStyle("-fx-text-fill: #2E1D0E;");

        // Configurar la posición en el grid
        GridPane.setColumnIndex(label, columnIndex);
        GridPane.setRowIndex(label, 0);

        // Usar el mismo margen que el header: solo left 10.0
        GridPane.setMargin(label, new Insets(0, 0, 0, 10.0));

        return label;
    }

    private void configurarBotones() {
        btnCancelar.setOnAction(e -> onCancelarClicked());
        btnSiguiente.setOnAction(e -> onSiguienteClicked());
    }

    @FXML
    private void onCancelarClicked() {
        HotelPremier.cambiarA("buscar_huesped1");
    }

    @FXML
    private void onSiguienteClicked() {
        if (huespedSeleccionado != null) {
            // DataTransfer.setHuespedSeleccionado(huespedSeleccionado);
            // HotelPremier.cambiarA("proxima_pantalla");
        } else {
            PopUpController.mostrarPopUp(
                    PopUpType.WARNING,
                    "Por favor, seleccione un huésped para continuar"
            );
        }
    }
}