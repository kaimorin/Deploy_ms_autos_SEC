package cl.matiivilla.vehiculos.service;

import cl.matiivilla.vehiculos.dto.VehiculoRequest;
import cl.matiivilla.vehiculos.dto.VehiculoResponse;
import cl.matiivilla.vehiculos.model.Vehiculo;
import cl.matiivilla.vehiculos.repository.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @InjectMocks
    private VehiculoService vehiculoService;

    private Vehiculo vehiculo;
    private VehiculoRequest request;

    @BeforeEach
    void setUp() {
        vehiculo = Vehiculo.builder()
                .id(1L)
                .patente("ABCD12")
                .marca("Toyota")
                .modelo("Yaris")
                .anio(2020)
                .color("Rojo")
                .tipo("Sedan")
                .disponible(true)
                .build();

        request = new VehiculoRequest();
        request.setPatente("ABCD12");
        request.setMarca("Toyota");
        request.setModelo("Yaris");
        request.setAnio(2020);
        request.setColor("Rojo");
        request.setTipo("Sedan");
        request.setDisponible(true);
    }

    // ---------- findAll ----------

    @Test
    void findAll_devuelveListaDeVehiculos() {
        when(vehiculoRepository.findAll()).thenReturn(List.of(vehiculo));

        List<VehiculoResponse> resultado = vehiculoService.findAll();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getPatente()).isEqualTo("ABCD12");
        verify(vehiculoRepository).findAll();
    }

    // ---------- findById ----------

    @Test
    void findById_cuandoExiste_devuelveVehiculo() {
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));

        VehiculoResponse resultado = vehiculoService.findById(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getMarca()).isEqualTo("Toyota");
    }

    @Test
    void findById_cuandoNoExiste_lanza404() {
        when(vehiculoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehiculoService.findById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ---------- create ----------

    @Test
    void create_cuandoPatenteNoExiste_guardaVehiculo() {
        when(vehiculoRepository.existsByPatente("ABCD12")).thenReturn(false);
        when(vehiculoRepository.save(any(Vehiculo.class))).thenReturn(vehiculo);

        VehiculoResponse resultado = vehiculoService.create(request);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getPatente()).isEqualTo("ABCD12");
        verify(vehiculoRepository).save(any(Vehiculo.class));
    }

    @Test
    void create_cuandoPatenteYaExiste_lanza409() {
        when(vehiculoRepository.existsByPatente("ABCD12")).thenReturn(true);

        assertThatThrownBy(() -> vehiculoService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(vehiculoRepository, never()).save(any());
    }

    // ---------- update ----------

    @Test
    void update_cuandoExiste_actualizaVehiculo() {
        request.setColor("Azul");
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));
        when(vehiculoRepository.findByPatente("ABCD12")).thenReturn(Optional.of(vehiculo)); // es el mismo vehículo
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(inv -> inv.getArgument(0));

        VehiculoResponse resultado = vehiculoService.update(1L, request);

        assertThat(resultado.getColor()).isEqualTo("Azul");
        verify(vehiculoRepository).save(vehiculo);
    }

    @Test
    void update_cuandoPatenteEsDeOtroVehiculo_lanza409() {
        Vehiculo otro = Vehiculo.builder().id(2L).patente("ABCD12").build();
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));
        when(vehiculoRepository.findByPatente("ABCD12")).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> vehiculoService.update(1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(vehiculoRepository, never()).save(any());
    }

    @Test
    void update_cuandoNoExiste_lanza404() {
        when(vehiculoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehiculoService.update(99L, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ---------- delete ----------

    @Test
    void delete_cuandoExiste_eliminaVehiculo() {
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));

        vehiculoService.delete(1L);

        verify(vehiculoRepository).delete(vehiculo);
    }

    @Test
    void delete_cuandoNoExiste_lanza404() {
        when(vehiculoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehiculoService.delete(99L))
                .isInstanceOf(ResponseStatusException.class);

        verify(vehiculoRepository, never()).delete(any());
    }
}
