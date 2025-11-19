package controllers.EstadoHabitaciones;

import ar.utn.hotel.HotelPremier;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;

public class EstadoHabitacionesController1 {

    @FXML
    private void onCancelarClicked(){
        HotelPremier.cambiarA("menu");

    }
}
