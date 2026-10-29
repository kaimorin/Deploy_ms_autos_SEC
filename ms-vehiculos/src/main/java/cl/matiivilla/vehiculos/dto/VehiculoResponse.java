package cl.matiivilla.vehiculos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoResponse {

    private Long id;
    private String patente;
    private String marca;
    private String modelo;
    private Integer anio;
    private String color;
    private String tipo;
    private Boolean disponible;
}
