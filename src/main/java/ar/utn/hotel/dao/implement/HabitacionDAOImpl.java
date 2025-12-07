package ar.utn.hotel.dao.implement;

import ar.utn.hotel.dao.interfaces.HabitacionDAO;
import ar.utn.hotel.dao.interfaces.TipoEstadoDAO;
import ar.utn.hotel.model.*;
import utils.HibernateUtil;
import enums.EstadoHab;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class HabitacionDAOImpl implements HabitacionDAO {

    private final TipoEstadoDAO tipoEstadoDAO;

    public HabitacionDAOImpl(TipoEstadoDAO tipoEstadoDAO) {
        this.tipoEstadoDAO = tipoEstadoDAO;
    }

    @Override
    public Habitacion guardar(Habitacion habitacion) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(habitacion);
            transaction.commit();
            return habitacion;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al guardar habitación: " + e.getMessage(), e);
        }
    }

    @Override
    public Habitacion buscarPorNumero(Integer numero) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT h FROM Habitacion h " +
                                    "LEFT JOIN FETCH h.estados " +
                                    "WHERE h.numero = :numero",
                            Habitacion.class)
                    .setParameter("numero", numero)
                    .uniqueResult();
        }
    }

    @Override
    public List<Habitacion> listarTodas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT h FROM Habitacion h " +
                                    "LEFT JOIN FETCH h.estados " +
                                    "ORDER BY h.numero",
                            Habitacion.class)
                    .getResultList();
        }
    }

    @Override
    public List<Habitacion> listarPorRangoDeFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT h FROM Habitacion h " +
                    "LEFT JOIN FETCH h.estados eh " +
                    "LEFT JOIN FETCH eh.tipoEstado " +
                    "WHERE h.numero NOT IN " +
                    "(SELECT DISTINCT r.habitacion.numero FROM Reserva r " +
                    "WHERE r.fechaInicio <= :fechaFin " +
                    "AND r.fechaFin >= :fechaInicio) " +
                    "ORDER BY h.numero";

            return session.createQuery(hql, Habitacion.class)
                    .setParameter("fechaInicio", fechaInicio)
                    .setParameter("fechaFin", fechaFin)
                    .getResultList();
        }
    }

    @Override
    public void reservarHabitaciones(Set<Integer> numerosHabitaciones, LocalDate fechaDesde, LocalDate fechaHasta) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Obtener el tipo estado RESERVADA del catálogo
            TipoEstado tipoReservada = tipoEstadoDAO.buscarPorEstado(EstadoHab.RESERVADA);

            if (tipoReservada == null) {
                throw new IllegalStateException("No existe el tipo estado RESERVADA en el catálogo");
            }

            for (Integer numero : numerosHabitaciones) {
                Habitacion habitacion = session.get(Habitacion.class, numero);

                if (habitacion == null) {
                    throw new IllegalArgumentException(
                            "La habitación número " + numero + " no existe"
                    );
                }

                // Obtener el estado actual
                EstadoHabitacion estadoActual = habitacion.getEstadoActual();

                // Verificar si el estado actual es DISPONIBLE
                if (estadoActual != null &&
                        estadoActual.getTipoEstado().getEstado() != EstadoHab.DISPONIBLE) {
                    throw new IllegalStateException(
                            "La habitación número " + numero + " no está disponible. " +
                                    "Estado actual: " + estadoActual.getTipoEstado().getEstado()
                    );
                }

                // Cerrar el estado actual si existe
                if (estadoActual != null) {
                    estadoActual.setFechaHasta(LocalDate.now().minusDays(1));
                    session.merge(estadoActual);
                }

                // Crear nuevo estado con tipo RESERVADA
                EstadoHabitacion nuevoEstado = EstadoHabitacion.builder()
                        .habitacion(habitacion)
                        .tipoEstado(tipoReservada)
                        .fechaDesde(fechaDesde != null ? fechaDesde : LocalDate.now())
                        .fechaHasta(fechaHasta)
                        .build();

                habitacion.getEstados().add(nuevoEstado);
                session.persist(nuevoEstado);
                session.merge(habitacion);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al reservar habitaciones: " + e.getMessage(), e);
        }
    }

    @Override
    public void ocuparHabitaciones(Set<Integer> numerosHabitaciones, LocalDate fechaDesde, LocalDate fechaHasta) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Obtener el tipo estado OCUPADA del catálogo
            TipoEstado tipoOcupada = tipoEstadoDAO.buscarPorEstado(EstadoHab.OCUPADA);

            if (tipoOcupada == null) {
                throw new IllegalStateException("No existe el tipo estado OCUPADA en el catálogo");
            }

            for (Integer numero : numerosHabitaciones) {
                Habitacion habitacion = session.get(Habitacion.class, numero);

                if (habitacion == null) {
                    throw new IllegalArgumentException(
                            "La habitación número " + numero + " no existe"
                    );
                }

                // Obtener el estado actual
                EstadoHabitacion estadoActual = habitacion.getEstadoActual();

                // Verificar si el estado actual es RESERVADA
                if (estadoActual == null ||
                        estadoActual.getTipoEstado().getEstado() != EstadoHab.RESERVADA) {
                    throw new IllegalStateException(
                            "La habitación número " + numero + " no está reservada. " +
                                    "Estado actual: " + (estadoActual != null ?
                                    estadoActual.getTipoEstado().getEstado() : "SIN ESTADO")
                    );
                }

                // Cerrar el estado actual
                estadoActual.setFechaHasta(LocalDate.now().minusDays(1));
                session.merge(estadoActual);

                // Crear nuevo estado con tipo OCUPADA
                EstadoHabitacion nuevoEstado = EstadoHabitacion.builder()
                        .habitacion(habitacion)
                        .tipoEstado(tipoOcupada)
                        .fechaDesde(fechaDesde != null ? fechaDesde : LocalDate.now())
                        .fechaHasta(fechaHasta)
                        .build();

                habitacion.getEstados().add(nuevoEstado);
                session.persist(nuevoEstado);
                session.merge(habitacion);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al ocupar habitaciones: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Habitacion> buscarPorTipo(TipoHabitacion tipo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT h FROM Habitacion h " +
                                    "LEFT JOIN FETCH h.estados " +
                                    "WHERE h.tipo = :tipo " +
                                    "ORDER BY h.numero",
                            Habitacion.class)
                    .setParameter("tipo", tipo)
                    .getResultList();
        }
    }

    @Override
    public List<Habitacion> buscarPorEstado(EstadoHab estado) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT h FROM Habitacion h " +
                    "JOIN h.estados eh " +
                    "JOIN eh.tipoEstado te " +
                    "WHERE te.estado = :estado " +
                    "AND (eh.fechaHasta IS NULL OR eh.fechaHasta >= :now) " +
                    "AND eh.fechaDesde <= :now " +
                    "ORDER BY h.numero";

            return session.createQuery(hql, Habitacion.class)
                    .setParameter("estado", estado)
                    .setParameter("now", LocalDate.now())
                    .getResultList();
        }
    }

    @Override
    public List<Habitacion> buscarDisponibles() {
        return buscarPorEstado(EstadoHab.DISPONIBLE);
    }

    @Override
    public void actualizar(Habitacion habitacion) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(habitacion);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al actualizar habitación: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Integer numero) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Habitacion habitacion = session.get(Habitacion.class, numero);
            if (habitacion != null) {
                session.remove(habitacion);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al eliminar habitación: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existeNumero(Integer numero) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                            "SELECT COUNT(h) FROM Habitacion h WHERE h.numero = :numero", Long.class)
                    .setParameter("numero", numero)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    @Override
    public Long contarPorTipo(TipoHabitacion tipo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT COUNT(h) FROM Habitacion h WHERE h.tipo = :tipo", Long.class)
                    .setParameter("tipo", tipo)
                    .uniqueResult();
        }
    }
}