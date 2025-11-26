package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.HuespedDAO;
import ar.utn.hotel.dto.BuscarHuespedDTO;
import ar.utn.hotel.model.Huesped;
import utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class HuespedDAOImpl implements HuespedDAO {

    @Override
    public Huesped guardar(Huesped huesped) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            s.persist(huesped);
            tx.commit();
            return huesped;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    @Override
    public boolean existePorDocumento(String numeroDocumento, String tipoDocumento) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Long count = s.createQuery("""
                    SELECT COUNT(h) FROM Huesped h
                    WHERE h.numeroDocumento = :numDoc
                      AND h.tipoDocumento = :tipoDoc
                    """, Long.class)
                    .setParameter("numDoc", numeroDocumento)
                    .setParameter("tipoDoc", tipoDocumento)
                    .uniqueResult();
            boolean bandera = count != null && count > 0;
            if(bandera){
                System.out.println("--------");
                System.out.println("El numero de documento ya existe");
                System.out.println("--------");
            }
            return bandera;
        }
    }

    // HuespedDAOImpl.java
    @Override
    public List<Huesped> buscarHuesped(BuscarHuespedDTO dto) {
        // Validar que al menos haya un parámetro
        if ((dto.getNombre() == null || dto.getNombre().trim().isEmpty()) &&
                (dto.getApellido() == null || dto.getApellido().trim().isEmpty()) &&
                (dto.getTipoDocumento() == null || dto.getTipoDocumento().trim().isEmpty()) &&
                (dto.getNumeroDocumento() == null || dto.getNumeroDocumento().trim().isEmpty())) {

            System.out.println("Error: Debe proporcionar al menos un criterio de búsqueda");
            return new ArrayList<>(); // Retorna lista vacía
        }

        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            // Construir la query dinámicamente
            StringBuilder hql = new StringBuilder("SELECT h FROM Huesped h WHERE 1=1");

            // Agregar condiciones solo si los parámetros no son nulos
            if (dto.getNombre() != null && !dto.getNombre().trim().isEmpty()) {
                hql.append(" AND LOWER(h.nombre) LIKE LOWER(:nombre)");
            }
            if (dto.getApellido() != null && !dto.getApellido().trim().isEmpty()) {
                hql.append(" AND LOWER(h.apellido) LIKE LOWER(:apellido)");
            }
            if (dto.getTipoDocumento() != null && !dto.getTipoDocumento().trim().isEmpty()) {
                hql.append(" AND h.tipoDocumento = :tipoDoc");
            }
            if (dto.getNumeroDocumento() != null && !dto.getNumeroDocumento().trim().isEmpty()) {
                hql.append(" AND h.numeroDocumento = :numDoc");
            }

            // Crear la query
            var query = s.createQuery(hql.toString(), Huesped.class);

            // Setear parámetros solo si no son nulos
            if (dto.getNombre() != null && !dto.getNombre().trim().isEmpty()) {
                query.setParameter("nombre", "%" + dto.getNombre()  + "%");
            }
            if (dto.getApellido() != null && !dto.getApellido().trim().isEmpty()) {
                query.setParameter("apellido", "%" + dto.getApellido() + "%");
            }
            if (dto.getTipoDocumento() != null && !dto.getTipoDocumento().trim().isEmpty()) {
                query.setParameter("tipoDoc", dto.getTipoDocumento());
            }
            if (dto.getNumeroDocumento() != null && !dto.getNumeroDocumento().trim().isEmpty()) {
                query.setParameter("numDoc", dto.getNumeroDocumento());
            }

            List<Huesped> resultados = query.getResultList();

            if (resultados.isEmpty()) {
                System.out.println("No se encontraron huéspedes con los criterios especificados");
            }

            return resultados;
        }
    }
}