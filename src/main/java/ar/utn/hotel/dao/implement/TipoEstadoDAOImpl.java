package ar.utn.hotel.dao.implement;

import ar.utn.hotel.dao.interfaces.TipoEstadoDAO;
import ar.utn.hotel.model.TipoEstado;
import utils.HibernateUtil;
import enums.EstadoHab;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class TipoEstadoDAOImpl implements TipoEstadoDAO {

    @Override
    public TipoEstado guardar(TipoEstado tipoEstado) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(tipoEstado);
            transaction.commit();
            return tipoEstado;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al guardar tipo estado: " + e.getMessage(), e);
        }
    }

    @Override
    public TipoEstado buscarPorId(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(TipoEstado.class, id);
        }
    }

    @Override
    public TipoEstado buscarPorEstado(EstadoHab estado) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM TipoEstado te WHERE te.estado = :estado",
                            TipoEstado.class)
                    .setParameter("estado", estado)
                    .uniqueResult();
        }
    }

    @Override
    public List<TipoEstado> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM TipoEstado te ORDER BY te.estado",
                            TipoEstado.class)
                    .getResultList();
        }
    }

    @Override
    public void actualizar(TipoEstado tipoEstado) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(tipoEstado);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al actualizar tipo estado: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            TipoEstado tipoEstado = session.get(TipoEstado.class, id);
            if (tipoEstado != null) {
                session.remove(tipoEstado);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al eliminar tipo estado: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existeEstado(EstadoHab estado) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                            "SELECT COUNT(te) FROM TipoEstado te WHERE te.estado = :estado",
                            Long.class)
                    .setParameter("estado", estado)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }
}