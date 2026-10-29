package cl.matiivilla.vehiculos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
//comentario para push
@RestController
@Tag(name = "Home", description = "Información básica de la API")
public class HomeController {
    
    @GetMapping("/")
    @Operation(summary = "Información de la API")
    public Map<String, String> home() {
        return Map.of("mensaje", "Api-vehiculos, Version 1.0");
    }
}
