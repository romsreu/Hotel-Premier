package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.PersonaDAO;
import ar.utn.hotel.model.Persona;
import ar.utn.hotel.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class PersonaDAOImpl implements PersonaDAO {

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
}