package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Vehicle;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ConflictException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.VehicleRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleService Unit Tests - CORRECTED")
class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Vehicle testVehicle;
    private VehicleRequest vehicleRequest;

    @BeforeEach
    void setUp() {
        // Setup test Vehicle (CORRECTED - NO status, NO volumeCapacity)
        testVehicle = new Vehicle();
        testVehicle.setId(1L);
        testVehicle.setLicensePlate("ZG-1234-AB");
        testVehicle.setMake("Mercedes");  // ← ISPRAVNO: 'make' ne 'manufacturer'
        testVehicle.setModel("Actros");
        testVehicle.setYear(2020);
        testVehicle.setFuelType("Diesel");
        testVehicle.setLoadCapacityKg(BigDecimal.valueOf(18000));  // ← BigDecimal!
        testVehicle.setCurrentMileageKm(50000L);
        testVehicle.setLastServiceDate(LocalDate.now().minusMonths(3));
        testVehicle.setNextServiceMileageKm(60000L);
        testVehicle.setFuelConsumptionLitersPer100Km(BigDecimal.valueOf(25.5));

        // Setup request DTO (CORRECTED)
        vehicleRequest = new VehicleRequest();
        vehicleRequest.setLicensePlate("ZG-1234-AB");
        vehicleRequest.setMake("Mercedes");
        vehicleRequest.setModel("Actros");
        vehicleRequest.setModelYear(2020);
        vehicleRequest.setFuelType("Diesel");
        vehicleRequest.setLoadCapacityKg(BigDecimal.valueOf(18000));
        vehicleRequest.setCurrentMileageKm(50000L);
        vehicleRequest.setLastServiceDate(LocalDate.now().minusMonths(3));
        vehicleRequest.setNextServiceMileageKm(60000L);
        vehicleRequest.setFuelConsumptionLitersPer100Km(BigDecimal.valueOf(25.5));
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {

        @Test
        @DisplayName("Should return all vehicles")
        void findAllVehicles_ShouldReturnAllVehicles() {
            // Given
            when(vehicleRepository.findAll()).thenReturn(Arrays.asList(testVehicle));

            // When
            List<VehicleResponse> result = vehicleService.findAllVehicles();

            // Then
            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLicensePlate()).isEqualTo("ZG-1234-AB");
            assertThat(result.get(0).getMake()).isEqualTo("Mercedes");
            verify(vehicleRepository).findAll();
        }

        @Test
        @DisplayName("Should return vehicle by ID")
        void findVehicleById_WhenExists_ShouldReturnVehicle() {
            // Given
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

            // When
            Optional<VehicleResponse> result = vehicleService.findVehicleById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
            assertThat(result.get().getMake()).isEqualTo("Mercedes");
            verify(vehicleRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return empty when vehicle not found")
        void findVehicleById_WhenNotFound_ShouldReturnEmpty() {
            // Given
            when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<VehicleResponse> result = vehicleService.findVehicleById(999L);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Create Vehicle")
    class CreateVehicle {

        @Test
        @DisplayName("Should create vehicle with valid data")
        void createVehicle_WithValidData_ShouldCreateVehicle() {
            // Given
            when(vehicleRepository.findByLicensePlate("ZG-1234-AB")).thenReturn(Optional.empty());
            when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);

            // When
            VehicleResponse result = vehicleService.createVehicle(vehicleRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getLicensePlate()).isEqualTo("ZG-1234-AB");
            assertThat(result.getMake()).isEqualTo("Mercedes");
            assertThat(result.getLoadCapacityKg()).isEqualTo(BigDecimal.valueOf(18000));
            verify(vehicleRepository).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should throw exception when license plate already exists")
        void createVehicle_WhenLicensePlateExists_ShouldThrowException() {
            // Given
            when(vehicleRepository.findByLicensePlate("ZG-1234-AB"))
                    .thenReturn(Optional.of(testVehicle));

            // When & Then
            assertThatThrownBy(() -> vehicleService.createVehicle(vehicleRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("registracijom")
                    .hasMessageContaining("ZG-1234-AB");

            verify(vehicleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Vehicle")
    class UpdateVehicle {

        @Test
        @DisplayName("Should update vehicle with valid data")
        void updateVehicle_WithValidData_ShouldUpdateVehicle() {
            // Given
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
            when(vehicleRepository.findByLicensePlate("ZG-1234-AB"))
                    .thenReturn(Optional.of(testVehicle));
            when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);

            // When
            VehicleResponse result = vehicleService.updateVehicle(1L, vehicleRequest);

            // Then
            assertThat(result).isNotNull();
            verify(vehicleRepository).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should throw exception when vehicle not found")
        void updateVehicle_WhenNotFound_ShouldThrowException() {
            // Given
            when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> vehicleService.updateVehicle(999L, vehicleRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Vozilo");
        }

        @Test
        @DisplayName("Should throw exception when updating to existing license plate")
        void updateVehicle_WhenLicensePlateConflict_ShouldThrowException() {
            // Given
            Vehicle otherVehicle = new Vehicle();
            otherVehicle.setId(2L);
            otherVehicle.setLicensePlate("ZG-1234-AB");

            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
            when(vehicleRepository.findByLicensePlate("ZG-1234-AB"))
                    .thenReturn(Optional.of(otherVehicle));

            // When & Then
            assertThatThrownBy(() -> vehicleService.updateVehicle(1L, vehicleRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("registracijom");

            verify(vehicleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Vehicle")
    class DeleteVehicle {

        @Test
        @DisplayName("Should delete vehicle when exists")
        void deleteVehicle_WhenExists_ShouldDelete() {
            // Given
            when(vehicleRepository.existsById(1L)).thenReturn(true);
            doNothing().when(vehicleRepository).deleteById(1L);

            // When
            vehicleService.deleteVehicle(1L);

            // Then
            verify(vehicleRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when vehicle not found")
        void deleteVehicle_WhenNotFound_ShouldThrowException() {
            // Given
            when(vehicleRepository.existsById(999L)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> vehicleService.deleteVehicle(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Vozilo");
        }
    }

    @Nested
    @DisplayName("Analytics Methods")
    class AnalyticsMethods {

        @Test
        @DisplayName("Should count total vehicles")
        void countTotalVehicles_ShouldReturnCount() {
            // Given
            when(vehicleRepository.count()).thenReturn(10L);

            // When
            Long result = vehicleService.countTotalVehicles();

            // Then
            assertThat(result).isEqualTo(10L);
            verify(vehicleRepository).count();
        }

        @Test
        @DisplayName("Should count free vehicles (without driver)")
        void countFreeVehicles_ShouldReturnCount() {
            // Given
            List<Vehicle> allVehicles = Arrays.asList(testVehicle);
            when(vehicleRepository.findAll()).thenReturn(allVehicles);

            // When
            Long result = vehicleService.countFreeVehicles();

            // Then
            assertThat(result).isEqualTo(1L);  // testVehicle has no driver
            verify(vehicleRepository).findAll();
        }

        @Test
        @DisplayName("Should find overdue maintenance vehicles")
        void findOverdueMaintenanceVehicles_ShouldReturnVehicles() {
            // Given - vehicle with negative remaining km (overdue)
            testVehicle.setCurrentMileageKm(65000L);  // Passed next service
            testVehicle.setNextServiceMileageKm(60000L);
            when(vehicleRepository.findAll()).thenReturn(Arrays.asList(testVehicle));

            // When
            List<VehicleResponse> result = vehicleService.findOverdueMaintenanceVehicles();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRemainingKmToService()).isNegative();
            verify(vehicleRepository).findAll();
        }

        @Test
        @DisplayName("Should find warning maintenance vehicles")
        void findWarningMaintenanceVehicles_ShouldReturnVehicles() {
            // Given - vehicle within warning threshold
            testVehicle.setCurrentMileageKm(56000L);  // 4000 km to service
            testVehicle.setNextServiceMileageKm(60000L);
            when(vehicleRepository.findAll()).thenReturn(Arrays.asList(testVehicle));

            // When
            List<VehicleResponse> result = vehicleService.findWarningMaintenanceVehicles(5000L);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRemainingKmToService()).isBetween(1L, 5000L);
            verify(vehicleRepository).findAll();
        }

        @Test
        @DisplayName("Should find free vehicles details")
        void findFreeVehiclesDetails_ShouldReturnFreeVehicles() {
            // Given
            when(vehicleRepository.findAll()).thenReturn(Arrays.asList(testVehicle));

            // When
            List<VehicleResponse> result = vehicleService.findFreeVehiclesDetails();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCurrentDriver()).isNull();
            verify(vehicleRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Map To Response")
    class MapToResponse {

        @Test
        @DisplayName("Should map vehicle to response DTO correctly")
        void mapToResponse_ShouldMapCorrectly() {
            // When
            VehicleResponse result = vehicleService.mapToResponse(testVehicle);

            // Then
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getLicensePlate()).isEqualTo("ZG-1234-AB");
            assertThat(result.getMake()).isEqualTo("Mercedes");
            assertThat(result.getModel()).isEqualTo("Actros");
            assertThat(result.getModelYear()).isEqualTo(2020);
            assertThat(result.getLoadCapacityKg()).isEqualTo(BigDecimal.valueOf(18000));
            assertThat(result.getCurrentMileageKm()).isEqualTo(50000L);
            assertThat(result.getNextServiceMileageKm()).isEqualTo(60000L);
            assertThat(result.getRemainingKmToService()).isEqualTo(10000L);
        }
    }
}
