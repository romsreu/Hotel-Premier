package ar.utn.hotel.dao.implement;

import ar.utn.hotel.dao.interfaces.TipoHabitacionDAO;
import ar.utn.hotel.model.TipoHabitacion;
import utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class TipoHabitacionDAOImpl implements TipoHabitacionDAO {

    @Override
    public TipoHabitacion guardar(TipoHabitacion tipo) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(tipo);
            transaction.commit();
            return tipo;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al guardar tipo de habitación: " + e.getMessage(), e);
        }
    }

    @Override
    public TipoHabitacion buscarPorId(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(TipoHabitacion.class, id);
        }
    }

    @Override
    public TipoHabitacion buscarPorNombre(String nombre) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM TipoHabitacion t WHERE t.nombre = :nombre", TipoHabitacion.class)
                    .setParameter("nombre", nombre)
                    .uniqueResult();
        }
    }

    @Override
    public List<TipoHabitacion> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM TipoHabitacion t ORDER BY t.nombre", TipoHabitacion.class)
                    .getResultList();
        }
    }

    @Override
    public void actualizar(TipoHabitacion tipo) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(tipo);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al actualizar tipo de habitación: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            TipoHabitacion tipo = session.get(TipoHabitacion.class, id);
            if (tipo != null) {
                session.remove(tipo);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al eliminar tipo de habitación: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existeNombre(String nombre) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                            "SELECT COUNT(t) FROM TipoHabitacion t WHERE t.nombre = :nombre", Long.class)
                    .setParameter("nombre", nombre)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    @Override
    public List<TipoHabitacion> buscarPorCapacidad(Integer capacidad) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM TipoHabitacion t WHERE t.capacidad >= :capacidad ORDER BY t.capacidad",
                            TipoHabitacion.class)
                    .setParameter("capacidad", capacidad)
                    .getResultList();
        }
    }

    @Override
    public List<TipoHabitacion> buscarPorRangoPrecio(Double precioMin, Double precioMax) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM TipoHabitacion t WHERE t.costoNoche BETWEEN :min AND :max ORDER BY t.costoNoche",
                            TipoHabitacion.class)
                    .setParameter("min", precioMin)
                    .setParameter("max", precioMax)
                    .getResultList();
        }
    }
}