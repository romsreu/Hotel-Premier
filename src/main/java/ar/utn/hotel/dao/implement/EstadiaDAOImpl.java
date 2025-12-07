package ar.utn.hotel.dao.implement;

import ar.utn.hotel.dao.interfaces.EstadiaDAO;
import ar.utn.hotel.model.Estadia;
import ar.utn.hotel.model.Reserva;
import utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class EstadiaDAOImpl implements EstadiaDAO {

    @Override
    public Estadia guardar(Estadia estadia) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(estadia);
            transaction.commit();
            return estadia;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al guardar estadía: " + e.getMessage(), e);
        }
    }

    @Override
    public Estadia buscarPorId(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT e FROM Estadia e " +
                                    "LEFT JOIN FETCH e.reserva r " +
                                    "LEFT JOIN FETCH r.huesped " +
                                    "LEFT JOIN FETCH e.habitacion " +
                                    "WHERE e.idEstadia = :id",
                            Estadia.class)
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }

    @Override
    public Estadia buscarPorReserva(Long idReserva) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT e FROM Estadia e " +
                                    "LEFT JOIN FETCH e.habitacion " +
                                    "WHERE e.reserva.id = :idReserva",
                            Estadia.class)
                    .setParameter("idReserva", idReserva)
                    .uniqueResult();
        }
    }

    @Override
    public List<Estadia> listarTodas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT e FROM Estadia e " +
                                    "LEFT JOIN FETCH e.reserva r " +
                                    "LEFT JOIN FETCH r.huesped " +
                                    "LEFT JOIN FETCH e.habitacion " +
                                    "ORDER BY e.fechaInicio DESC",
                            Estadia.class)
                    .getResultList();
        }
    }

    @Override
    public List<Estadia> listarActivas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT e FROM Estadia e " +
                                    "LEFT JOIN FETCH e.reserva r " +
                                    "LEFT JOIN FETCH r.huesped " +
                                    "LEFT JOIN FETCH e.habitacion " +
                                    "WHERE e.horaCheckOut IS NULL " +
                                    "ORDER BY e.fechaInicio DESC",
                            Estadia.class)
                    .getResultList();
        }
    }

    @Override
    public List<Estadia> listarPorHabitacion(Integer numeroHabitacion) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT e FROM Estadia e " +
                                    "LEFT JOIN FETCH e.reserva r " +
                                    "LEFT JOIN FETCH r.huesped " +
                                    "WHERE e.habitacion.numero = :numero " +
                                    "ORDER BY e.fechaInicio DESC",
                            Estadia.class)
                    .setParameter("numero", numeroHabitacion)
                    .getResultList();
        }
    }

    @Override
    public List<Estadia> listarPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT e FROM Estadia e " +
                                    "LEFT JOIN FETCH e.reserva r " +
                                    "LEFT JOIN FETCH r.huesped " +
                                    "LEFT JOIN FETCH e.habitacion " +
                                    "WHERE e.fechaInicio <= :fechaFin " +
                                    "AND e.fechaFin >= :fechaInicio " +
                                    "ORDER BY e.fechaInicio",
                            Estadia.class)
                    .setParameter("fechaInicio", fechaInicio)
                    .setParameter("fechaFin", fechaFin)
                    .getResultList();
        }
    }

    @Override
    public void actualizar(Estadia estadia) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(estadia);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al actualizar estadía: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Estadia estadia = session.get(Estadia.class, id);
            if (estadia != null) {
                session.remove(estadia);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al eliminar estadía: " + e.getMessage(), e);
        }
    }

    @Override
    public Estadia crearDesdeReserva(Reserva reserva) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Verificar si ya existe una estadía para esta reserva
            Estadia estadiaExistente = session.createQuery(
                            "FROM Estadia e WHERE e.reserva.id = :idReserva",
                            Estadia.class)
                    .setParameter("idReserva", reserva.getId())
                    .uniqueResult();

            if (estadiaExistente != null) {
                throw new IllegalStateException(
                        "Ya existe una estadía para la reserva #" + reserva.getId()
                );
            }

            // Crear nueva estadía
            Estadia estadia = Estadia.builder()
                    .reserva(reserva)
                    .habitacion(reserva.getHabitacion())
                    .fechaInicio(reserva.getFechaInicio())
                    .fechaFin(reserva.getFechaFin())
                    .horaCheckIn(LocalDateTime.now())
                    .build();

            session.persist(estadia);
            transaction.commit();

            return estadia;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al crear estadía desde reserva: " + e.getMessage(), e);
        }
    }
}