package ar.utn.hotel.dao.impl;
import ar.utn.hotel.dao.PersonaDAO;
import ar.utn.hotel.model.Persona;
import utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class PersonaDAOImpl implements PersonaDAO {

    @Override
    public Persona obtenerPorId(Long id) {
        Transaction tx = null;
        Persona persona = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            persona = session.get(Persona.class, id);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }

        return persona;
    }

    @Override
    public Persona buscarPorNombreApellido(String nombre, String apellido) {
        Transaction tx = null;
        Persona persona = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Query<Persona> query = session.createQuery(
                    "FROM Persona p WHERE LOWER(p.nombre) = LOWER(:nombre) " +
                            "AND LOWER(p.apellido) = LOWER(:apellido)",
                    Persona.class
            );
            query.setParameter("nombre", nombre);
            query.setParameter("apellido", apellido);

            List<Persona> resultados = query.list();
            if (!resultados.isEmpty()) {
                persona = resultados.getFirst();
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }

        return persona;
    }

    @Override
    public Persona buscarPorTelefono(String telefono) {
        Transaction tx = null;
        Persona persona = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Query<Persona> query = session.createQuery(
                    "FROM Persona p WHERE p.telefono = :telefono",
                    Persona.class
            );
            query.setParameter("telefono", telefono);

            List<Persona> resultados = query.list();
            if (!resultados.isEmpty()) {
                persona = resultados.getFirst();
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }

        return persona;
    }

    @Override
    public List<Persona> obtenerTodas() {
        Transaction tx = null;
        List<Persona> personas = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            personas = session.createQuery("FROM Persona", Persona.class).list();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }

        return personas;
    }

    @Override
    public Persona guardar(Persona persona) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            s.persist(persona);
            tx.commit();
            return persona;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    @Override
    public void actualizar(Persona persona) {
        Transaction tx = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(persona);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    @Override
    public void eliminar(Long id) {
        Transaction tx = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Persona persona = session.get(Persona.class, id);
            if (persona != null) {
                session.remove(persona);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}