package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.util.HashMap;
import java.util.Map;

public class SceneManager {

    private static final Map<String, Parent> roots = new HashMap<>();

    public static void precargarEscena(String nombre, String rutaFXML) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(rutaFXML));
            Parent root = loader.load();
            roots.put(nombre, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Parent getRoot(String nombre) {
        return roots.get(nombre);
    }
}
