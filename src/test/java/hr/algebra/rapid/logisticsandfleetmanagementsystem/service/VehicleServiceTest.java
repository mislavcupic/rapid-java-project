package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Vehicle;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.VehicleRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.DriverRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverRepository driverRepository;

    private VehicleService vehicleService;
    private Vehicle vehicle;
    private VehicleRequest requestDTO;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleServiceImpl(vehicleRepository, driverRepository);
        
        vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setLicensePlate("ZG-1234-AB");
        vehicle.setMake("Mercedes");

        requestDTO = new VehicleRequest();
        requestDTO.setLicensePlate("ZG-1234-AB");
    }

    @Test
    void findAllVehicles_ShouldReturnList() {
        when(vehicleRepository.findAll()).thenReturn(Arrays.asList(vehicle));
        List<VehicleResponse> result = vehicleService.findAllVehicles();
        assertThat(result).isNotEmpty();
    }

    @Test
    void findVehicleById_WhenExists_ShouldReturnVehicle() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        Optional<VehicleResponse> result = vehicleService.findVehicleById(1L);
        assertThat(result).isPresent();
    }

    @Test
    void createVehicle_ShouldCreateVehicle() {
        when(vehicleRepository.save(any())).thenReturn(vehicle);
        VehicleResponse result = vehicleService.createVehicle(requestDTO);
        assertThat(result).isNotNull();
    }

    @Test
    void deleteVehicle_ShouldDelete() {
        when(vehicleRepository.existsById(1L)).thenReturn(true);
        vehicleService.deleteVehicle(1L);
        verify(vehicleRepository, times(1)).deleteById(1L);
    }

    @Test
    void countTotalVehicles_ShouldReturnCount() {
        when(vehicleRepository.count()).thenReturn(10L);
        Long result = vehicleService.countTotalVehicles();
        assertThat(result).isEqualTo(10L);
    }
}
