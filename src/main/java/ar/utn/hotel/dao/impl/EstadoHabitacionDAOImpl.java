package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.EstadoHabitacionDAO;
import ar.utn.hotel.model.EstadoHabitacion;
import ar.utn.hotel.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class EstadoHabitacionDAOImpl implements EstadoHabitacionDAO {

    @Override
    public EstadoHabitacion guardar(EstadoHabitacion estado) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(estado);
            transaction.commit();
            return estado;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al guardar estado: " + e.getMessage(), e);
        }
    }

    @Override
    public EstadoHabitacion buscarPorId(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(EstadoHabitacion.class, id);
        }
    }

    @Override
    public EstadoHabitacion buscarPorEstado(enums.EstadoHabitacion estado) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM EstadoHabitacion e WHERE e.estado = :estado",
                            EstadoHabitacion.class)
                    .setParameter("estado", estado)
                    .uniqueResult();
        }
    }

    @Override
    public List<EstadoHabitacion> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM EstadoHabitacion e ORDER BY e.estado",
                            EstadoHabitacion.class)
                    .getResultList();
        }
    }

    @Override
    public void actualizar(EstadoHabitacion estado) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(estado);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al actualizar estado: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            EstadoHabitacion estado = session.get(EstadoHabitacion.class, id);
            if (estado != null) {
                session.remove(estado);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al eliminar estado: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existeEstado(enums.EstadoHabitacion estado) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                            "SELECT COUNT(e) FROM EstadoHabitacion e WHERE e.estado = :estado",
                            Long.class)
                    .setParameter("estado", estado)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }
}