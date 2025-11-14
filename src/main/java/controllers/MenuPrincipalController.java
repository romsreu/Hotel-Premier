package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import ar.utn.hotel.HotelPremier;

public class MenuPrincipalController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private void on_alta_huesped_pressed(ActionEvent event) {
        HotelPremier.cambiarA("alta_huesped");
    }
}
