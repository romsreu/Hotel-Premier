package ar.utn.hotel;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.GeorefLoader;
import utils.SceneManager;

public class HotelPremier extends Application {

    private static Scene mainScene;

    @Override
    public void start(Stage stage) {
        GeorefLoader.cargarTodo();
        SceneManager.precargarEscena("menu", "/views/interfaces/menu-principal.fxml");
        SceneManager.precargarEscena("alta_huesped", "/views/interfaces/dar-alta-huesped.fxml");
        //SceneManager.precargarEscena("config", "/views/interfaces/configuracion.fxml");
        //SceneManager.precargarEscena("estadisticas", "/views/interfaces/estadisticas.fxml");

        // Crear ÚNICA Scene
        mainScene = new Scene(SceneManager.getRoot("menu"));

        stage.setTitle("Hotel Premier");
        stage.setScene(mainScene);
        stage.setResizable(true);
        stage.setFullScreen(true);
        stage.show();
    }

    public static void cambiarA(String nombreEscena) {
        mainScene.setRoot(SceneManager.getRoot(nombreEscena));
    }

    public static void main(String[] args) {
        launch(args);
    }
}

