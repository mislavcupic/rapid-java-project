package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * KOMPLETNI UNIT TESTOVI ZA AssignmentServiceImpl
 * Pokriva:
 * - CRUD operacije
 * - Driver workflow (startAssignment, completeAssignment)
 * - Shipment status transitions
 * - Assignment conflict detection
 */
@ExtendWith(MockitoExtension.class)
class AssignmentServiceImplTest {

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

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    private Assignment testAssignment;
    private AssignmentRequestDTO testRequest;
    private Driver testDriver;
    private Vehicle testVehicle;
    private Shipment testShipment;
    private Route testRoute;

    @BeforeEach
    void setUp() {
        // Setup UserInfo
        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("driver1");
        userInfo.setFirstName("John");
        userInfo.setLastName("Doe");

        // Setup Driver
        testDriver = new Driver();
        testDriver.setId(1L);
        testDriver.setUserInfo(userInfo);
        testDriver.setLicenseNumber("LIC-001");
        testDriver.setPhoneNumber("+385991234567");

        // Setup Vehicle
        testVehicle = new Vehicle();
        testVehicle.setId(1L);
        testVehicle.setLicensePlate("ZG-1234-AB");
        testVehicle.setMake("Mercedes");
        testVehicle.setModel("Sprinter");
        testVehicle.setYear(2022);

        // Setup Route
        testRoute = new Route();
        testRoute.setId(1L);
        testRoute.setOriginAddress("Zagreb, Croatia");
        testRoute.setDestinationAddress("Split, Croatia");
        testRoute.setEstimatedDistanceKm(380.5);
        testRoute.setStatus(RouteStatus.CALCULATED);

        // Setup Shipment
        testShipment = new Shipment();
        testShipment.setId(1L);
        testShipment.setTrackingNumber("SHIP-001");
        testShipment.setDescription("Test shipment");
        testShipment.setWeightKg(BigDecimal.valueOf(100.0));
        testShipment.setStatus(ShipmentStatus.PENDING);
        testShipment.setRoute(testRoute);
        testShipment.setExpectedDeliveryDate(LocalDateTime.now().plusDays(2));

        // Setup Assignment
        testAssignment = new Assignment();
        testAssignment.setId(1L);
        testAssignment.setDriver(testDriver);
        testAssignment.setVehicle(testVehicle);
        testAssignment.setShipment(testShipment);
        testAssignment.setStartTime(LocalDateTime.now().plusHours(2));
        testAssignment.setStatus("SCHEDULED");

        // Setup Request DTO
        testRequest = new AssignmentRequestDTO();
        testRequest.setDriverId(1L);
        testRequest.setVehicleId(1L);
        testRequest.setShipmentId(1L);
        testRequest.setStartTime(LocalDateTime.now().plusHours(2));
    }

    // ==========================================
    // CRUD TESTS
    // ==========================================

    @Test
    void testFindAll_Success() {
        // Arrange
        List<Assignment> assignments = Arrays.asList(testAssignment);
        when(assignmentRepository.findAll()).thenReturn(assignments);
        when(vehicleService.mapToResponse(any(Vehicle.class))).thenReturn(new VehicleResponse());
        when(shipmentService.mapToResponse(any(Shipment.class))).thenReturn(new ShipmentResponse());

        // Act
        List<AssignmentResponseDTO> result = assignmentService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(assignmentRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Success() {
        // Arrange
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(vehicleService.mapToResponse(any(Vehicle.class))).thenReturn(new VehicleResponse());
        when(shipmentService.mapToResponse(any(Shipment.class))).thenReturn(new ShipmentResponse());

        // Act
        Optional<AssignmentResponseDTO> result = assignmentService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        verify(assignmentRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(assignmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<AssignmentResponseDTO> result = assignmentService.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(assignmentRepository, times(1)).findById(999L);
    }

    @Test
    void testCreateAssignment_Success() {
        // Arrange
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        when(assignmentRepository.findByShipmentId(1L)).thenReturn(Optional.empty());
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);
        when(vehicleService.mapToResponse(any(Vehicle.class))).thenReturn(new VehicleResponse());
        when(shipmentService.mapToResponse(any(Shipment.class))).thenReturn(new ShipmentResponse());

        // Act
        AssignmentResponseDTO result = assignmentService.createAssignment(testRequest);

        // Assert
        assertNotNull(result);
        verify(driverRepository, times(1)).findById(1L);
        verify(vehicleRepository, times(1)).findById(1L);
        verify(shipmentRepository, times(1)).findById(1L);
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
        verify(shipmentRepository, times(1)).save(any(Shipment.class));
    }

    @Test
    void testCreateAssignment_DriverNotFound() {
        // Arrange
        when(driverRepository.findById(999L)).thenReturn(Optional.empty());

        AssignmentRequestDTO request = new AssignmentRequestDTO();
        request.setDriverId(999L);
        request.setVehicleId(1L);
        request.setShipmentId(1L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            assignmentService.createAssignment(request);
        });

        verify(driverRepository, times(1)).findById(999L);
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void testCreateAssignment_VehicleNotFound() {
        // Arrange
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        AssignmentRequestDTO request = new AssignmentRequestDTO();
        request.setDriverId(1L);
        request.setVehicleId(999L);
        request.setShipmentId(1L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            assignmentService.createAssignment(request);
        });

        verify(vehicleRepository, times(1)).findById(999L);
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void testCreateAssignment_ShipmentNotFound() {
        // Arrange
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

        AssignmentRequestDTO request = new AssignmentRequestDTO();
        request.setDriverId(1L);
        request.setVehicleId(1L);
        request.setShipmentId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            assignmentService.createAssignment(request);
        });

        verify(shipmentRepository, times(1)).findById(999L);
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void testCreateAssignment_ShipmentAlreadyAssigned() {
        // Arrange
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        when(assignmentRepository.findByShipmentId(1L)).thenReturn(Optional.of(testAssignment));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            assignmentService.createAssignment(testRequest);
        });

        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void testCreateAssignment_InvalidShipmentStatus() {
        // Arrange
        testShipment.setStatus(ShipmentStatus.IN_TRANSIT); // Not PENDING

        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        when(assignmentRepository.findByShipmentId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            assignmentService.createAssignment(testRequest);
        });

        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void testUpdateAssignment_Success() {
        // Arrange
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);
        when(vehicleService.mapToResponse(any(Vehicle.class))).thenReturn(new VehicleResponse());
        when(shipmentService.mapToResponse(any(Shipment.class))).thenReturn(new ShipmentResponse());

        // Act
        AssignmentResponseDTO result = assignmentService.updateAssignment(1L, testRequest);

        // Assert
        assertNotNull(result);
        verify(assignmentRepository, times(1)).findById(1L);
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
    }

    @Test
    void testUpdateAssignment_ChangeShipment() {
        // Arrange
        Shipment newShipment = new Shipment();
        newShipment.setId(2L);
        newShipment.setTrackingNumber("SHIP-002");
        newShipment.setStatus(ShipmentStatus.PENDING);
        newShipment.setRoute(testRoute);

        testRequest.setShipmentId(2L);

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(shipmentRepository.findById(2L)).thenReturn(Optional.of(newShipment));
        when(assignmentRepository.findByShipmentId(2L)).thenReturn(Optional.empty());
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);
        when(vehicleService.mapToResponse(any(Vehicle.class))).thenReturn(new VehicleResponse());
        when(shipmentService.mapToResponse(any(Shipment.class))).thenReturn(new ShipmentResponse());

        // Act
        AssignmentResponseDTO result = assignmentService.updateAssignment(1L, testRequest);

        // Assert
        assertNotNull(result);
        verify(shipmentRepository, times(2)).save(any(Shipment.class)); // Old & new shipment
    }

    @Test
    void testUpdateAssignment_NotFound() {
        // Arrange
        when(assignmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            assignmentService.updateAssignment(999L, testRequest);
        });

        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void testDeleteAssignment_Success() {
        // Arrange
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);
        doNothing().when(assignmentRepository).delete(any(Assignment.class));

        // Act
        assignmentService.deleteAssignment(1L);

        // Assert
        verify(assignmentRepository, times(1)).findById(1L);
        verify(shipmentRepository, times(1)).save(any(Shipment.class));
        verify(assignmentRepository, times(1)).delete(any(Assignment.class));
    }

    @Test
    void testDeleteAssignment_NotFound() {
        // Arrange
        when(assignmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            assignmentService.deleteAssignment(999L);
        });

        verify(assignmentRepository, never()).delete(any(Assignment.class));
    }

    // ==========================================
    // DRIVER WORKFLOW TESTS
    // ==========================================

    @Test
    void testFindAssignmentsByDriver_Success() {
        // Arrange
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(assignmentRepository.findByDriverIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(Arrays.asList(testAssignment));
        when(vehicleService.mapToResponse(any(Vehicle.class))).thenReturn(new VehicleResponse());
        when(shipmentService.mapToResponse(any(Shipment.class))).thenReturn(new ShipmentResponse());

        // Act
        List<AssignmentResponseDTO> result = assignmentService.findAssignmentsByDriver(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(driverRepository, times(1)).findById(1L);
        verify(assignmentRepository, times(1)).findByDriverIdAndStatusIn(eq(1L), anyList());
    }

    @Test
    void testStartAssignment_Success() {
        // Arrange
        testShipment.setStatus(ShipmentStatus.SCHEDULED);
        testAssignment.setStatus("SCHEDULED");
        testAssignment.setStartTime(null); // Will be set to now

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);
        when(vehicleService.mapToResponse(any(Vehicle.class))).thenReturn(new VehicleResponse());
        when(shipmentService.mapToResponse(any(Shipment.class))).thenReturn(new ShipmentResponse());

        // Act
        AssignmentResponseDTO result = assignmentService.startAssignment(1L, 1L);

        // Assert
        assertNotNull(result);
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
        verify(shipmentRepository, times(1)).save(any(Shipment.class));
    }

    @Test
    void testStartAssignment_WrongDriver() {
        // Arrange
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            assignmentService.startAssignment(1L, 999L);
        });

        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void testStartAssignment_InvalidStatus() {
        // Arrange
        testAssignment.setStatus("IN_PROGRESS");
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            assignmentService.startAssignment(1L, 1L);
        });

        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void testCompleteAssignment_Success() {
        // Arrange
        testShipment.setStatus(ShipmentStatus.DELIVERED);
        testAssignment.setStatus("IN_PROGRESS");

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);
        when(vehicleService.mapToResponse(any(Vehicle.class))).thenReturn(new VehicleResponse());
        when(shipmentService.mapToResponse(any(Shipment.class))).thenReturn(new ShipmentResponse());

        // Act
        AssignmentResponseDTO result = assignmentService.completeAssignment(1L, 1L);

        // Assert
        assertNotNull(result);
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
    }

    @Test
    void testCompleteAssignment_ShipmentNotDelivered() {
        // Arrange
        testShipment.setStatus(ShipmentStatus.IN_TRANSIT);
        testAssignment.setStatus("IN_PROGRESS");

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            assignmentService.completeAssignment(1L, 1L);
        });

        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void testCompleteAssignment_InvalidStatus() {
        // Arrange
        testShipment.setStatus(ShipmentStatus.DELIVERED);
        testAssignment.setStatus("SCHEDULED");

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            assignmentService.completeAssignment(1L, 1L);
        });

        verify(assignmentRepository, never()).save(any(Assignment.class));
    }
}
