package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.HabitacionDAO;
import ar.utn.hotel.dao.PersonaDAO;
import ar.utn.hotel.dao.ReservaDAO;
import ar.utn.hotel.model.Habitacion;
import ar.utn.hotel.model.Persona;
import ar.utn.hotel.model.Reserva;
import utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class ReservaDAOImpl implements ReservaDAO {

    private final PersonaDAO personaDAO;
    private final HabitacionDAO habitacionDAO;

    public ReservaDAOImpl(PersonaDAO personaDAO, HabitacionDAO habitacionDAO) {
        this.personaDAO = personaDAO;
        this.habitacionDAO = habitacionDAO;
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
            if (persona == null) throw new RuntimeException("Error: La persona no se encuentra cargada en el sistema.");

            // Crear la reserva
            reserva = Reserva.builder()
                    .persona(persona)
                    .fechaInicio(fechaInicio)
                    .fechaFin(fechaFin)
                    .build();

            // Obtener y agregar las habitaciones
            for (Integer numero : numerosHabitaciones) {
                Habitacion habitacion = session.get(Habitacion.class, numero);
                if (habitacion != null) {
                    reserva.agregarHabitacion(habitacion);
                } else {
                    throw new IllegalArgumentException(
                            "La habitación número " + numero + " no existe"
                    );
                }
            }

            session.persist(reserva);

            // Cambiar el estado de las habitaciones a RESERVADA
            habitacionDAO.reservarHabitaciones(numerosHabitaciones);

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.out.println(e.getMessage());
        }
        return reserva;
    }

    @Override
    public Reserva crearReservaPorNombreApellido(String nombre, String apellido,
                                                 LocalDate fechaInicio, LocalDate fechaFin,
                                                 Set<Integer> numerosHabitaciones) {
        Persona persona = personaDAO.buscarPorNombreApellido(nombre, apellido);

        if (persona == null) throw new RuntimeException("Error: La persona no se encuentra cargada en el sistema.");

        return crearReserva(persona.getId(), fechaInicio, fechaFin, numerosHabitaciones);
    }

    @Override
    public Reserva crearReservaPorTelefono(String telefono, LocalDate fechaInicio,
                                           LocalDate fechaFin, Set<Integer> numerosHabitaciones) {
        Persona persona = personaDAO.buscarPorTelefono(telefono);

        if (persona == null) throw new RuntimeException("Error: La persona no se encuentra cargada en el sistema.");

        return crearReserva(persona.getId(), fechaInicio, fechaFin, numerosHabitaciones);
    }

    @Override
    public Reserva obtenerPorId(Long id) {
        Transaction transaction = null;
        Reserva reserva = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            reserva = session.get(Reserva.class, id);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }

        return reserva;
    }

    @Override
    public List<Reserva> obtenerTodas() {
        Transaction transaction = null;
        List<Reserva> reservas = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Query<Reserva> query = session.createQuery(
                    "SELECT DISTINCT r FROM Reserva r " +
                            "LEFT JOIN FETCH r.habitaciones " +
                            "LEFT JOIN FETCH r.persona",
                    Reserva.class
            );
            reservas = query.list();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }

        return reservas;
    }

    @Override
    public List<Reserva> obtenerPorPersona(Long idPersona) {
        Transaction transaction = null;
        List<Reserva> reservas = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Query<Reserva> query = session.createQuery(
                    "SELECT DISTINCT r FROM Reserva r " +
                            "LEFT JOIN FETCH r.habitaciones " +
                            "WHERE r.persona.id = :idPersona",
                    Reserva.class
            );
            query.setParameter("idPersona", idPersona);
            reservas = query.list();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }

        return reservas;
    }

    @Override
    public List<Reserva> obtenerPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        Transaction transaction = null;
        List<Reserva> reservas = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Query<Reserva> query = session.createQuery(
                    "FROM Reserva r WHERE r.fechaInicio <= :fechaFin " +
                            "AND r.fechaFin >= :fechaInicio",
                    Reserva.class
            );
            query.setParameter("fechaInicio", fechaInicio);
            query.setParameter("fechaFin", fechaFin);

            reservas = query.list();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }

        return reservas;
    }

    @Override
    public List<Reserva> obtenerPorHabitacion(Integer numeroHabitacion) {
        Transaction transaction = null;
        List<Reserva> reservas = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Query<Reserva> query = session.createQuery(
                    "SELECT DISTINCT r FROM Reserva r " +
                            "JOIN r.habitaciones h " +
                            "WHERE h.numero = :numeroHabitacion",
                    Reserva.class
            );
            query.setParameter("numeroHabitacion", numeroHabitacion);
            reservas = query.list();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }

        return reservas;
    }

    @Override
    public void actualizar(Reserva reserva) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(reserva);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public void eliminar(Long id) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Reserva reserva = session.get(Reserva.class, id);
            if (reserva != null) {
                session.remove(reserva);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }
}