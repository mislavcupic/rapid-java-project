package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ConflictException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceImplTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private RouteRepository routeRepository; // Dodano jer servis koristi routeRepository
    @Mock private VehicleService vehicleService;
    @Mock private ShipmentService shipmentService;

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    private Assignment testAssignment;
    private AssignmentRequestDTO testRequest;
    private Driver testDriver;
    private Vehicle testVehicle;
    private Shipment testShipment;

    @BeforeEach
    void setUp() {
        testDriver = new Driver();
        testDriver.setId(1L);

        testVehicle = new Vehicle();
        testVehicle.setId(1L);

        testShipment = new Shipment();
        testShipment.setId(1L);
        testShipment.setStatus(ShipmentStatus.PENDING);
        // Za calculateHaversine test
        testShipment.setOriginLatitude(45.8150);
        testShipment.setOriginLongitude(15.9819);
        testShipment.setDestinationLatitude(45.8155);
        testShipment.setDestinationLongitude(15.9820);

        testAssignment = new Assignment();
        testAssignment.setId(1L);
        testAssignment.setDriver(testDriver);
        testAssignment.setVehicle(testVehicle);
        // POPRAVAK: Inicijalizacija liste, a ne castanje
        List<Shipment> shipments = new ArrayList<>();
        shipments.add(testShipment);
        testAssignment.setShipments(shipments);
        testAssignment.setStatus("SCHEDULED");

        testRequest = new AssignmentRequestDTO();
        testRequest.setDriverId(1L);
        testRequest.setVehicleId(1L);
        testRequest.setShipmentIds(Collections.singletonList(1L));
        testRequest.setStartTime(LocalDateTime.now().plusHours(2));

        lenient().when(vehicleService.mapToResponse(any())).thenReturn(new VehicleResponse());
        lenient().when(shipmentService.mapToResponse(any())).thenReturn(new ShipmentResponse());
    }

    @Test
    @DisplayName("Dohvat svih - Coverage 100%")
    void findAll_Success() {
        when(assignmentRepository.findAll()).thenReturn(List.of(testAssignment));
        List<AssignmentResponseDTO> result = assignmentService.findAll();
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Kreiranje naloga - Branch: No shipments (Conflict)")
    void createAssignment_NoShipments_ThrowsConflict() {
        testRequest.setShipmentIds(Collections.emptyList());
        when(driverRepository.findById(any())).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findById(any())).thenReturn(Optional.of(testVehicle));
        when(shipmentRepository.findAllById(any())).thenReturn(Collections.emptyList());

        assertThrows(ConflictException.class, () -> assignmentService.createAssignment(testRequest));
    }

    @Test
    @DisplayName("Kreiranje naloga - Success")
    void createAssignment_Success() {
        when(driverRepository.findById(any())).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findById(any())).thenReturn(Optional.of(testVehicle));
        when(shipmentRepository.findAllById(any())).thenReturn(List.of(testShipment));
        when(routeRepository.save(any())).thenReturn(new Route());
        when(assignmentRepository.save(any())).thenReturn(testAssignment);

        AssignmentResponseDTO result = assignmentService.createAssignment(testRequest);
        assertNotNull(result);
        assertEquals(ShipmentStatus.SCHEDULED, testShipment.getStatus());
    }

    @Test
    @DisplayName("Start Assignment - Branch: Wrong Driver (Conflict)")
    void startAssignment_WrongDriver_ThrowsConflict() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));

        // Pokušava startati vozač s ID 99, a nalog je od ID 1
        assertThrows(ConflictException.class, () -> assignmentService.startAssignment(1L, 99L));
    }

    @Test
    @DisplayName("Start Assignment - Success")
    void startAssignment_Success() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.saveAndFlush(any())).thenReturn(testAssignment);

        Optional<AssignmentResponseDTO> result = assignmentService.startAssignment(1L, 1L);

        assertTrue(result.isPresent());
        assertEquals("IN_PROGRESS", testAssignment.getStatus());
        assertEquals(ShipmentStatus.IN_TRANSIT, testShipment.getStatus());
    }

    @Test
    @DisplayName("Optimize Order - Branch: Haversine Calculation")
    void optimizeAssignmentOrder_Success() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));

        AssignmentResponseDTO result = assignmentService.optimizeAssignmentOrder(1L);

        assertNotNull(result);
        verify(shipmentRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("Update Status - Branch: Mapping Switch Case")
    void updateStatus_AllCases() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.save(any())).thenReturn(testAssignment);

        // Testiranje grananja u mapToShipmentStatus (IN_PROGRESS -> IN_TRANSIT)
        assignmentService.updateStatus(1L, "IN_PROGRESS");
        assertEquals(ShipmentStatus.IN_TRANSIT, testShipment.getStatus());

        // Testiranje grananja (COMPLETED -> DELIVERED)
        assignmentService.updateStatus(1L, "COMPLETED");
        assertEquals(ShipmentStatus.DELIVERED, testShipment.getStatus());
    }

    @Test
    @DisplayName("Delete Assignment - Branch: ResourceNotFound")
    void deleteAssignment_NotFound_ThrowsException() {
        when(assignmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> assignmentService.deleteAssignment(99L));
    }

    // --- 1. TEST ZA MAPIRANJE (mapToResponse coverage) ---
    @Test
    @DisplayName("Coverage: Mapiranje naloga bez vozača i vozila")
    void mapToResponse_NullDriverAndVehicle() {
        Assignment emptyAssignment = new Assignment();
        emptyAssignment.setId(10L);
        emptyAssignment.setDriver(null);
        emptyAssignment.setVehicle(null);
        emptyAssignment.setShipments(null);

        AssignmentResponseDTO result = assignmentService.mapToResponse(emptyAssignment);

        assertNotNull(result);
        assertNull(result.getDriver());
        assertNull(result.getVehicle());
    }

    // --- 2. TEST ZA UPDATE STATUS (Branch: Default case u switchu) ---
    @Test
    @DisplayName("Branch: Update statusa na nepoznatu vrijednost (Default case)")
    void updateStatus_DefaultCase() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.save(any())).thenReturn(testAssignment);

        // Šaljemo status koji nije IN_PROGRESS, COMPLETED ili SCHEDULED
        assignmentService.updateStatus(1L, "UNKNOWN_STATUS");

        // Provjeravamo da je mapirano na PENDING (default grana u mapToShipmentStatus)
        assertEquals(ShipmentStatus.PENDING, testShipment.getStatus());
    }

    // --- 3. TEST ZA CREATE (Branch: Driver Not Found) ---
    @Test
    @DisplayName("Exception: Kreiranje neuspješno jer vozač ne postoji")
    void createAssignment_DriverNotFound() {
        when(driverRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> assignmentService.createAssignment(testRequest));
    }

    // --- 4. TEST ZA UPDATE (Branch: Assignment Not Found) ---
    @Test
    @DisplayName("Branch: Update nepostojećeg naloga")
    void updateAssignment_NotFound() {
        when(assignmentRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<AssignmentResponseDTO> result = assignmentService.updateAssignment(99L, testRequest);

        assertTrue(result.isEmpty());
    }

    // --- 5. TEST ZA OPTIMIZE (Branch: No Shipments) ---
    @Test
    @DisplayName("Branch: Optimizacija naloga bez pošiljaka")
    void optimizeAssignmentOrder_NoShipments() {
        testAssignment.setShipments(new ArrayList<>()); // Prazna lista
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));

        AssignmentResponseDTO result = assignmentService.optimizeAssignmentOrder(1L);

        assertNotNull(result);
        verify(shipmentRepository, never()).save(any());
    }

    // --- 6. TEST ZA START (Branch: Assignment Not Found) ---
    @Test
    @DisplayName("Branch: Startanje nepostojećeg naloga")
    void startAssignment_NotFound() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<AssignmentResponseDTO> result = assignmentService.startAssignment(1L, 1L);

        assertTrue(result.isEmpty());
    }

    // --- 7. TEST ZA COMPLETE (Branch: Wrong Driver) ---
    @Test
    @DisplayName("Conflict: Završetak naloga od strane pogrešnog vozača")
    void completeAssignment_WrongDriver() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));

        assertThrows(ConflictException.class, () -> assignmentService.completeAssignment(1L, 99L));
    }

    // --- 8. TEST ZA FORCE UPDATE (Line Coverage) ---
    @Test
    @DisplayName("Coverage: Force update statusa pošiljke")
    void forceUpdateShipmentStatus_Success() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));

        assignmentService.updateShipmentStatus(1L, ShipmentStatus.IN_TRANSIT);

        assertEquals(ShipmentStatus.IN_TRANSIT, testShipment.getStatus());
        verify(shipmentRepository).save(testShipment);
    }

    // --- 9. TEST ZA FIND BY DRIVER (Branch: Prazna lista za vozača) ---
    @Test
    @DisplayName("Branch: Dohvat rasporeda za vozača bez naloga")
    void findAssignmentsByDriver_Empty() {
        when(assignmentRepository.findByDriverIdAndStatusIn(anyLong(), any()))
                .thenReturn(Collections.emptyList());

        List<AssignmentResponseDTO> result = assignmentService.findAssignmentsByDriver(1L);

        assertTrue(result.isEmpty());
    }

    // --- 10. TEST ZA UPDATE SHIPMENT STATUS (Helper metoda) ---
    @Test
    @DisplayName("Coverage: Update statusa pojedinačne pošiljke")
    void updateShipmentStatus_Success() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));

        assignmentService.updateShipmentStatus(1L, ShipmentStatus.DELIVERED);

        assertEquals(ShipmentStatus.DELIVERED, testShipment.getStatus());
        verify(shipmentRepository).save(testShipment);
    }
}