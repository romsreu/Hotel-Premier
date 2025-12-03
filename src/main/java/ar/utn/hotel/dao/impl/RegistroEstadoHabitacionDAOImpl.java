package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.RegistroEstadoHabitacionDAO;
import ar.utn.hotel.model.RegistroEstadoHabitacion;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.time.LocalDate;
import java.util.ArrayList; // <--- Agregado
import java.util.List;      // <--- Agregado

public class RegistroEstadoHabitacionDAOImpl implements RegistroEstadoHabitacionDAO {

    @Override
    public void persist(RegistroEstadoHabitacion registro) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(registro);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al persistir registro de estado: " + e.getMessage(), e);
        }
    }

    @Override
    public void merge(RegistroEstadoHabitacion registro) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(registro);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al actualizar registro de estado: " + e.getMessage(), e);
        }
    }

    @Override
    public RegistroEstadoHabitacion obtenerRegistroActual(Integer numeroHabitacion) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM RegistroEstadoHabitacion WHERE habitacion.numero = :numero AND fechaFin IS NULL";
            return session.createQuery(hql, RegistroEstadoHabitacion.class)
                    .setParameter("numero", numeroHabitacion)
                    .uniqueResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public RegistroEstadoHabitacion obtenerRegistroPorFecha(Integer numeroHabitacion, LocalDate fecha) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM RegistroEstadoHabitacion " +
                    "WHERE habitacion.numero = :numero " +
                    "AND fechaInicio <= :fecha " +
                    "AND (fechaFin IS NULL OR fechaFin >= :fecha)";

            return session.createQuery(hql, RegistroEstadoHabitacion.class)
                    .setParameter("numero", numeroHabitacion)
                    .setParameter("fecha", fecha)
                    .uniqueResult();
        } catch (Exception e) {
            return null;
        }
    }

    // --- NUEVO MÉTODO OPTIMIZADO ---
    @Override
    public List<RegistroEstadoHabitacion> buscarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Esta consulta trae TODOS los registros que se solapan con el rango de fechas solicitado
            // de una sola vez, evitando hacer cientos de consultas pequeñas.
            String hql = "FROM RegistroEstadoHabitacion r " +
                    "WHERE r.fechaInicio <= :fechaFin " +
                    "AND (r.fechaFin IS NULL OR r.fechaFin >= :fechaInicio)";

            return session.createQuery(hql, RegistroEstadoHabitacion.class)
                    .setParameter("fechaInicio", fechaInicio)
                    .setParameter("fechaFin", fechaFin)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>(); // Retornamos lista vacía para evitar NullPointerException
        }
    }
}