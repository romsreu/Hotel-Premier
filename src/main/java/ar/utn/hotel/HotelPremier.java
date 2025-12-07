package ar.utn.hotel;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.GeorefLoader;
import utils.SceneManager;

public class  HotelPremier extends Application {

    private static Scene mainScene;

    @Override
    public void start(Stage stage) {

        GeorefLoader.cargarTodo();
        precargarEscenas();

        mainScene = new Scene(SceneManager.getRoot("menu"));

        stage.setTitle("Hotel Premier");
        stage.setScene(mainScene);
        stage.setResizable(true);
        stage.setFullScreen(true);
        stage.show();
    }

    private void precargarEscenas (){
        // Escenas que se precargan (estáticas, no cambian)
        SceneManager.precargarEscena("menu", "/views/interfaces/menu-principal/menu-principal.fxml");
        SceneManager.precargarEscena("alta_huesped", "/views/interfaces/alta-huesped/alta-huesped.fxml");
        SceneManager.precargarEscena("estado_habs1", "/views/interfaces/estado-habitaciones/estado-habitaciones-1.fxml");
        SceneManager.precargarEscena("buscar_huesped1", "/views/interfaces/buscar-huesped/buscar-huesped-1.fxml");

        // Escenas dinámicas solo se registran (se cargan bajo demanda)
        SceneManager.registrarEscena("estado_habs2", "/views/interfaces/estado-habitaciones/estado-habitaciones-2.fxml");
        SceneManager.registrarEscena("buscar_huesped2", "/views/interfaces/buscar-huesped/buscar-huesped-2.fxml");
        SceneManager.registrarEscena("reservar_hab1", "/views/interfaces/reservar-habitacion/reservar-habitacion-1.fxml");
        SceneManager.registrarEscena("reservar_hab2", "/views/interfaces/reservar-habitacion/reservar-habitacion-2.fxml");
        SceneManager.registrarEscena("ocupar_hab1", "/views/interfaces/ocupar-habitacion/ocupar-habitacion-1.fxml");
        SceneManager.registrarEscena("ocupar_hab2", "/views/interfaces/ocupar-habitacion/ocupar-habitacion-2.fxml");
    }

    public static void cambiarA(String nombreEscena) {
        mainScene.setRoot(SceneManager.getRoot(nombreEscena));
    }
}

