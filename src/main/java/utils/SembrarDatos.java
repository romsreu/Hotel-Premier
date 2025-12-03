package utils;

import ar.utn.hotel.model.EstadoHabitacion; // Asegúrate de importar esto
import ar.utn.hotel.model.Habitacion;
import ar.utn.hotel.model.TipoHabitacion;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.ArrayList;
import java.util.List;

public class SembrarDatos {

    public static void sembrar() {
        // 1. Cargar la configuración
        SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .buildSessionFactory();

        Session session = factory.openSession();

        try {
            // --- PARTE 1: VERIFICAR Y CREAR ESTADOS (¡CRUCIAL!) ---
            Long countEstados = (Long) session.createQuery("SELECT COUNT(e) FROM EstadoHabitacion e").uniqueResult();

            if (countEstados == 0) {
                System.out.println(">> Creando Estados de Habitación...");
                session.beginTransaction();

                // Crea los estados que tu código busca obligatoriamente
                crearEstado(session, "DISPONIBLE", "La habitación está lista para usarse.");
                crearEstado(session, "OCUPADA", "La habitación tiene huéspedes alojados.");
                crearEstado(session, "RESERVADA", "La habitación tiene una reserva futura confirmada.");
                crearEstado(session, "MANTENIMIENTO", "La habitación está en reparaciones.");
                crearEstado(session, "LIMPIEZA", "La habitación se está limpiando.");

                session.getTransaction().commit();
                System.out.println(">> Estados creados correctamente.");
            }

            // --- PARTE 2: CREAR HABITACIONES (Como antes) ---
            Long countHabs = (Long) session.createQuery("SELECT COUNT(h) FROM Habitacion h").uniqueResult();

            if (countHabs > 0) {
                System.out.println(">> Ya existen habitaciones. Carga finalizada.");
                return;
            }

            System.out.println(">> Base de datos vacía de habitaciones. Creando 48 habitaciones y 5 tipos...");
            session.beginTransaction();

            // 2. Crear los 5 Tipos
            TipoHabitacion simple = TipoHabitacion.builder().nombre("Individual Standard").costoNoche(50000.0).capacidadMaxima(1).descripcion("Habitación compacta ideal para viajeros solos.").build();
            TipoHabitacion doble = TipoHabitacion.builder().nombre("Doble Clásica").costoNoche(80000.0).capacidadMaxima(2).descripcion("Espacio confortable para parejas.").build();
            TipoHabitacion triple = TipoHabitacion.builder().nombre("Triple Familiar").costoNoche(120000.0).capacidadMaxima(3).descripcion("Habitación amplia para familias.").build();
            TipoHabitacion suite = TipoHabitacion.builder().nombre("Suite Ejecutiva").costoNoche(180000.0).capacidadMaxima(2).descripcion("Lujo y confort con área de trabajo.").build();
            TipoHabitacion presidencial = TipoHabitacion.builder().nombre("Suite Presidencial").costoNoche(350000.0).capacidadMaxima(4).descripcion("La máxima experiencia de lujo.").build();

            session.save(simple);
            session.save(doble);
            session.save(triple);
            session.save(suite);
            session.save(presidencial);

            // 3. Crear las 48 Habitaciones
            List<Habitacion> habitaciones = new ArrayList<>();
            crearPiso(session, 1, simple, doble, 1, 2, habitaciones);
            crearPiso(session, 2, doble, triple, 2, 3, habitaciones);
            crearPiso(session, 3, triple, suite, 3, 2, habitaciones);
            crearPiso(session, 4, suite, presidencial, 2, 4, habitaciones);

            session.getTransaction().commit();
            System.out.println(">> ¡Éxito! Se cargó todo.");

        } catch (Exception e) {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            System.err.println(">> Error al sembrar datos: " + e.getMessage());
        } finally {
            session.close();
            factory.close();
        }
    }

    private static void crearEstado(Session session, String nombre, String descripcion) {
        EstadoHabitacion estado = new EstadoHabitacion(); // O usa builder si tienes Lombok @Builder en EstadoHabitacion
        estado.setNombre(nombre);
        estado.setDescripcion(descripcion);
        session.save(estado);
    }

    private static void crearPiso(Session session, int piso,
                                  TipoHabitacion tipoA, TipoHabitacion tipoB,
                                  int capA, int capB,
                                  List<Habitacion> lista) {
        int inicio = (piso * 100) + 1;
        for (int i = 0; i < 12; i++) {
            int numeroHab = inicio + i;
            boolean esPar = (numeroHab % 2 == 0);
            TipoHabitacion tipo = esPar ? tipoB : tipoA;
            int capacidad = esPar ? capB : capA;

            Habitacion h = Habitacion.builder()
                    .numero(numeroHab)
                    .tipoHabitacion(tipo)
                    .capacidad(capacidad)
                    .descripcion("Habitación " + tipo.getNombre() + " - Piso " + piso)
                    .build();

            session.save(h);
            lista.add(h);
        }
    }
}