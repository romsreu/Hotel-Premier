package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.EstadoHabitacionDAO;
import ar.utn.hotel.dao.HabitacionDAO;
import ar.utn.hotel.dao.RegistroEstadoHabitacionDAO;
import ar.utn.hotel.dao.TipoHabitacionDAO;
import ar.utn.hotel.model.EstadoHabitacion;
import ar.utn.hotel.model.Habitacion;
import ar.utn.hotel.model.RegistroEstadoHabitacion;
import ar.utn.hotel.model.TipoHabitacion;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class HabitacionDAOImpl implements HabitacionDAO {

    private final EstadoHabitacionDAO estadoHabitacionDAO;
    private final RegistroEstadoHabitacionDAO registroEstadoHabitacionDAO;
    private final TipoHabitacionDAO tipoHabitacionDAO;

    public HabitacionDAOImpl() {
        this.estadoHabitacionDAO = new EstadoHabitacionDAOImpl();
        this.registroEstadoHabitacionDAO = new RegistroEstadoHabitacionDAOImpl();
        this.tipoHabitacionDAO = new TipoHabitacionDAOImpl();
    }

    public HabitacionDAOImpl(EstadoHabitacionDAO estadoHabitacionDAO,
                             RegistroEstadoHabitacionDAO registroEstadoHabitacionDAO,
                             TipoHabitacionDAO tipoHabitacionDAO) {
        this.estadoHabitacionDAO = estadoHabitacionDAO;
        this.registroEstadoHabitacionDAO = registroEstadoHabitacionDAO;
        this.tipoHabitacionDAO = tipoHabitacionDAO;
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
            return session.get(Habitacion.class, numero);
        }
    }

    @Override
    public List<Habitacion> listarTodas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Habitacion h ORDER BY h.numero", Habitacion.class)
                    .getResultList();
        }
    }

    @Override
    public List<Habitacion> listarPorRangoDeFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        Transaction transaction = null;
        List<Habitacion> habitacionesDisponibles = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            EstadoHabitacion estadoMantenimiento = estadoHabitacionDAO.obtenerPorNombre("MANTENIMIENTO");

            if (estadoMantenimiento == null) {
                throw new IllegalStateException("El estado 'MANTENIMIENTO' no está definido en el catálogo.");
            }

            String hql = "SELECT h FROM Habitacion h WHERE h.numero NOT IN " +
                    "(SELECT DISTINCT hab.numero FROM Reserva r " +
                    "JOIN r.habitaciones hab " +
                    "WHERE r.fechaInicio <= :fechaFin " +
                    "AND r.fechaFin >= :fechaInicio) " +
                    "AND h.numero NOT IN (" +
                    "    SELECT DISTINCT reh.habitacion.numero FROM RegistroEstadoHabitacion reh " +
                    "    WHERE reh.estadoHabitacion = :estadoMantenimiento " +
                    "    AND reh.fechaInicio <= :fechaFin " +
                    "    AND (reh.fechaFin IS NULL OR reh.fechaFin >= :fechaInicio) " +
                    ") " +
                    "ORDER BY h.numero";

            var query = session.createQuery(hql, Habitacion.class);
            query.setParameter("fechaInicio", fechaInicio);
            query.setParameter("fechaFin", fechaFin);
            query.setParameter("estadoMantenimiento", estadoMantenimiento);

            habitacionesDisponibles = query.list();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error al listar habitaciones disponibles: " + e.getMessage(), e);
        }

        return habitacionesDisponibles;
    }

    @Override
    public void reservarHabitaciones(Set<Integer> numerosHabitaciones) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            EstadoHabitacion estadoReservada = estadoHabitacionDAO.obtenerPorNombre("RESERVADA");
            EstadoHabitacion estadoDisponible = estadoHabitacionDAO.obtenerPorNombre("DISPONIBLE");

            if (estadoReservada == null || estadoDisponible == null) {
                throw new IllegalStateException("Faltan estados de habitación en el catálogo (DISPONIBLE o RESERVADA).");
            }

            for (Integer numero : numerosHabitaciones) {
                Habitacion habitacion = session.get(Habitacion.class, numero);

                if (habitacion == null) {
                    throw new IllegalArgumentException(
                            "La habitación número " + numero + " no existe"
                    );
                }

                RegistroEstadoHabitacion registroActual = registroEstadoHabitacionDAO.obtenerRegistroActual(numero);

                if (registroActual == null || !registroActual.getEstadoHabitacion().equals(estadoDisponible)) {
                    String nombreEstado = (registroActual != null)
                            ? registroActual.getEstadoHabitacion().getNombre()
                            : "SIN REGISTRO INICIAL";

                    throw new IllegalStateException(
                            "La habitación número " + numero + " no está disponible. " +
                                    "Estado actual: " + nombreEstado
                    );
                }

                registroActual.finalizarRegistro();
                registroEstadoHabitacionDAO.merge(registroActual);

                RegistroEstadoHabitacion nuevoRegistro = RegistroEstadoHabitacion.builder()
                        .habitacion(habitacion)
                        .estadoHabitacion(estadoReservada)
                        .fechaInicio(LocalDate.now())
                        .fechaFin(null)
                        .build();

                registroEstadoHabitacionDAO.persist(nuevoRegistro);
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
    public List<Habitacion> buscarPorTipo(TipoHabitacion tipo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // HQL: Filtra Habitacion (h) donde su campo 'tipo' es igual al objeto TipoHabitacion pasado.
            // Hibernate maneja automáticamente la comparación de la entidad TipoHabitacion.
            String hql = "FROM Habitacion h WHERE h.tipoHabitacion = :tipoObj";

            return session.createQuery(hql, Habitacion.class)
                    .setParameter("tipoObj", tipo)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar habitaciones por tipo: " + tipo.toString(), e);
        }
    }

    @Override
    public List<Habitacion> buscarDisponibles() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            EstadoHabitacion estadoDisponible = estadoHabitacionDAO.obtenerPorNombre("DISPONIBLE");

            if (estadoDisponible == null) {
                throw new IllegalStateException("El estado 'DISPONIBLE' no está definido en el catálogo.");
            }

            String hql = "SELECT DISTINCT h FROM Habitacion h " +
                    "JOIN RegistroEstadoHabitacion reh ON reh.habitacion.numero = h.numero " +
                    "WHERE reh.estadoHabitacion = :estadoDisponible " +
                    "AND reh.fechaFin IS NULL " +
                    "ORDER BY h.numero";

            return session.createQuery(hql, Habitacion.class)
                    .setParameter("estadoDisponible", estadoDisponible)
                    .getResultList();
        }
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
    public boolean existeNumero(String numero) {
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
            // HQL: Cuenta Habitacion (h) donde su campo 'tipo' es igual al objeto TipoHabitacion pasado.
            String hql = "SELECT COUNT(h) FROM Habitacion h WHERE h.tipoHabitacion = :tipoObj";

            return session.createQuery(hql, Long.class)
                    .setParameter("tipoObj", tipo)
                    .uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error al contar habitaciones por tipo: " + tipo.toString(), e);
        }
    }
}