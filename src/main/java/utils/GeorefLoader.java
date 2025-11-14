package utils;

import javafx.application.Platform;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class GeorefLoader {

    private static final HttpClient http = HttpClient.newHttpClient();

    public static void cargarTodo() {
        new Thread(() -> {
            try {
                cargarProvincias();

                // Cargar localidades por cada provincia
                for (String prov : GeorefCache.provincias) {
                    cargarLocalidades(prov);
                }

                GeorefCache.listo = true;
                System.out.println("✔ GEOREF cargado completamente");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void cargarProvincias() throws Exception {

        String url = "https://apis.datos.gob.ar/georef/api/provincias?campos=nombre&max=200";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        String json = http.send(req, HttpResponse.BodyHandlers.ofString()).body();
        GeorefCache.provincias = extraerNombres(json);
    }

    private static void cargarLocalidades(String provincia) throws Exception {

        String provEncoded = URLEncoder.encode(provincia, StandardCharsets.UTF_8);
        String url = "https://apis.datos.gob.ar/georef/api/localidades?provincia=" + provEncoded
                + "&campos=nombre&max=5000";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        String json = http.send(req, HttpResponse.BodyHandlers.ofString()).body();
        List<String> lista = extraerNombres(json);

        GeorefCache.localidadesPorProvincia.put(provincia, lista);
    }

    private static List<String> extraerNombres(String json) {
        java.util.List<String> nombres = new java.util.ArrayList<>();
        String[] partes = json.split("\"nombre\"\\s*:\\s*");
        for (int i = 1; i < partes.length; i++) {
            String resto = partes[i].trim();
            if (!resto.startsWith("\"")) continue;
            int fin = resto.indexOf('"', 1);
            if (fin > 1) {
                nombres.add(resto.substring(1, fin).trim());
            }
        }
        nombres.sort(String::compareToIgnoreCase);
        return nombres;
    }
}
