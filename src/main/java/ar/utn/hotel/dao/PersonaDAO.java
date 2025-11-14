package ar.utn.hotel.dao;

import ar.utn.hotel.model.Persona;
import ar.utn.hotel.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class PersonaDAO {

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
