package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Assignment;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Shipment;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Vehicle;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.AssignmentRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.DriverRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.ShipmentRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.VehicleRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl.AssignmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit testovi za AssignmentService
 * Testira sve metode iz STVARNOG AssignmentService interface-a
 */
@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private ShipmentService shipmentService;

    private AssignmentService assignmentService;

    private Assignment assignment;
    private Driver driver;
    private Vehicle vehicle;
    private Shipment shipment;
    private AssignmentRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        // Create service implementation with mocked dependencies
        assignmentService = new AssignmentServiceImpl(
            assignmentRepository,
            driverRepository,
            vehicleRepository,
            shipmentRepository,
            vehicleService,
            shipmentService
        );

        // Setup test data
        driver = new Driver();
        driver.setId(1L);

        vehicle = new Vehicle();
        vehicle.setId(2L);

        shipment = new Shipment();
        shipment.setId(3L);

        assignment = new Assignment();
        assignment.setId(1L);
        assignment.setDriver(driver);
        assignment.setVehicle(vehicle);
        assignment.setShipment(shipment);
        assignment.setStartTime(LocalDateTime.of(2025, 1, 15, 8, 0));
        assignment.setStatus("ACTIVE");

        requestDTO = new AssignmentRequestDTO();
        requestDTO.setDriverId(1L);
        requestDTO.setVehicleId(2L);
        requestDTO.setShipmentId(3L);
        requestDTO.setStartTime(LocalDateTime.of(2025, 1, 15, 8, 0));
    }

    @Test
    void findAll_ShouldReturnListOfAssignmentResponseDTOs() {
        // Arrange
        when(assignmentRepository.findAll()).thenReturn(Arrays.asList(assignment));

        // Act
        List<AssignmentResponseDTO> result = assignmentService.findAll();

        // Assert
        assertThat(result).isNotEmpty().hasSize(1);
        verify(assignmentRepository, times(1)).findAll();
    }

    @Test
    void findAll_WhenNoAssignments_ShouldReturnEmptyList() {
        // Arrange
        when(assignmentRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<AssignmentResponseDTO> result = assignmentService.findAll();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findById_WhenAssignmentExists_ShouldReturnOptionalWithDTO() {
        // Arrange
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

        // Act
        Optional<AssignmentResponseDTO> result = assignmentService.findById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(assignmentRepository, times(1)).findById(1L);
    }

    @Test
    void findById_WhenAssignmentNotFound_ShouldReturnEmptyOptional() {
        // Arrange
        when(assignmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<AssignmentResponseDTO> result = assignmentService.findById(999L);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void createAssignment_WhenValidRequest_ShouldReturnCreatedAssignment() {
        // Arrange
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(vehicleRepository.findById(2L)).thenReturn(Optional.of(vehicle));
        when(shipmentRepository.findById(3L)).thenReturn(Optional.of(shipment));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(assignment);

        // Act
        AssignmentResponseDTO result = assignmentService.createAssignment(requestDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(driverRepository, times(1)).findById(1L);
        verify(vehicleRepository, times(1)).findById(2L);
        verify(shipmentRepository, times(1)).findById(3L);
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
    }

    @Test
    void createAssignment_WhenDriverNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(driverRepository.findById(999L)).thenReturn(Optional.empty());
        requestDTO.setDriverId(999L);

        // Act & Assert
        assertThatThrownBy(() -> assignmentService.createAssignment(requestDTO))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Driver");

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_WhenVehicleNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());
        requestDTO.setVehicleId(999L);

        // Act & Assert
        assertThatThrownBy(() -> assignmentService.createAssignment(requestDTO))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Vehicle");

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_WhenShipmentNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(vehicleRepository.findById(2L)).thenReturn(Optional.of(vehicle));
        when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());
        requestDTO.setShipmentId(999L);

        // Act & Assert
        assertThatThrownBy(() -> assignmentService.createAssignment(requestDTO))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Shipment");

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void updateAssignment_WhenValidRequest_ShouldReturnUpdatedAssignment() {
        // Arrange
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(vehicleRepository.findById(2L)).thenReturn(Optional.of(vehicle));
        when(shipmentRepository.findById(3L)).thenReturn(Optional.of(shipment));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(assignment);

        // Act
        AssignmentResponseDTO result = assignmentService.updateAssignment(1L, requestDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(assignmentRepository, times(1)).findById(1L);
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
    }

    @Test
    void updateAssignment_WhenAssignmentNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(assignmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> assignmentService.updateAssignment(999L, requestDTO))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void deleteAssignment_WhenAssignmentExists_ShouldDeleteSuccessfully() {
        // Arrange
        when(assignmentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(assignmentRepository).deleteById(1L);

        // Act
        assignmentService.deleteAssignment(1L);

        // Assert
        verify(assignmentRepository, times(1)).existsById(1L);
        verify(assignmentRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteAssignment_WhenAssignmentNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(assignmentRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> assignmentService.deleteAssignment(999L))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(assignmentRepository, never()).deleteById(any());
    }

    @Test
    void findAssignmentsByDriver_ShouldReturnDriverAssignments() {
        // Arrange
        when(assignmentRepository.findByDriverId(1L)).thenReturn(Arrays.asList(assignment));

        // Act
        List<AssignmentResponseDTO> result = assignmentService.findAssignmentsByDriver(1L);

        // Assert
        assertThat(result).isNotEmpty().hasSize(1);
        verify(assignmentRepository, times(1)).findByDriverId(1L);
    }

    @Test
    void findAssignmentsByDriver_WhenNoAssignments_ShouldReturnEmptyList() {
        // Arrange
        when(assignmentRepository.findByDriverId(999L)).thenReturn(Arrays.asList());

        // Act
        List<AssignmentResponseDTO> result = assignmentService.findAssignmentsByDriver(999L);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void mapToResponse_ShouldMapAssignmentToResponseDTO() {
        // Act
        AssignmentResponseDTO result = assignmentService.mapToResponse(assignment);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAssignmentStatus()).isEqualTo("ACTIVE");
    }
}
