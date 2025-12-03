package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.EstadoHabitacionDAO;
import ar.utn.hotel.dao.PersonaDAO;
import ar.utn.hotel.dao.ReservaDAO;
import ar.utn.hotel.model.*;
import ar.utn.hotel.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class ReservaDAOImpl implements ReservaDAO {

    private final PersonaDAO personaDAO;
    private final EstadoHabitacionDAO estadoHabitacionDAO;

    public ReservaDAOImpl(PersonaDAO personaDAO, EstadoHabitacionDAO estadoHabitacionDAO) {
        this.personaDAO = personaDAO;
        this.estadoHabitacionDAO = estadoHabitacionDAO;
    }

    @Override
    public Reserva crearReserva(Long idPersona, LocalDate fechaInicio, LocalDate fechaFin,
                                Set<Integer> numerosHabitaciones) {
        Transaction transaction = null;
        Reserva reserva = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Verificar que la persona existe
            Persona persona = session.get(Persona.class, idPersona);
            if (persona == null) {
                throw new RuntimeException("Error: La persona no se encuentra cargada en el sistema.");
            }

            // Obtener el estado RESERVADA del catálogo
            EstadoHabitacion estadoReservada = estadoHabitacionDAO.buscarPorEstado(
                    enums.EstadoHabitacion.RESERVADA
            );

            if (estadoReservada == null) {
                throw new IllegalStateException("No existe el estado RESERVADA en el catálogo");
            }

            // Crear la reserva
            reserva = Reserva.builder()
                    .persona(persona)
                    .fechaInicio(fechaInicio)
                    .fechaFin(fechaFin)
                    .build();

            // Obtener y agregar las habitaciones, cambiando su estado
            for (Integer numero : numerosHabitaciones) {
                Habitacion habitacion = session.createQuery(
                                "SELECT DISTINCT h FROM Habitacion h " +
                                        "LEFT JOIN FETCH h.historialEstados " +
                                        "WHERE h.numero = :numero",
                                Habitacion.class)
                        .setParameter("numero", numero)
                        .uniqueResult();

                if (habitacion == null) {
                    throw new IllegalArgumentException(
                            "La habitación número " + numero + " no existe"
                    );
                }

                // Verificar que la habitación esté disponible
                RegistroEstadoHabitacion registroActual = habitacion.getEstadoActual();
                if (registroActual != null &&
                        registroActual.getEstado().getEstado() != enums.EstadoHabitacion.DISPONIBLE) {
                    throw new IllegalStateException(
                            "La habitación número " + numero + " no está disponible. " +
                                    "Estado actual: " + registroActual.getEstado().getEstado()
                    );
                }

                // Cerrar el registro actual
                if (registroActual != null) {
                    registroActual.setFechaHasta(fechaInicio.minusDays(1));
                    session.merge(registroActual);
                }

                // Crear nuevo registro con estado RESERVADA
                RegistroEstadoHabitacion nuevoRegistro = RegistroEstadoHabitacion.builder()
                        .habitacion(habitacion)
                        .estado(estadoReservada)
                        .fechaDesde(fechaInicio)
                        .fechaHasta(fechaFin)
                        .build();

                habitacion.getHistorialEstados().add(nuevoRegistro);
                session.persist(nuevoRegistro);

                // Agregar habitación a la reserva
                reserva.agregarHabitacion(habitacion);
            }

            // Persistir la reserva
            session.persist(reserva);

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al crear reserva: " + e.getMessage(), e);
        }

        return reserva;
    }

    @Override
    public Reserva crearReservaPorNombreApellido(String nombre, String apellido,
                                                 LocalDate fechaInicio, LocalDate fechaFin,
                                                 Set<Integer> numerosHabitaciones) {
        Persona persona = personaDAO.buscarPorNombreApellido(nombre, apellido);

        if (persona == null) {
            throw new RuntimeException("Error: La persona no se encuentra cargada en el sistema.");
        }

        return crearReserva(persona.getId(), fechaInicio, fechaFin, numerosHabitaciones);
    }

    @Override
    public Reserva crearReservaPorTelefono(String telefono, LocalDate fechaInicio,
                                           LocalDate fechaFin, Set<Integer> numerosHabitaciones) {
        Persona persona = personaDAO.buscarPorTelefono(telefono);

        if (persona == null) {
            throw new RuntimeException("Error: La persona no se encuentra cargada en el sistema.");
        }

        return crearReserva(persona.getId(), fechaInicio, fechaFin, numerosHabitaciones);
    }

    @Override
    public Reserva obtenerPorId(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT r FROM Reserva r " +
                                    "LEFT JOIN FETCH r.habitaciones h " +
                                    "LEFT JOIN FETCH h.historialEstados " +
                                    "LEFT JOIN FETCH r.persona " +
                                    "WHERE r.id = :id",
                            Reserva.class)
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }

    @Override
    public List<Reserva> obtenerTodas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT r FROM Reserva r " +
                                    "LEFT JOIN FETCH r.habitaciones " +
                                    "LEFT JOIN FETCH r.persona " +
                                    "ORDER BY r.fechaInicio DESC",
                            Reserva.class)
                    .getResultList();
        }
    }

    @Override
    public List<Reserva> obtenerPorPersona(Long idPersona) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT r FROM Reserva r " +
                                    "LEFT JOIN FETCH r.habitaciones " +
                                    "WHERE r.persona.id = :idPersona " +
                                    "ORDER BY r.fechaInicio DESC",
                            Reserva.class)
                    .setParameter("idPersona", idPersona)
                    .getResultList();
        }
    }

    @Override
    public List<Reserva> obtenerPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT r FROM Reserva r " +
                                    "LEFT JOIN FETCH r.habitaciones " +
                                    "LEFT JOIN FETCH r.persona " +
                                    "WHERE r.fechaInicio <= :fechaFin " +
                                    "AND r.fechaFin >= :fechaInicio " +
                                    "ORDER BY r.fechaInicio",
                            Reserva.class)
                    .setParameter("fechaInicio", fechaInicio)
                    .setParameter("fechaFin", fechaFin)
                    .getResultList();
        }
    }

    @Override
    public List<Reserva> obtenerPorHabitacion(Integer numeroHabitacion) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT r FROM Reserva r " +
                                    "JOIN r.habitaciones h " +
                                    "LEFT JOIN FETCH r.persona " +
                                    "WHERE h.numero = :numeroHabitacion " +
                                    "ORDER BY r.fechaInicio DESC",
                            Reserva.class)
                    .setParameter("numeroHabitacion", numeroHabitacion)
                    .getResultList();
        }
    }

    @Override
    public void actualizar(Reserva reserva) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(reserva);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al actualizar reserva: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Reserva reserva = session.get(Reserva.class, id);
            if (reserva != null) {
                // Liberar las habitaciones (cambiar estado a DISPONIBLE)
                EstadoHabitacion estadoDisponible = estadoHabitacionDAO.buscarPorEstado(
                       enums.EstadoHabitacion.DISPONIBLE
                );

                if (estadoDisponible != null) {
                    for (Habitacion habitacion : reserva.getHabitaciones()) {
                        RegistroEstadoHabitacion registroActual = habitacion.getEstadoActual();
                        if (registroActual != null) {
                            registroActual.setFechaHasta(LocalDate.now());
                            session.merge(registroActual);
                        }

                        RegistroEstadoHabitacion nuevoRegistro = RegistroEstadoHabitacion.builder()
                                .habitacion(habitacion)
                                .estado(estadoDisponible)
                                .fechaDesde(LocalDate.now())
                                .build();

                        habitacion.getHistorialEstados().add(nuevoRegistro);
                        session.persist(nuevoRegistro);
                    }
                }

                session.remove(reserva);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al eliminar reserva: " + e.getMessage(), e);
        }
    }
}