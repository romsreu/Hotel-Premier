package controllers.BuscarHuesped;

import ar.utn.hotel.HotelPremier;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class BuscarHuespedController1 {
    @FXML
    public void onCancelarClicked(ActionEvent actionEvent) {
        HotelPremier.cambiarA("menu");
    }
}
