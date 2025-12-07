package ar.utn.hotel.dao.implement;

import ar.utn.hotel.dao.interfaces.ReservaDAO;
import ar.utn.hotel.dao.interfaces.TipoEstadoDAO;
import ar.utn.hotel.dto.CrearReservaDTO;
import ar.utn.hotel.model.*;
import enums.EstadoHab;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.time.LocalDate;
import java.util.List;

public class ReservaDAOImpl implements ReservaDAO {

    private final TipoEstadoDAO tipoEstadoDAO;

    public ReservaDAOImpl(TipoEstadoDAO tipoEstadoDAO) {
        this.tipoEstadoDAO = tipoEstadoDAO;
    }

    @Override
    public Reserva crearReserva(CrearReservaDTO dto) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Verificar que el huésped existe
            Huesped huesped = session.get(Huesped.class, dto.getIdHuesped());
            if (huesped == null) {
                throw new RuntimeException("Error: El huésped no se encuentra registrado en el sistema.");
            }

            // Obtener la habitación
            Habitacion habitacion = session.createQuery(
                            "SELECT DISTINCT h FROM Habitacion h " +
                                    "LEFT JOIN FETCH h.estados " +
                                    "WHERE h.numero = :numero",
                            Habitacion.class)
                    .setParameter("numero", dto.getNumeroHabitacion())
                    .uniqueResult();

            if (habitacion == null) {
                throw new IllegalArgumentException("La habitación número " + dto.getNumeroHabitacion() + " no existe");
            }

            // Obtener el tipo estado RESERVADA
            TipoEstado tipoReservada = tipoEstadoDAO.buscarPorEstado(EstadoHab.RESERVADA);
            if (tipoReservada == null) {
                throw new IllegalStateException("No existe el tipo estado RESERVADA en el catálogo");
            }

            // Verificar disponibilidad
            EstadoHabitacion estadoActual = habitacion.getEstadoActual();
            if (estadoActual != null && estadoActual.getTipoEstado().getEstado() != EstadoHab.DISPONIBLE) {
                throw new IllegalStateException(
                        "La habitación número " + dto.getNumeroHabitacion() + " no está disponible. " +
                                "Estado actual: " + estadoActual.getTipoEstado().getEstado()
                );
            }

            // Cerrar el estado actual
            if (estadoActual != null) {
                estadoActual.setFechaHasta(dto.getFechaInicio().minusDays(1));
                session.merge(estadoActual);
            }

            // Crear nuevo estado RESERVADA
            EstadoHabitacion nuevoEstado = EstadoHabitacion.builder()
                    .habitacion(habitacion)
                    .tipoEstado(tipoReservada)
                    .fechaDesde(dto.getFechaInicio())
                    .fechaHasta(dto.getFechaFin())
                    .build();

            habitacion.getEstados().add(nuevoEstado);
            session.persist(nuevoEstado);

            // Crear la reserva
            Reserva reserva = Reserva.builder()
                    .huesped(huesped)
                    .habitacion(habitacion)
                    .fechaInicio(dto.getFechaInicio())
                    .fechaFin(dto.getFechaFin())
                    .cantHuespedes(dto.getCantHuespedes())
                    .descuento(dto.getDescuento())
                    .build();

            session.persist(reserva);
            transaction.commit();

            return reserva;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al crear reserva: " + e.getMessage(), e);
        }
    }

    @Override
    public Reserva obtenerPorId(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT r FROM Reserva r " +
                                    "LEFT JOIN FETCH r.habitacion h " +
                                    "LEFT JOIN FETCH h.estados " +
                                    "LEFT JOIN FETCH r.huesped " +
                                    "WHERE r.id = :id",
                            Reserva.class)
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }

    @Override
    public List<Reserva> obtenerTodas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT r FROM Reserva r " +
                                    "LEFT JOIN FETCH r.habitacion " +
                                    "LEFT JOIN FETCH r.huesped " +
                                    "ORDER BY r.fechaInicio DESC",
                            Reserva.class)
                    .getResultList();
        }
    }

    @Override
    public List<Reserva> obtenerPorHuesped(Long idHuesped) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT r FROM Reserva r " +
                                    "LEFT JOIN FETCH r.habitacion " +
                                    "WHERE r.huesped.id = :idHuesped " +
                                    "ORDER BY r.fechaInicio DESC",
                            Reserva.class)
                    .setParameter("idHuesped", idHuesped)
                    .getResultList();
        }
    }

    @Override
    public List<Reserva> obtenerPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT r FROM Reserva r " +
                                    "LEFT JOIN FETCH r.habitacion " +
                                    "LEFT JOIN FETCH r.huesped " +
                                    "WHERE r.fechaInicio <= :fechaFin " +
                                    "AND r.fechaFin >= :fechaInicio " +
                                    "ORDER BY r.fechaInicio",
                            Reserva.class)
                    .setParameter("fechaInicio", fechaInicio)
                    .setParameter("fechaFin", fechaFin)
                    .getResultList();
        }
    }

    @Override
    public List<Reserva> obtenerPorHabitacion(Integer numeroHabitacion) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT r FROM Reserva r " +
                                    "LEFT JOIN FETCH r.huesped " +
                                    "WHERE r.habitacion.numero = :numeroHabitacion " +
                                    "ORDER BY r.fechaInicio DESC",
                            Reserva.class)
                    .setParameter("numeroHabitacion", numeroHabitacion)
                    .getResultList();
        }
    }

    @Override
    public void actualizar(Reserva reserva) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(reserva);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al actualizar reserva: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Reserva reserva = session.get(Reserva.class, id);
            if (reserva != null) {
                // Liberar la habitación (cambiar estado a DISPONIBLE)
                TipoEstado tipoDisponible = tipoEstadoDAO.buscarPorEstado(EstadoHab.DISPONIBLE);

                if (tipoDisponible != null) {
                    Habitacion habitacion = reserva.getHabitacion();

                    EstadoHabitacion estadoActual = habitacion.getEstadoActual();
                    if (estadoActual != null) {
                        estadoActual.setFechaHasta(LocalDate.now());
                        session.merge(estadoActual);
                    }

                    EstadoHabitacion nuevoEstado = EstadoHabitacion.builder()
                            .habitacion(habitacion)
                            .tipoEstado(tipoDisponible)
                            .fechaDesde(LocalDate.now())
                            .build();

                    habitacion.getEstados().add(nuevoEstado);
                    session.persist(nuevoEstado);
                }

                session.remove(reserva);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al eliminar reserva: " + e.getMessage(), e);
        }
    }
}