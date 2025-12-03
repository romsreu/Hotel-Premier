package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.EstadoHabitacionDAO;
import ar.utn.hotel.model.EstadoHabitacion;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

public class EstadoHabitacionDAOImpl implements EstadoHabitacionDAO {

    @Override
    public EstadoHabitacion obtenerPorNombre(String nombre) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM EstadoHabitacion WHERE nombre = :nombre";
            return session.createQuery(hql, EstadoHabitacion.class)
                    .setParameter("nombre", nombre)
                    .uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar estado por nombre: " + nombre, e);
        }
    }

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
}