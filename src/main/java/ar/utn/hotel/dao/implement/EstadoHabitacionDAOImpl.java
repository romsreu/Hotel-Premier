package ar.utn.hotel.dao.implement;

import ar.utn.hotel.dao.interfaces.EstadoHabitacionDAO;
import ar.utn.hotel.model.EstadoHabitacion;
import utils.HibernateUtil;
import enums.EstadoHab;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EstadoHabitacionDAOImpl implements EstadoHabitacionDAO {

    @Override
    public EstadoHabitacion guardar(EstadoHabitacion estadoHabitacion) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(estadoHabitacion);
            transaction.commit();
            return estadoHabitacion;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al guardar estado habitación: " + e.getMessage(), e);
        }
    }

    @Override
    public EstadoHabitacion buscarPorId(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT eh FROM EstadoHabitacion eh " +
                                    "LEFT JOIN FETCH eh.tipoEstado " +
                                    "LEFT JOIN FETCH eh.habitacion " +
                                    "WHERE eh.id = :id",
                            EstadoHabitacion.class)
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }

    @Override
    public List<EstadoHabitacion> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT eh FROM EstadoHabitacion eh " +
                                    "LEFT JOIN FETCH eh.tipoEstado " +
                                    "LEFT JOIN FETCH eh.habitacion " +
                                    "ORDER BY eh.habitacion.numero, eh.fechaDesde DESC",
                            EstadoHabitacion.class)
                    .getResultList();
        }
    }

    @Override
    public List<EstadoHabitacion> listarPorHabitacion(Integer numeroHabitacion) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT eh FROM EstadoHabitacion eh " +
                                    "LEFT JOIN FETCH eh.tipoEstado " +
                                    "WHERE eh.habitacion.numero = :numero " +
                                    "ORDER BY eh.fechaDesde DESC",
                            EstadoHabitacion.class)
                    .setParameter("numero", numeroHabitacion)
                    .getResultList();
        }
    }

    @Override
    public List<EstadoHabitacion> listarActivos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT eh FROM EstadoHabitacion eh " +
                                    "LEFT JOIN FETCH eh.tipoEstado " +
                                    "LEFT JOIN FETCH eh.habitacion " +
                                    "WHERE eh.fechaHasta IS NULL OR eh.fechaHasta >= :hoy " +
                                    "ORDER BY eh.habitacion.numero",
                            EstadoHabitacion.class)
                    .setParameter("hoy", LocalDate.now())
                    .getResultList();
        }
    }

    @Override
    public List<EstadoHabitacion> listarPorTipoEstado(EstadoHab estado) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT eh FROM EstadoHabitacion eh " +
                                    "LEFT JOIN FETCH eh.tipoEstado te " +
                                    "LEFT JOIN FETCH eh.habitacion " +
                                    "WHERE te.estado = :estado " +
                                    "AND (eh.fechaHasta IS NULL OR eh.fechaHasta >= :hoy) " +
                                    "ORDER BY eh.habitacion.numero",
                            EstadoHabitacion.class)
                    .setParameter("estado", estado)
                    .setParameter("hoy", LocalDate.now())
                    .getResultList();
        }
    }

    @Override
    public EstadoHabitacion obtenerEstadoActual(Integer numeroHabitacion) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT eh FROM EstadoHabitacion eh " +
                                    "LEFT JOIN FETCH eh.tipoEstado " +
                                    "WHERE eh.habitacion.numero = :numero " +
                                    "AND eh.fechaDesde <= :hoy " +
                                    "AND (eh.fechaHasta IS NULL OR eh.fechaHasta >= :hoy) " +
                                    "ORDER BY eh.fechaDesde DESC",
                            EstadoHabitacion.class)
                    .setParameter("numero", numeroHabitacion)
                    .setParameter("hoy", LocalDate.now())
                    .setMaxResults(1)
                    .uniqueResult();
        }
    }

    @Override
    public EstadoHabitacion obtenerEstadoEn(Integer numeroHabitacion, LocalDate fecha) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT eh FROM EstadoHabitacion eh " +
                                    "LEFT JOIN FETCH eh.tipoEstado " +
                                    "WHERE eh.habitacion.numero = :numero " +
                                    "AND eh.fechaDesde <= :fecha " +
                                    "AND (eh.fechaHasta IS NULL OR eh.fechaHasta >= :fecha) " +
                                    "ORDER BY eh.fechaDesde DESC",
                            EstadoHabitacion.class)
                    .setParameter("numero", numeroHabitacion)
                    .setParameter("fecha", fecha)
                    .setMaxResults(1)
                    .uniqueResult();
        }
    }

    @Override
    public void actualizar(EstadoHabitacion estadoHabitacion) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(estadoHabitacion);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al actualizar estado habitación: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            EstadoHabitacion estadoHabitacion = session.get(EstadoHabitacion.class, id);
            if (estadoHabitacion != null) {
                session.remove(estadoHabitacion);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al eliminar estado habitación: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, EstadoHab> obtenerEstadosEnRango(List<Integer> numerosHabitaciones,
                                                        LocalDate fechaInicio,
                                                        LocalDate fechaFin) {
        Map<String, EstadoHab> resultado = new HashMap<>();

        if (numerosHabitaciones == null || numerosHabitaciones.isEmpty()) {
            return resultado;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Una sola query que trae TODOS los estados necesarios
            List<EstadoHabitacion> estados = session.createQuery(
                            "SELECT eh FROM EstadoHabitacion eh " +
                                    "JOIN FETCH eh.habitacion h " +
                                    "JOIN FETCH eh.tipoEstado te " +
                                    "WHERE h.numero IN :numeros " +
                                    "AND (eh.fechaHasta IS NULL OR eh.fechaHasta >= :inicio) " +
                                    "AND eh.fechaDesde <= :fin " +
                                    "ORDER BY h.numero, eh.fechaDesde",
                            EstadoHabitacion.class)
                    .setParameterList("numeros", numerosHabitaciones)
                    .setParameter("inicio", fechaInicio)
                    .setParameter("fin", fechaFin)
                    .getResultList();

            // Mapear cada combinación habitación-fecha al estado correspondiente
            for (Integer numHab : numerosHabitaciones) {
                LocalDate fechaActual = fechaInicio;
                while (!fechaActual.isAfter(fechaFin)) {
                    // Buscar el estado correspondiente a esta fecha
                    LocalDate finalFechaActual = fechaActual;
                    LocalDate finalFechaActual1 = fechaActual;
                    EstadoHab estadoEnFecha = estados.stream()
                            .filter(eh -> eh.getHabitacion().getNumero().equals(numHab))
                            .filter(eh -> !finalFechaActual.isBefore(eh.getFechaDesde()))
                            .filter(eh -> eh.getFechaHasta() == null || !finalFechaActual1.isAfter(eh.getFechaHasta()))
                            .map(eh -> eh.getTipoEstado().getEstado())
                            .findFirst()
                            .orElse(EstadoHab.DISPONIBLE);

                    String clave = numHab + "_" + fechaActual;
                    resultado.put(clave, estadoEnFecha);

                    fechaActual = fechaActual.plusDays(1);
                }
            }

            return resultado;

        } catch (Exception e) {
            System.err.println("Error al obtener estados en rango: " + e.getMessage());
            e.printStackTrace();
            return resultado;
        }
    }
}