package controllers;

import enums.ContextoEstadoHabitaciones;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import ar.utn.hotel.HotelPremier;
import utils.DataTransfer;

public class MenuPrincipalController {

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
