package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.EstadoHabitacionDAO;
import ar.utn.hotel.dao.HabitacionDAO;
import ar.utn.hotel.model.Habitacion;
import ar.utn.hotel.model.EstadoHabitacion;
import ar.utn.hotel.model.RegistroEstadoHabitacion;
import ar.utn.hotel.model.TipoHabitacion;
import ar.utn.hotel.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class HabitacionDAOImpl implements HabitacionDAO {

    private final EstadoHabitacionDAO estadoHabitacionDAO;

    public HabitacionDAOImpl(EstadoHabitacionDAO estadoHabitacionDAO) {
        this.estadoHabitacionDAO = estadoHabitacionDAO;
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
                                    "LEFT JOIN FETCH h.historialEstados " +
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
                                    "LEFT JOIN FETCH h.historialEstados " +
                                    "ORDER BY h.numero",
                            Habitacion.class)
                    .getResultList();
        }
    }

    @Override
    public List<Habitacion> listarPorRangoDeFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT h FROM Habitacion h " +
                    "LEFT JOIN FETCH h.historialEstados reg " +
                    "LEFT JOIN FETCH reg.estado " +
                    "WHERE h.numero NOT IN " +
                    "(SELECT DISTINCT hab.numero FROM Reserva r " +
                    "JOIN r.habitaciones hab " +
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

            // Obtener el estado RESERVADA del catálogo
            EstadoHabitacion estadoReservada = estadoHabitacionDAO.buscarPorEstado(
                    enums.EstadoHabitacion.RESERVADA
            );

            if (estadoReservada == null) {
                throw new IllegalStateException("No existe el estado RESERVADA en el catálogo");
            }

            for (Integer numero : numerosHabitaciones) {
                Habitacion habitacion = session.get(Habitacion.class, numero);

                if (habitacion == null) {
                    throw new IllegalArgumentException(
                            "La habitación número " + numero + " no existe"
                    );
                }

                // Obtener el registro actual
                RegistroEstadoHabitacion registroActual = habitacion.getEstadoActual();

                // Verificar si el estado actual es DISPONIBLE
                if (registroActual != null &&
                        registroActual.getEstado().getEstado() != enums.EstadoHabitacion.DISPONIBLE) {
                    throw new IllegalStateException(
                            "La habitación número " + numero + " no está disponible. " +
                                    "Estado actual: " + registroActual.getEstado().getEstado()
                    );
                }

                // Cerrar el registro actual si existe
                if (registroActual != null) {
                    registroActual.setFechaHasta(LocalDate.now().minusDays(1));
                    session.merge(registroActual);
                }

                // Crear nuevo registro con estado RESERVADA
                RegistroEstadoHabitacion nuevoRegistro = RegistroEstadoHabitacion.builder()
                        .habitacion(habitacion)
                        .estado(estadoReservada)
                        .fechaDesde(fechaDesde != null ? fechaDesde : LocalDate.now())
                        .fechaHasta(fechaHasta)
                        .build();

                habitacion.getHistorialEstados().add(nuevoRegistro);
                session.persist(nuevoRegistro);
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

            // Obtener el estado OCUPADA del catálogo
            EstadoHabitacion estadoOcupada = estadoHabitacionDAO.buscarPorEstado(
                    enums.EstadoHabitacion.OCUPADA
            );

            if (estadoOcupada == null) {
                throw new IllegalStateException("No existe el estado OCUPADA en el catálogo");
            }

            for (Integer numero : numerosHabitaciones) {
                Habitacion habitacion = session.get(Habitacion.class, numero);

                if (habitacion == null) {
                    throw new IllegalArgumentException(
                            "La habitación número " + numero + " no existe"
                    );
                }

                // Obtener el registro actual
                RegistroEstadoHabitacion registroActual = habitacion.getEstadoActual();

                // Verificar si el estado actual es RESERVADA
                if (registroActual == null ||
                        registroActual.getEstado().getEstado() != enums.EstadoHabitacion.RESERVADA) {
                    throw new IllegalStateException(
                            "La habitación número " + numero + " no está reservada. " +
                                    "Estado actual: " + (registroActual != null ?
                                    registroActual.getEstado().getEstado() : "SIN ESTADO")
                    );
                }

                // Cerrar el registro actual
                registroActual.setFechaHasta(LocalDate.now().minusDays(1));
                session.merge(registroActual);

                // Crear nuevo registro con estado OCUPADA
                RegistroEstadoHabitacion nuevoRegistro = RegistroEstadoHabitacion.builder()
                        .habitacion(habitacion)
                        .estado(estadoOcupada)
                        .fechaDesde(fechaDesde != null ? fechaDesde : LocalDate.now())
                        .fechaHasta(fechaHasta)
                        .build();

                habitacion.getHistorialEstados().add(nuevoRegistro);
                session.persist(nuevoRegistro);
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
                                    "LEFT JOIN FETCH h.historialEstados " +
                                    "WHERE h.tipo = :tipo " +
                                    "ORDER BY h.numero",
                            Habitacion.class)
                    .setParameter("tipo", tipo)
                    .getResultList();
        }
    }

    @Override
    public List<Habitacion> buscarPorEstado(enums.EstadoHabitacion estado) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT h FROM Habitacion h " +
                    "JOIN h.historialEstados reg " +
                    "JOIN reg.estado e " +
                    "WHERE e.estado = :estado " +
                    "AND (reg.fechaHasta IS NULL OR reg.fechaHasta >= :now) " +
                    "AND reg.fechaDesde <= :now " +
                    "ORDER BY h.numero";

            return session.createQuery(hql, Habitacion.class)
                    .setParameter("estado", estado)
                    .setParameter("now", LocalDate.now())
                    .getResultList();
        }
    }

    @Override
    public List<Habitacion> buscarDisponibles() {
        return buscarPorEstado(enums.EstadoHabitacion.DISPONIBLE);
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