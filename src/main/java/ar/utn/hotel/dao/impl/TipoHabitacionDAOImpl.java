package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.TipoHabitacionDAO;
import ar.utn.hotel.model.TipoHabitacion;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.util.List;

public class TipoHabitacionDAOImpl implements TipoHabitacionDAO {

    @Override
    public TipoHabitacion obtenerPorNombre(String nombre) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM TipoHabitacion WHERE nombre = :nombre";
            return session.createQuery(hql, TipoHabitacion.class)
                    .setParameter("nombre", nombre)
                    .uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar tipo de habitación por nombre: " + nombre, e);
        }
    }

    @Override
    public TipoHabitacion obtenerPorId(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(TipoHabitacion.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar tipo de habitación por id: " + id, e);
        }
    }

    @Override
    public List<TipoHabitacion> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM TipoHabitacion ORDER BY nombre", TipoHabitacion.class)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error al listar tipos de habitación", e);
        }
    }

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
}