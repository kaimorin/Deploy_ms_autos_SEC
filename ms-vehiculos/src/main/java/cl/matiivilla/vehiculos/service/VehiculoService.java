package cl.matiivilla.vehiculos.service;

import cl.matiivilla.vehiculos.dto.VehiculoRequest;
import cl.matiivilla.vehiculos.dto.VehiculoResponse;
import cl.matiivilla.vehiculos.model.Vehiculo;
import cl.matiivilla.vehiculos.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;

    public List<VehiculoResponse> findAll() {
        return vehiculoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public VehiculoResponse findById(Long id) {
        return toResponse(buscarOFallar(id));
    }

    public VehiculoResponse create(VehiculoRequest request) {
        if (vehiculoRepository.existsByPatente(request.getPatente())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un vehículo con la patente " + request.getPatente());
        }

        Vehiculo vehiculo = Vehiculo.builder()
                .patente(request.getPatente())
                .marca(request.getMarca())
                .modelo(request.getModelo())
                .anio(request.getAnio())
                .color(request.getColor())
                .tipo(request.getTipo())
                .disponible(request.getDisponible())
                .build();

        return toResponse(vehiculoRepository.save(vehiculo));
    }

    public VehiculoResponse update(Long id, VehiculoRequest request) {
        Vehiculo vehiculo = buscarOFallar(id);

        vehiculoRepository.findByPatente(request.getPatente())
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Ya existe un vehículo con la patente " + request.getPatente());
                });

        vehiculo.setPatente(request.getPatente());
        vehiculo.setMarca(request.getMarca());
        vehiculo.setModelo(request.getModelo());
        vehiculo.setAnio(request.getAnio());
        vehiculo.setColor(request.getColor());
        vehiculo.setTipo(request.getTipo());
        vehiculo.setDisponible(request.getDisponible());

        return toResponse(vehiculoRepository.save(vehiculo));
    }

    public void delete(Long id) {
        Vehiculo vehiculo = buscarOFallar(id);
        vehiculoRepository.delete(vehiculo);
    }

    private Vehiculo buscarOFallar(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el vehículo con id " + id));
    }

    private VehiculoResponse toResponse(Vehiculo vehiculo) {
        return VehiculoResponse.builder()
                .id(vehiculo.getId())
                .patente(vehiculo.getPatente())
                .marca(vehiculo.getMarca())
                .modelo(vehiculo.getModelo())
                .anio(vehiculo.getAnio())
                .color(vehiculo.getColor())
                .tipo(vehiculo.getTipo())
                .disponible(vehiculo.getDisponible())
                .build();
    }
}
