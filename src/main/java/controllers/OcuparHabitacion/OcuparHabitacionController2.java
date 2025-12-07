package controllers.OcuparHabitacion;

import ar.utn.hotel.HotelPremier;
import ar.utn.hotel.dto.CrearReservaDTO;
import ar.utn.hotel.dto.HabitacionReservaDTO;
import ar.utn.hotel.dto.ReservaDTO;
import ar.utn.hotel.gestor.GestorHabitacion;
import ar.utn.hotel.gestor.GestorReserva;
import ar.utn.hotel.model.Huesped;
import ar.utn.hotel.model.Reserva;
import controllers.PopUp.PopUpController;
import enums.PopUpType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.Cursor;
import utils.DataTransfer;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class OcuparHabitacionController2 {

    @FXML private GridPane gridPane;
    @FXML private Button btnCancelar;
    @FXML private Button btnAceptar;

    private GestorHabitacion gestorHabitacion;
    private GestorReserva gestorReserva;
    private List<Huesped> huespedesEncontrados;
    private Huesped huespedSeleccionado;
    private List<HabitacionReservaDTO> habitacionesSeleccionadas;

    @FXML
    public void initialize() {
        // Inicializar gestores
        gestorHabitacion = new GestorHabitacion();
        gestorReserva = new GestorReserva();

        // Establecer referencias circulares
        gestorReserva.setGestorHabitacion(gestorHabitacion);
        gestorHabitacion.setGestorReserva(gestorReserva);

        // Obtener datos del DataTransfer
        huespedesEncontrados = DataTransfer.getHuespedesEnBusqueda();
        habitacionesSeleccionadas = DataTransfer.getHabitacionesSeleccionadas();

        // Validar que tengamos todos los datos necesarios
        if (huespedesEncontrados == null || huespedesEncontrados.isEmpty()) {
            PopUpController.mostrarPopUp(
                    PopUpType.ERROR,
                    "No hay huéspedes para mostrar"
            );
            HotelPremier.cambiarA("ocupar_hab1");
            return;
        }

        if (habitacionesSeleccionadas == null || habitacionesSeleccionadas.isEmpty()) {
            PopUpController.mostrarPopUp(
                    PopUpType.ERROR,
                    "No hay habitaciones seleccionadas"
            );
            HotelPremier.cambiarA("menu");
            return;
        }

        cargarHuespedes();
        btnAceptar.setDisable(true);
    }

    private void cargarHuespedes() {
        gridPane.getRowConstraints().clear();

        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(40.0);
        headerRow.setPrefHeight(40.0);
        gridPane.getRowConstraints().add(headerRow);

        int fila = 1;

        for (Huesped huesped : huespedesEncontrados) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(50.0);
            row.setPrefHeight(50.0);
            gridPane.getRowConstraints().add(row);

            // Columna 0: Nombre
            Label lblNombre = crearLabel(huesped.getNombre());
            gridPane.add(lblNombre, 0, fila);

            // Columna 1: Apellido
            Label lblApellido = crearLabel(huesped.getApellido());
            gridPane.add(lblApellido, 1, fila);

            // Columna 2: Tipo Documento
            String tipoDoc = huesped.getTipoDocumento() != null ?
                    huesped.getTipoDocumento() : "N/A";
            Label lblTipoDoc = crearLabel(tipoDoc);
            gridPane.add(lblTipoDoc, 2, fila);

            // Columna 3: Número Documento
            String numDoc = huesped.getNumeroDocumento() != null ?
                    huesped.getNumeroDocumento() : "N/A";
            Label lblNumDoc = crearLabel(numDoc);
            gridPane.add(lblNumDoc, 3, fila);

            // Columna 4: Botón Seleccionar
            Button btnSeleccionar = new Button("SELECCIONAR");
            btnSeleccionar.setStyle(
                    "-fx-background-color: #28a745; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15;"
            );
            btnSeleccionar.setCursor(Cursor.HAND);
            btnSeleccionar.setOnAction(e -> seleccionarHuesped(huesped));

            gridPane.add(btnSeleccionar, 4, fila);

            fila++;
        }
    }

    private Label crearLabel(String texto) {
        Label label = new Label(texto);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #252523; -fx-padding: 10;");
        return label;
    }

    private void seleccionarHuesped(Huesped huesped) {
        huespedSeleccionado = huesped;
        btnAceptar.setDisable(false);

        PopUpController.mostrarPopUp(
                PopUpType.SUCCESS,
                String.format("✓ Huésped seleccionado:\n%s %s",
                        huesped.getNombre(),
                        huesped.getApellido())
        );
    }

    @FXML
    public void onCancelarClicked(ActionEvent actionEvent) {
        HotelPremier.cambiarA("ocupar_hab1");
    }

    @FXML
    public void onAceptarClicked(ActionEvent actionEvent) {
        if (huespedSeleccionado == null) {
            PopUpController.mostrarPopUp(
                    PopUpType.WARNING,
                    "Debe seleccionar un huésped"
            );
            return;
        }

        confirmarOcupacion();
    }

    private void confirmarOcupacion() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("¿Confirmar ocupación?\n\n");
        mensaje.append(String.format("Huésped: %s %s\n",
                huespedSeleccionado.getNombre(),
                huespedSeleccionado.getApellido()));

        if (huespedSeleccionado.getNumeroDocumento() != null) {
            mensaje.append(String.format("DNI: %s\n\n",
                    huespedSeleccionado.getNumeroDocumento()));
        }

        mensaje.append("HABITACIONES A OCUPAR:\n");
        for (HabitacionReservaDTO hab : habitacionesSeleccionadas) {
            mensaje.append(String.format("• Habitación %d (%s)\n",
                    hab.getNumeroHabitacion(),
                    hab.getTipoHabitacion()));
            mensaje.append(String.format("  %s - %s\n",
                    hab.getFechaIngreso().format(formatter),
                    hab.getFechaEgreso().format(formatter)));
        }

        PopUpController.mostrarPopUpConCallback(
                PopUpType.CONFIRMATION,
                mensaje.toString(),
                confirmado -> {
                    if (confirmado) {
                        procesarOcupacion();
                    }
                }
        );
    }

    private void procesarOcupacion() {
        try {
            List<Long> reservasCreadas = new ArrayList<>();

            // Por cada habitación seleccionada, procesar la ocupación
            for (HabitacionReservaDTO hab : habitacionesSeleccionadas) {
                // 1. Buscar si existe una reserva para esta habitación en estas fechas
                Reserva reservaExistente = gestorReserva.buscarReservaPorHabitacionYFecha(
                        hab.getNumeroHabitacion(),
                        hab.getFechaIngreso()
                );

                Long idReserva;

                if (reservaExistente == null) {
                    // NO existe reserva: Crear reserva primero
                    CrearReservaDTO dtoReserva = CrearReservaDTO.builder()
                            .idHuesped(huespedSeleccionado.getId())
                            .numeroHabitacion(hab.getNumeroHabitacion())
                            .fechaInicio(hab.getFechaIngreso())
                            .fechaFin(hab.getFechaEgreso())
                            .cantHuespedes(1) // Por defecto 1
                            .descuento(0.0)
                            .build();

                    ReservaDTO reserva = gestorReserva.crearReserva(dtoReserva);
                    idReserva = reserva.getId();

                    // Delay entre operaciones
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    // Ya existe reserva
                    idReserva = reservaExistente.getId();
                }

                reservasCreadas.add(idReserva);
            }

            // 2. Hacer check-in en todas las reservas (crear estadías)
            for (Long idReserva : reservasCreadas) {
                gestorHabitacion.realizarCheckIn(idReserva);

                // Delay entre operaciones
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            mostrarExito();

        } catch (Exception e) {
            PopUpController.mostrarPopUp(
                    PopUpType.ERROR,
                    "Error al procesar la ocupación:\n" + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    private void mostrarExito() {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("✓ OCUPACIÓN EXITOSA\n\n");
        mensaje.append(String.format("Huésped: %s %s\n\n",
                huespedSeleccionado.getNombre(),
                huespedSeleccionado.getApellido()));
        mensaje.append(String.format("Habitaciones ocupadas: %d\n\n",
                habitacionesSeleccionadas.size()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (HabitacionReservaDTO hab : habitacionesSeleccionadas) {
            mensaje.append(String.format("• Hab %d: %s - %s\n",
                    hab.getNumeroHabitacion(),
                    hab.getFechaIngreso().format(formatter),
                    hab.getFechaEgreso().format(formatter)));
        }

        PopUpController.mostrarPopUpConCallback(
                PopUpType.SUCCESS,
                mensaje.toString(),
                confirmado -> {
                    DataTransfer.limpiar();
                    HotelPremier.cambiarA("menu");
                }
        );
    }
}