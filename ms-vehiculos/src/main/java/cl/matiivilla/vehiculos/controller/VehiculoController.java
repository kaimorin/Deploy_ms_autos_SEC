package cl.matiivilla.vehiculos.controller;

import cl.matiivilla.vehiculos.dto.VehiculoRequest;
import cl.matiivilla.vehiculos.dto.VehiculoResponse;
import cl.matiivilla.vehiculos.service.VehiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehiculos")
@RequiredArgsConstructor
@Tag(name = "Vehículos", description = "CRUD de vehículos")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    @GetMapping
    @Operation(summary = "Listar todos los vehículos")
    public List<VehiculoResponse> findAll() {
        return vehiculoService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar un vehículo por id")
    public VehiculoResponse findById(@PathVariable Long id) {
        return vehiculoService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear un vehículo nuevo")
    public VehiculoResponse create(@Valid @RequestBody VehiculoRequest request) {
        return vehiculoService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un vehículo existente")
    public VehiculoResponse update(@PathVariable Long id, @Valid @RequestBody VehiculoRequest request) {
        return vehiculoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un vehículo")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehiculoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
