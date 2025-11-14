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
    opens ar.utn.hotel.dto to org.hibernate.orm.core;
    opens ar.utn.hotel.dao to org.hibernate.orm.core;
    opens ar.utn.hotel to javafx.fxml;
    opens controllers to javafx.fxml;

    exports ar.utn.hotel;
    exports controllers;
}
