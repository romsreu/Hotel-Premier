package ar.utn.hotel.dao.implement;

import ar.utn.hotel.dao.interfaces.HuespedDAO;
import ar.utn.hotel.dto.HuespedDTO;
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
    public Huesped obtenerPorId(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Huesped.class, id);
        }
    }

    @Override
    public List<Huesped> obtenerTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Huesped", Huesped.class).list();
        }
    }

    @Override
    public void actualizar(Huesped huesped) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(huesped);
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
            Huesped huesped = session.get(Huesped.class, id);
            if (huesped != null) {
                session.remove(huesped);
            }
            tx.commit();
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
            return count != null && count > 0;
        }
    }

    @Override
    public List<Huesped> buscarHuesped(HuespedDTO dto) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT h FROM Huesped h WHERE 1=1");

            boolean hayFiltros = false;

            if (dto.getNombre() != null && !dto.getNombre().trim().isEmpty()) {
                hql.append(" AND LOWER(h.nombre) LIKE LOWER(:nombre)");
                hayFiltros = true;
            }
            if (dto.getApellido() != null && !dto.getApellido().trim().isEmpty()) {
                hql.append(" AND LOWER(h.apellido) LIKE LOWER(:apellido)");
                hayFiltros = true;
            }
            if (dto.getNumeroDocumento() != null && !dto.getNumeroDocumento().trim().isEmpty()) {
                hql.append(" AND h.numeroDocumento = :numDoc");
                hayFiltros = true;
            }
            if (dto.getTipoDocumento() != null && !dto.getTipoDocumento().trim().isEmpty()) {
                hql.append(" AND h.tipoDocumento = :tipoDoc");
                hayFiltros = true;
            }
            if (dto.getTelefono() != null && !dto.getTelefono().trim().isEmpty()) {
                hql.append(" AND h.telefono = :telefono");
                hayFiltros = true;
            }
            if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
                hql.append(" AND LOWER(h.email) = LOWER(:email)");
                hayFiltros = true;
            }
            if (dto.getCuit() != null && !dto.getCuit().trim().isEmpty()) {
                hql.append(" AND h.cuit = :cuit");
                hayFiltros = true;
            }

            if (!hayFiltros) {
                return new ArrayList<>();
            }

            var query = s.createQuery(hql.toString(), Huesped.class);

            if (dto.getNombre() != null && !dto.getNombre().trim().isEmpty()) {
                query.setParameter("nombre", "%" + dto.getNombre().trim() + "%");
            }
            if (dto.getApellido() != null && !dto.getApellido().trim().isEmpty()) {
                query.setParameter("apellido", "%" + dto.getApellido().trim() + "%");
            }
            if (dto.getNumeroDocumento() != null && !dto.getNumeroDocumento().trim().isEmpty()) {
                query.setParameter("numDoc", dto.getNumeroDocumento().trim());
            }
            if (dto.getTipoDocumento() != null && !dto.getTipoDocumento().trim().isEmpty()) {
                query.setParameter("tipoDoc", dto.getTipoDocumento().trim());
            }
            if (dto.getTelefono() != null && !dto.getTelefono().trim().isEmpty()) {
                query.setParameter("telefono", dto.getTelefono().trim());
            }
            if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
                query.setParameter("email", dto.getEmail().trim());
            }
            if (dto.getCuit() != null && !dto.getCuit().trim().isEmpty()) {
                query.setParameter("cuit", dto.getCuit().trim());
            }

            return query.getResultList();
        }
    }
}