package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.HabitacionDAO;
import enums.EstadoHabitacion;
import ar.utn.hotel.model.Habitacion;
import utils.HibernateUtil;
import enums.TipoHabitacion;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class HabitacionDAOImpl implements HabitacionDAO {

    @Override
    public Habitacion guardar(Habitacion habitacion) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(habitacion);
            transaction.commit();
            return habitacion;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al guardar habitación: " + e.getMessage(), e);
        }
    }

    @Override
    public Habitacion buscarPorNumero(Integer numero) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Habitacion.class, numero);
        }
    }

    @Override
    public List<Habitacion> listarTodas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Habitacion h ORDER BY h.numero", Habitacion.class)
                    .getResultList();
        }
    }

    @Override
    public List<Habitacion> listarPorRangoDeFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        Transaction transaction = null;
        List<Habitacion> habitacionesDisponibles = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Buscar habitaciones que NO tienen reservas en el rango de fechas especificado
            String hql = "SELECT h FROM Habitacion h WHERE h.numero NOT IN " +
                    "(SELECT DISTINCT hab.numero FROM Reserva r " +
                    "JOIN r.habitaciones hab " +
                    "WHERE r.fechaInicio <= :fechaFin " +
                    "AND r.fechaFin >= :fechaInicio) " +
                    "AND h.estado = :estado " +
                    "ORDER BY h.numero";

            var query = session.createQuery(hql, Habitacion.class);
            query.setParameter("fechaInicio", fechaInicio);
            query.setParameter("fechaFin", fechaFin);
            query.setParameter("estado", EstadoHabitacion.DISPONIBLE);

            habitacionesDisponibles = query.list();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }

        return habitacionesDisponibles;
    }

    @Override
    public void reservarHabitaciones(Set<Integer> numerosHabitaciones) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            for (Integer numero : numerosHabitaciones) {
                Habitacion habitacion = session.get(Habitacion.class, numero);

                if (habitacion == null) {
                    throw new IllegalArgumentException(
                            "La habitación número " + numero + " no existe"
                    );
                }

                if (habitacion.getEstado() != EstadoHabitacion.DISPONIBLE) {
                    throw new IllegalStateException(
                            "La habitación número " + numero + " no está disponible. " +
                                    "Estado actual: " + habitacion.getEstado()
                    );
                }

                // Cambiar estado a RESERVADA
                habitacion.setEstado(EstadoHabitacion.RESERVADA);
                session.merge(habitacion);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al reservar habitaciones: " + e.getMessage());
        }
    }

    @Override
    public List<Habitacion> buscarPorTipo(TipoHabitacion tipo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Habitacion h WHERE h.tipo = :tipo ORDER BY h.numero", Habitacion.class)
                    .setParameter("tipo", tipo)
                    .getResultList();
        }
    }

    @Override
    public List<Habitacion> buscarPorEstado(EstadoHabitacion estado) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Habitacion h WHERE h.estado = :estado ORDER BY h.numero", Habitacion.class)
                    .setParameter("estado", estado)
                    .getResultList();
        }
    }

    @Override
    public List<Habitacion> buscarDisponibles() {
        return buscarPorEstado(EstadoHabitacion.DISPONIBLE);
    }

    @Override
    public void actualizar(Habitacion habitacion) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(habitacion);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al actualizar habitación: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Integer numero) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Habitacion habitacion = session.get(Habitacion.class, numero);
            if (habitacion != null) {
                session.remove(habitacion);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al eliminar habitación: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existeNumero(String numero) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                            "SELECT COUNT(h) FROM Habitacion h WHERE h.numero = :numero", Long.class)
                    .setParameter("numero", numero)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    @Override
    public Long contarPorTipo(TipoHabitacion tipo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT COUNT(h) FROM Habitacion h WHERE h.tipo = :tipo", Long.class)
                    .setParameter("tipo", tipo)
                    .uniqueResult();
        }
    }
}