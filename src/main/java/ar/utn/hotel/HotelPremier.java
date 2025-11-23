package ar.utn.hotel;

import ar.utn.hotel.dao.HabitacionDAO;
import ar.utn.hotel.dao.PersonaDAO;
import ar.utn.hotel.dao.ReservaDAO;
import ar.utn.hotel.dao.impl.HabitacionDAOImpl;
import ar.utn.hotel.dao.impl.PersonaDAOImpl;
import ar.utn.hotel.dao.impl.ReservaDAOImpl;
import ar.utn.hotel.dto.BuscarHuespedDTO;
import ar.utn.hotel.dto.HabitacionDTO;
import ar.utn.hotel.dto.ReservaDTO;
import ar.utn.hotel.gestor.GestorHabitacion;
import ar.utn.hotel.gestor.GestorHuesped;
import ar.utn.hotel.gestor.GestorReserva;
import ar.utn.hotel.model.Huesped;
import ar.utn.hotel.services.InicializadorHabitaciones;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.GeorefLoader;
import utils.SceneManager;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class  HotelPremier extends Application {

    private static Scene mainScene;

    @Override
    public void start(Stage stage) {

        probarMetodos();

        GeorefLoader.cargarTodo();
        precargarEscenas();

        mainScene = new Scene(SceneManager.getRoot("menu"));

        stage.setTitle("Hotel Premier");
        stage.setScene(mainScene);
        stage.setResizable(true);
        stage.setFullScreen(true);
        stage.show();
    }

    private static void probarMetodos() {
        // Inicializar DAOs
        HabitacionDAO habitacionDAO = new HabitacionDAOImpl();
        PersonaDAO personaDAO = new PersonaDAOImpl();
        ReservaDAO reservaDAO = new ReservaDAOImpl(personaDAO, habitacionDAO);

        // Inicializar Gestores
        GestorHabitacion gestorHabitacion = new GestorHabitacion(habitacionDAO);
        GestorReserva gestorReserva = new GestorReserva(reservaDAO, personaDAO);

        // Ejemplo: Crear reserva usando DTO con ID de persona
        System.out.println("\n=== Creando reserva con DTO (por ID) ===");
        try {
            ReservaDTO dto = ReservaDTO.builder()
                    .nombrePersona("PEPE")
                    .apellidoPersona("LUI")
                    .fechaInicio(LocalDate.now())
                    .fechaFin(LocalDate.now().plusDays(10))
                    .numerosHabitaciones(new HashSet<>(List.of(101, 102)))
                    .build();

            ReservaDTO reservaCreada = gestorReserva.crearReserva(dto);
            System.out.println("✓ Reserva creada: " + reservaCreada);
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
        }

        // Mostrar habitaciones disponibles entre 23/11/2025 y 1/12/2025
        System.out.println("=== Estado de habitaciones ===");
        List<HabitacionDTO> disponibles = gestorHabitacion.obtenerHabitacionesDisponibles(LocalDate.now(), LocalDate.now().plusDays(7));
        disponibles.forEach(habitacionDTO -> System.out.println("✓ Habitación disponible: " + habitacionDTO.getNumero()));

    }

    private void precargarEscenas (){
        // Escenas que se precargan (estáticas, no cambian)
        SceneManager.precargarEscena("menu", "/views/interfaces/menu-principal.fxml");
        SceneManager.precargarEscena("alta_huesped", "/views/interfaces/dar-alta-huesped.fxml");
        SceneManager.precargarEscena("estado_habs1", "/views/interfaces/estado-habitaciones/estado-habitaciones-1.fxml");
        SceneManager.precargarEscena("buscar_huesped1", "/views/interfaces/buscar-huesped/buscar-huesped-1.fxml");
        SceneManager.precargarEscena("reservar_hab1", "/views/interfaces/reservar-habitacion/reservar-habitacion-1.fxml");
        SceneManager.precargarEscena("ocupar_hab1", "/views/interfaces/ocupar-habitacion/ocupar-habitacion-1.fxml");

        // Escenas dinámicas solo se registran (se cargan bajo demanda)
        SceneManager.registrarEscena("estado_habs2", "/views/interfaces/estado-habitaciones/estado-habitaciones-2.fxml");
        SceneManager.registrarEscena("buscar_huesped2", "/views/interfaces/buscar-huesped/buscar-huesped-2.fxml");
        SceneManager.registrarEscena("reservar_hab2", "/views/interfaces/reservar-habitacion/reservar-habitacion-2.fxml");
        SceneManager.registrarEscena("ocupar_hab2", "/views/interfaces/ocupar-habitacion/ocupar-habitacion-2.fxml");
    }

    public static void cambiarA(String nombreEscena) {
        mainScene.setRoot(SceneManager.getRoot(nombreEscena));
    }
}

