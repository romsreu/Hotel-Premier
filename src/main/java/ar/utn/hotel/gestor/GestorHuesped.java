package ar.utn.hotel.gestor;

import ar.utn.hotel.dao.interfaces.DireccionDAO;
import ar.utn.hotel.dao.interfaces.HuespedDAO;
import ar.utn.hotel.dao.implement.DireccionDAOImpl;
import ar.utn.hotel.dao.implement.HuespedDAOImpl;
import ar.utn.hotel.dto.DarAltaHuespedDTO;
import ar.utn.hotel.dto.HuespedDTO;
import ar.utn.hotel.model.*;

import java.util.List;

public class GestorHuesped {

    private final DireccionDAO direccionDAO;
    private final HuespedDAO huespedDAO;

    // Constructor por defecto
    public GestorHuesped() {
        this.direccionDAO = new DireccionDAOImpl();
        this.huespedDAO = new HuespedDAOImpl();
    }

    // Constructor para inyección de dependencias
    public GestorHuesped(DireccionDAO direccionDAO, HuespedDAO huespedDAO) {
        this.direccionDAO = direccionDAO;
        this.huespedDAO = huespedDAO;
    }

    /**
     * Da de alta un nuevo huésped en el sistema
     */
    public Huesped cargar(DarAltaHuespedDTO dto) {
        // Verificar si ya existe
        if (huespedDAO.existePorDocumento(dto.getNumeroDocumento(), dto.getTipoDocumento())) {
            throw new IllegalArgumentException(
                    "Ya existe un huésped registrado con el documento " +
                            dto.getTipoDocumento() + " " + dto.getNumeroDocumento()
            );
        }

        // Buscar o crear Dirección
        Direccion dir = direccionDAO.buscarPorDatos(
                dto.getCalle(),
                dto.getNumero(),
                dto.getDepto(),
                dto.getPiso(),
                dto.getCodPostal(),
                dto.getLocalidad(),
                dto.getProvincia(),
                dto.getPais()
        );

        if (dir == null) {
            dir = Direccion.builder()
                    .calle(dto.getCalle())
                    .numero(dto.getNumero())
                    .departamento(dto.getDepto())
                    .piso(dto.getPiso())
                    .codPostal(dto.getCodPostal())
                    .localidad(dto.getLocalidad())
                    .provincia(dto.getProvincia())
                    .pais(dto.getPais())
                    .build();

            direccionDAO.guardar(dir);
        }

        // Crear Huesped
        Huesped huesped = Huesped.builder()
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .telefono(dto.getTelefono())
                .direccion(dir)
                .numeroDocumento(dto.getNumeroDocumento())
                .tipoDocumento(dto.getTipoDocumento())
                .posicionIVA(dto.getPosicionIVA())
                .fechaNacimiento(dto.getFechaNacimiento())
                .ocupacion(dto.getOcupacion())
                .nacionalidad(dto.getNacionalidad())
                .email(dto.getEmail())
                .cuit(dto.getCuit())
                .build();

        return huespedDAO.guardar(huesped);
    }

    /**
     * Busca huéspedes según criterios flexibles
     * Solo busca por los campos que no sean null en el DTO
     */
    public List<Huesped> buscarHuesped(HuespedDTO dto) {
        return huespedDAO.buscarHuesped(dto);
    }

    /**
     * Busca huéspedes por nombre y apellido
     */
    public List<Huesped> buscarPorNombreApellido(String nombre, String apellido) {
        HuespedDTO dto = HuespedDTO.builder()
                .nombre(nombre)
                .apellido(apellido)
                .build();
        return buscarHuesped(dto);
    }

    /**
     * Busca huéspedes por nombre, apellido y número de documento
     */
    public List<Huesped> buscarPorNombreApellidoDocumento(String nombre, String apellido, String numeroDocumento) {
        HuespedDTO dto = HuespedDTO.builder()
                .nombre(nombre)
                .apellido(apellido)
                .numeroDocumento(numeroDocumento)
                .build();
        return buscarHuesped(dto);
    }

    /**
     * Busca huéspedes por documento
     */
    public List<Huesped> buscarPorDocumento(String numeroDocumento, String tipoDocumento) {
        HuespedDTO dto = HuespedDTO.builder()
                .numeroDocumento(numeroDocumento)
                .tipoDocumento(tipoDocumento)
                .build();
        return buscarHuesped(dto);
    }

    /**
     * Busca huéspedes por email
     */
    public List<Huesped> buscarPorEmail(String email) {
        HuespedDTO dto = HuespedDTO.builder()
                .email(email)
                .build();
        return buscarHuesped(dto);
    }

    /**
     * Busca huéspedes por teléfono
     */
    public List<Huesped> buscarPorTelefono(String telefono) {
        HuespedDTO dto = HuespedDTO.builder()
                .telefono(telefono)
                .build();
        return buscarHuesped(dto);
    }

    /**
     * Obtiene un huésped por ID
     */
    public Huesped obtenerPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        Huesped huesped = huespedDAO.obtenerPorId(id);
        if (huesped == null) {
            throw new IllegalArgumentException("No existe huésped con ID " + id);
        }

        return huesped;
    }

    /**
     * Obtiene todos los huéspedes
     */
    public List<Huesped> obtenerTodos() {
        return huespedDAO.obtenerTodos();
    }

    /**
     * Actualiza un huésped existente
     */
    public void actualizar(Huesped huesped) {
        if (huesped == null || huesped.getId() == null) {
            throw new IllegalArgumentException("El huésped o su ID no pueden ser nulos");
        }

        huespedDAO.actualizar(huesped);
    }

    /**
     * Elimina un huésped
     */
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        huespedDAO.eliminar(id);
    }
}