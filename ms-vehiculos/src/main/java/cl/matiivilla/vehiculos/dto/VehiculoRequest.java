package cl.matiivilla.vehiculos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehiculoRequest {

    @NotBlank(message = "La patente es obligatoria")
    private String patente;

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;

    @NotNull(message = "El año es obligatorio")
    @Min(value = 1950, message = "El año debe ser mayor o igual a 1950")
    private Integer anio;

    private String color;

    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    @NotNull(message = "La disponibilidad es obligatoria")
    private Boolean disponible;
}
