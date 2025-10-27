package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private VehicleController vehicleController;

    private VehicleResponse responseDTO;
    private VehicleRequest requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new VehicleResponse();
        responseDTO.setId(1L);
        responseDTO.setLicensePlate("ZG-1234-AB");

        requestDTO = new VehicleRequest();
        requestDTO.setLicensePlate("ZG-1234-AB");
    }

    @Test
    void getAllVehicles_ShouldReturnList() {
        when(vehicleService.findAllVehicles()).thenReturn(Arrays.asList(responseDTO));
        ResponseEntity<List<VehicleResponse>> response = vehicleController.getAllVehicles();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getVehicleById_WhenExists_ShouldReturnVehicle() {
        when(vehicleService.findVehicleById(1L)).thenReturn(Optional.of(responseDTO));
        ResponseEntity<VehicleResponse> response = vehicleController.getVehicleById(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void createVehicle_ShouldReturnCreatedVehicle() {
        when(vehicleService.createVehicle(any())).thenReturn(responseDTO);
        ResponseEntity<VehicleResponse> response = vehicleController.createVehicle(requestDTO);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void deleteVehicle_ShouldReturnNoContent() {
        doNothing().when(vehicleService).deleteVehicle(1L);
        ResponseEntity<Void> response = vehicleController.deleteVehicle(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
