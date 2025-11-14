package ar.utn.hotel.gestor;
import ar.utn.hotel.dao.DireccionDAO;
import ar.utn.hotel.dao.HuespedDAO;
import ar.utn.hotel.dto.DarAltaHuespedDTO;
import ar.utn.hotel.model.*;

public class GestorHuesped {

    private final DireccionDAO direccionDAO = new DireccionDAO();
    private final HuespedDAO huespedDAO = new HuespedDAO();

    public Huesped cargar(DarAltaHuespedDTO dto) {

        // 1) Buscar o crear Dirección
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

        // 2) Crear Huesped directamente (hereda de Persona)
        Huesped huesped = new Huesped();

        // Datos de Persona (clase padre)
        huesped.setNombre(dto.getNombre());
        huesped.setApellido(dto.getApellido());
        huesped.setTelefono(dto.getTelefono());
        huesped.setDireccion(dir);

        // Datos específicos de Huesped
        huesped.setNumeroDocumento(dto.getNumeroDocumento());
        huesped.setTipoDocumento(dto.getTipoDocumento());
        huesped.setPosicionIVA(dto.getPosicionIVA());
        huesped.setFechaNacimiento(dto.getFechaNacimiento());
        huesped.setOcupacion(dto.getOcupacion());
        huesped.setNacionalidad(dto.getNacionalidad());
        huesped.setEmail(dto.getEmail());
        huesped.setCuit(dto.getCuit());

        // 3) Guardar Huesped (Hibernate maneja la herencia automáticamente)
        return huespedDAO.guardar(huesped);
    }
}