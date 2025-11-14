package ar.utn.hotel.dao;

import ar.utn.hotel.model.Direccion;
import ar.utn.hotel.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class DireccionDAO {

    public Direccion guardar(Direccion direccion) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            s.persist(direccion);
            tx.commit();
            return direccion;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public Direccion buscarPorDatos(
            String calle, String numero, String depto, String piso,
            String codPostal, String localidad, String provincia, String pais
    ) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("""
                    SELECT d FROM Direccion d
                    WHERE d.calle = :calle
                      AND d.numero = :numero
                      AND d.departamento = :depto
                      AND d.piso = :piso
                      AND d.codPostal = :cp
                      AND d.localidad = :loc
                      AND d.provincia = :prov
                      AND d.pais = :pais
                    """, Direccion.class)
                    .setParameter("calle", calle)
                    .setParameter("numero", numero)
                    .setParameter("depto", depto)
                    .setParameter("piso", piso)
                    .setParameter("cp", codPostal)
                    .setParameter("loc", localidad)
                    .setParameter("prov", provincia)
                    .setParameter("pais", pais)
                    .uniqueResult();
        }
    }
}
