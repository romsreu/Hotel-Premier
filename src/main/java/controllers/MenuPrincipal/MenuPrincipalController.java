package controllers.MenuPrincipal;

import enums.ContextoEstadoHabitaciones;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import ar.utn.hotel.HotelPremier;
import javafx.scene.text.Text;
import utils.DataTransfer;

import javafx.util.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MenuPrincipalController {
    @FXML
    private Text fechaId;
    @FXML
    private Text horaId;

    @FXML
    private void initialize() {
        cargarFechaActual();
        iniciarReloj();
    }

    private void cargarFechaActual() {
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        fechaId.setText(hoy.format(formatoFecha));
    }

    private void iniciarReloj() {
        DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm:ss");

        Timeline reloj = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    LocalTime horaActual = LocalTime.now();
                    horaId.setText(horaActual.format(formatoHora));
                }),
                new KeyFrame(Duration.seconds(1))
        );

        reloj.setCycleCount(Animation.INDEFINITE);
        reloj.play();
    }

    @FXML
    private AnchorPane rootPane;

    @FXML
    private void on_alta_huesped_pressed(ActionEvent event) {
        HotelPremier.cambiarA("alta_huesped");
    }

    @FXML
    public void on_buscar_huesped_pressed(ActionEvent actionEvent) {
        HotelPremier.cambiarA("buscar_huesped1");
    }

    @FXML
    private void on_mostrar_estado_habs_pressed(ActionEvent event){
        DataTransfer.setContextoEstadoHabitaciones(ContextoEstadoHabitaciones.MOSTRAR);
        HotelPremier.cambiarA("estado_habs1");
    }

    @FXML
    public void on_reservar_habs_pressed(ActionEvent actionEvent) {
        DataTransfer.setContextoEstadoHabitaciones(ContextoEstadoHabitaciones.RESERVAR);
        HotelPremier.cambiarA("estado_habs1");
    }

    @FXML
    public void on_ocupar_habs_pressed(ActionEvent actionEvent) {
        DataTransfer.setContextoEstadoHabitaciones(ContextoEstadoHabitaciones.OCUPAR);
        HotelPremier.cambiarA("estado_habs1");
    }
}
