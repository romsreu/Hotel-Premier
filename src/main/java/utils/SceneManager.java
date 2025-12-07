package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.util.HashMap;
import java.util.Map;

public class SceneManager {

    private static final Map<String, Parent> roots = new HashMap<>();
    private static final Map<String, String> rutasFXML = new HashMap<>();

    // Registrar la ruta de una escena (sin cargarla aún)
    public static void registrarEscena(String nombre, String rutaFXML) {
        rutasFXML.put(nombre, rutaFXML);
    }

    // Precargar una escena (cargarla inmediatamente)
    public static void precargarEscena(String nombre, String rutaFXML) {
        try {
            rutasFXML.put(nombre, rutaFXML); // También registrar la ruta por si se necesita recargar
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(rutaFXML));
            Parent root = loader.load();
            roots.put(nombre, root);
        } catch (Exception e) {
            System.err.println("Error al precargar escena '" + nombre + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Obtener el root de una escena (cargándola si no está precargada)
    public static Parent getRoot(String nombre) {
        // Si ya está cargada, devolverla
        if (roots.containsKey(nombre)) {
            return roots.get(nombre);
        }

        // Si no está cargada pero está registrada, cargarla ahora
        if (rutasFXML.containsKey(nombre)) {
            try {
                FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(rutasFXML.get(nombre)));
                Parent root = loader.load();
                // NO guardar en cache para escenas dinámicas
                return root;
            } catch (Exception e) {
                System.err.println("Error al cargar escena '" + nombre + "': " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        System.err.println("Escena '" + nombre + "' no encontrada. No está ni precargada ni registrada.");
        return null;
    }

    // Recargar una escena (útil para escenas dinámicas que necesitan refrescarse)
    public static Parent recargarEscena(String nombre) {
        if (rutasFXML.containsKey(nombre)) {
            try {
                FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(rutasFXML.get(nombre)));
                Parent root = loader.load();
                return root;
            } catch (Exception e) {
                System.err.println("Error al recargar escena '" + nombre + "': " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    // Limpiar una escena precargada (liberar memoria)
    public static void limpiarEscena(String nombre) {
        roots.remove(nombre);
    }
}