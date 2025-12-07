module ar.utn.hotel.hotel_premier {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires java.net.http;
    requires java.desktop;
    requires org.hibernate.orm.core;
    requires java.naming;
    requires jakarta.persistence;
    requires static lombok;


    opens ar.utn.hotel.model to org.hibernate.orm.core;
    opens ar.utn.hotel.dao.implement to org.hibernate.orm.core;
    opens ar.utn.hotel.dao.interfaces to org.hibernate.orm.core;
    opens ar.utn.hotel to javafx.fxml;
    opens controllers.BuscarHuesped to javafx.fxml;
    opens controllers.EstadoHabitaciones to javafx.fxml;
    opens controllers.ReservarHabitacion to javafx.fxml;
    opens controllers.OcuparHabitacion to javafx.fxml;

    exports ar.utn.hotel;
    exports controllers.EstadoHabitaciones;
    exports controllers.AltaHuesped;
    opens controllers.AltaHuesped to javafx.fxml;
    exports controllers.MenuPrincipal;
    opens controllers.MenuPrincipal to javafx.fxml;
    exports controllers.PopUp;
    opens controllers.PopUp to javafx.fxml;

}
