package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.DriverService;
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
class DriverControllerTest {

    @Mock
    private DriverService driverService;

    @InjectMocks
    private DriverController driverController;

    private DriverResponseDTO responseDTO;
    private DriverRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new DriverResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setLicenseNumber("LIC12345");

        requestDTO = new DriverRequestDTO();
        requestDTO.setLicenseNumber("LIC12345");
    }

    @Test
    void getAllDrivers_ShouldReturnList() {
        when(driverService.findAllDrivers()).thenReturn(Arrays.asList(responseDTO));
        ResponseEntity<List<DriverResponseDTO>> response = driverController.getAllDrivers();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getDriverById_WhenExists_ShouldReturnDriver() {
        when(driverService.findDriverById(1L)).thenReturn(Optional.of(responseDTO));
        ResponseEntity<DriverResponseDTO> response = driverController.getDriverById(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void createDriver_ShouldReturnCreatedDriver() {
        when(driverService.createDriver(any())).thenReturn(responseDTO);
        ResponseEntity<DriverResponseDTO> response = driverController.createDriver(requestDTO);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void deleteDriver_ShouldReturnNoContent() {
        doNothing().when(driverService).deleteDriver(1L);
        ResponseEntity<Void> response = driverController.deleteDriver(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
