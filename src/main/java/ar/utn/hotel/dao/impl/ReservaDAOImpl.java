package ar.utn.hotel.dao.impl;

import ar.utn.hotel.dao.HabitacionDAO;
import ar.utn.hotel.dao.PersonaDAO;
import ar.utn.hotel.dao.ReservaDAO;
import ar.utn.hotel.model.*;
import utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class ReservaDAOImpl implements ReservaDAO {

    private final PersonaDAO personaDAO;
    private final HabitacionDAO habitacionDAO;

    public ReservaDAOImpl(PersonaDAO personaDAO, HabitacionDAO habitacionDAO) {
        this.personaDAO = personaDAO;
        this.habitacionDAO = habitacionDAO;
    }

    @Override
    public Reserva crearReserva(Long idPersona, LocalDate fechaInicio, LocalDate fechaFin,
                                Set<Integer> numerosHabitaciones) {

        // 1. ABRIMOS SESIÓN MANUALMENTE (Sin try-with-resources)
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        Reserva reserva = null;

        try {
            transaction = session.beginTransaction();
            System.out.println(">> (DAO) Transacción iniciada. Buscando datos...");

            // 2. RECUPERAR PERSONA (Dentro de esta sesión)
            Persona persona = session.get(Persona.class, idPersona);
            if (persona == null) throw new RuntimeException("Error: La persona ID " + idPersona + " no existe.");

            // 3. RECUPERAR ESTADOS
            EstadoHabitacion estadoReservada = session.createQuery("FROM EstadoHabitacion WHERE nombre = 'RESERVADA'", EstadoHabitacion.class).uniqueResult();
            EstadoHabitacion estadoDisponible = session.createQuery("FROM EstadoHabitacion WHERE nombre = 'DISPONIBLE'", EstadoHabitacion.class).uniqueResult();

            if (estadoReservada == null) throw new RuntimeException("Falta estado 'RESERVADA' en BDD.");

            // 4. CREAR OBJETO RESERVA
            reserva = Reserva.builder()
                    .persona(persona)
                    .fechaInicio(fechaInicio)
                    .fechaFin(fechaFin)
                    // .montoTotal(0.0) // Descomentar si tienes este campo
                    .build();

            System.out.println(">> (DAO) Procesando " + numerosHabitaciones.size() + " habitaciones...");

            // 5. PROCESAR HABITACIONES
            // 5. PROCESAR HABITACIONES
            for (Integer numero : numerosHabitaciones) {
                Habitacion habitacion = session.get(Habitacion.class, numero);
                if (habitacion == null) throw new IllegalArgumentException("Habitación " + numero + " no existe.");

                // A. Agregar a la reserva (Relación Java)
                reserva.agregarHabitacion(habitacion);

                // B. Crear el registro de estado SOLO para los días seleccionados
                // NOTA: Eliminamos la parte que hacía "setFechaFin(LocalDate.now())" al registro anterior
                // para no afectar la disponibilidad de hoy si la reserva es a futuro.

                RegistroEstadoHabitacion nuevoReg = RegistroEstadoHabitacion.builder()
                        .habitacion(habitacion)
                        .estadoHabitacion(estadoReservada)
                        .fechaInicio(fechaInicio) // <--- CORRECCIÓN: Usamos la fecha que eligió el usuario
                        .fechaFin(fechaFin)       // <--- CORRECCIÓN: Usamos la fecha de fin real
                        .build();

                session.persist(nuevoReg);
            }

            System.out.println(">> (DAO) Guardando reserva en BDD...");

            // 6. GUARDAR RESERVA
            session.persist(reserva);

            transaction.commit();
            System.out.println(">> (DAO) ¡Commit exitoso!");

        } catch (Exception e) {
            // AQUÍ VEMOS EL ERROR REAL
            System.err.println("!!! ERROR REAL EN CREAR RESERVA !!!");
            e.printStackTrace();

            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                    System.out.println(">> Rollback realizado.");
                } catch (Exception rollEx) {
                    System.err.println(">> Falló el rollback: " + rollEx.getMessage());
                }
            }
            throw new RuntimeException("Error al crear reserva: " + e.getMessage());
        } finally {
            // CERRAR SESIÓN SIEMPRE
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return reserva;
    }

    // ... MANTÉN TUS OTROS MÉTODOS IGUAL (obtenerPorId, etc.) ...
    // Solo copia los métodos de lectura de tu versión anterior,
    // pero ASEGURATE DE REEMPLAZAR 'crearReserva' CON ESTE DE ARRIBA.

    @Override
    public Reserva crearReservaPorNombreApellido(String nombre, String apellido, LocalDate fi, LocalDate ff, Set<Integer> habs) {
        Persona p = personaDAO.buscarPorNombreApellido(nombre, apellido);
        return crearReserva(p.getId(), fi, ff, habs);
    }

    @Override
    public Reserva crearReservaPorTelefono(String tel, LocalDate fi, LocalDate ff, Set<Integer> habs) {
        Persona p = personaDAO.buscarPorTelefono(tel);
        return crearReserva(p.getId(), fi, ff, habs);
    }

    @Override
    public Reserva obtenerPorId(Long id) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) { return s.get(Reserva.class, id); }
    }

    @Override
    public List<Reserva> obtenerTodas() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) { return s.createQuery("FROM Reserva", Reserva.class).list(); }
    }

    @Override
    public List<Reserva> obtenerPorPersona(Long id) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("FROM Reserva r WHERE r.persona.id = :id", Reserva.class).setParameter("id", id).list();
        }
    }

    @Override
    public List<Reserva> obtenerPorFechas(LocalDate i, LocalDate f) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("FROM Reserva r WHERE r.fechaInicio <= :f AND r.fechaFin >= :i", Reserva.class)
                    .setParameter("i", i).setParameter("f", f).list();
        }
    }

    @Override
    public List<Reserva> obtenerPorHabitacion(Integer num) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("SELECT r FROM Reserva r JOIN r.habitaciones h WHERE h.numero = :num", Reserva.class).setParameter("num", num).list();
        }
    }

    @Override
    public void actualizar(Reserva r) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction t = s.beginTransaction(); s.merge(r); t.commit();
        }
    }

    @Override
    public void eliminar(Long id) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction t = s.beginTransaction();
            Reserva r = s.get(Reserva.class, id); if (r!=null) s.remove(r);
            t.commit();
        }
    }
}