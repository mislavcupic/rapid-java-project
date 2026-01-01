package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ConflictException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.VehicleService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceImplTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private RouteRepository routeRepository;
    @Mock private EntityManager entityManager;
    @Mock private VehicleService vehicleService;
    @Mock private ShipmentService shipmentService;

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    private Driver testDriver;
    private Vehicle testVehicle;
    private Shipment testShipment;
    private Assignment testAssignment;
    private Route testRoute;

    @BeforeEach
    void setUp() {
        assignmentService.setSelf(assignmentService);
        ReflectionTestUtils.setField(assignmentService, "entityManager", entityManager);

        testDriver = new Driver(); testDriver.setId(1L);
        testVehicle = new Vehicle(); testVehicle.setId(1L);

        testShipment = new Shipment();
        testShipment.setId(1L);
        testShipment.setOriginAddress("Zagreb");
        testShipment.setDestinationAddress("Split");
        testShipment.setOriginLatitude(45.815); testShipment.setOriginLongitude(15.981);
        testShipment.setDestinationLatitude(43.508); testShipment.setDestinationLongitude(16.440);
        testShipment.setStatus(ShipmentStatus.PENDING);

        testRoute = new Route();
        testRoute.setId(1L);
        testRoute.setOriginAddress("Zagreb");
        testRoute.setDestinationAddress("Split");
        testRoute.setStatus(RouteStatus.DRAFT);

        testAssignment = new Assignment();
        testAssignment.setId(1L);
        testAssignment.setDriver(testDriver);
        testAssignment.setVehicle(testVehicle);
        testAssignment.setRoute(testRoute);
        testAssignment.setShipments(new ArrayList<>(List.of(testShipment)));
        testAssignment.setStatus("SCHEDULED");
    }

    // --- 1. KREIRANJE (SUCCESS) ---
    @Test
    void createAssignment_Success() {
        AssignmentRequestDTO req = new AssignmentRequestDTO();
        req.setDriverId(1L); req.setVehicleId(1L); req.setShipmentIds(List.of(1L));

        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(shipmentRepository.findAllById(any())).thenReturn(List.of(testShipment));
        when(routeRepository.save(any())).thenReturn(testRoute);
        when(assignmentRepository.save(any())).thenReturn(testAssignment);
        when(shipmentRepository.findByAssignmentId(any())).thenReturn(List.of(testShipment));
        when(assignmentRepository.findById(any())).thenReturn(Optional.of(testAssignment));

        assertNotNull(assignmentService.createAssignment(req));
        verify(entityManager, atLeastOnce()).clear();
    }

    // --- 2. FIND ALL ---
    @Test
    void findAll_Success() {
        when(assignmentRepository.findAll()).thenReturn(List.of(testAssignment));
        assertEquals(1, assignmentService.findAll().size());
    }

    // --- 3. FIND BY ID ---
    @Test
    void findById_Success() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        assertTrue(assignmentService.findById(1L).isPresent());
    }

    // --- 4. VEHICLE NOT FOUND ---
    @Test
    void createAssignment_VehicleNotFound() {
        AssignmentRequestDTO req = new AssignmentRequestDTO();
        req.setDriverId(1L); req.setVehicleId(99L);
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> assignmentService.createAssignment(req));
    }

    // --- 5. NO SHIPMENTS CONFLICT ---
    @Test
    void createAssignment_NoShipments() {
        AssignmentRequestDTO req = new AssignmentRequestDTO();
        req.setDriverId(1L); req.setVehicleId(1L); req.setShipmentIds(List.of());
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(shipmentRepository.findAllById(any())).thenReturn(Collections.emptyList());
        assertThrows(ConflictException.class, () -> assignmentService.createAssignment(req));
    }

    // --- 6. UPDATE ASSIGNMENT ---
    @Test
    void updateAssignment_Success() {
        AssignmentRequestDTO req = new AssignmentRequestDTO();
        req.setDriverId(1L); req.setVehicleId(1L); req.setShipmentIds(List.of(1L));

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(shipmentRepository.findByAssignmentId(1L)).thenReturn(List.of(testShipment));
        when(shipmentRepository.findAllById(any())).thenReturn(List.of(testShipment));
        when(assignmentRepository.saveAndFlush(any())).thenReturn(testAssignment);

        assignmentService.updateAssignment(1L, req);
        verify(entityManager, atLeastOnce()).clear();
    }

    // --- 7. DELETE ---
    @Test
    void deleteAssignment_Success() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        assignmentService.deleteAssignment(1L);
        verify(assignmentRepository).delete(any());
        verify(assignmentRepository).flush();
    }

    // --- 8. SHIPMENT STATUS UPDATE ---
    @Test
    void updateShipmentStatus_Success() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        assignmentService.updateShipmentStatus(1L, ShipmentStatus.DELIVERED);
        verify(shipmentRepository).save(any());
    }

    // --- 9. NALOG STATUS UPDATE ---
    @Test
    void updateStatus_Success() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.save(any())).thenReturn(testAssignment);
        assignmentService.updateStatus(1L, "COMPLETED");
        assertEquals(ShipmentStatus.DELIVERED, testShipment.getStatus());
    }

    // --- 10. START ASSIGNMENT ---
    @Test
    void startAssignment_Success() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.saveAndFlush(any())).thenReturn(testAssignment);
        assignmentService.startAssignment(1L, 1L);
        assertEquals("IN_PROGRESS", testAssignment.getStatus());
    }

    // --- 11. START WRONG DRIVER ---
    @Test
    void startAssignment_WrongDriver() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        assertThrows(ConflictException.class, () -> assignmentService.startAssignment(1L, 99L));
    }

    // --- 12. COMPLETE SUCCESS ---
    @Test
    void completeAssignment_Success() {
        testAssignment.setStatus("IN_PROGRESS");
        testShipment.setStatus(ShipmentStatus.DELIVERED);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.save(any())).thenReturn(testAssignment);
        assignmentService.completeAssignment(1L, 1L);
        assertEquals("COMPLETED", testAssignment.getStatus());
    }

    // --- 13. COMPLETE NOT DELIVERED ---
    @Test
    void completeAssignment_NotDelivered() {
        testAssignment.setStatus("IN_PROGRESS");
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        assertThrows(ConflictException.class, () -> assignmentService.completeAssignment(1L, 1L));
    }

    // --- 14. COMPLETE WRONG STATUS ---
    @Test
    void completeAssignment_WrongStatus() {
        testAssignment.setStatus("SCHEDULED");
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        assertThrows(ConflictException.class, () -> assignmentService.completeAssignment(1L, 1L));
    }

    // --- 15. OPTIMIZE HAVERSINE ---
    @Test
    void optimizeAssignmentOrder_Haversine() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(shipmentRepository.findByAssignmentId(1L)).thenReturn(List.of(testShipment));

        assertNotNull(assignmentService.optimizeAssignmentOrder(1L));
        verify(entityManager, atLeastOnce()).clear();
    }

    // --- 16. TSP OPTIMIZACIJA ---
    @Test
    void optimizeWithMultipleShipments() {
        Shipment s2 = new Shipment(); s2.setId(2L);
        s2.setOriginLatitude(46.0); s2.setOriginLongitude(16.0);
        s2.setDestinationLatitude(47.0); s2.setDestinationLongitude(17.0);

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(shipmentRepository.findByAssignmentId(1L)).thenReturn(List.of(testShipment, s2));

        assignmentService.optimizeAssignmentOrder(1L);
        verify(entityManager, atLeastOnce()).clear();
    }

    // --- 17. FIND BY DRIVER ---
    @Test
    void findAssignmentsByDriver_Success() {
        when(assignmentRepository.findByDriverIdAndStatusIn(anyLong(), any())).thenReturn(List.of(testAssignment));
        assertFalse(assignmentService.findAssignmentsByDriver(1L).isEmpty());
    }

    // --- 18. PERMUTATIONS COVERAGE ---
    @Test
    void generatePermutations_Coverage() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(shipmentRepository.findByAssignmentId(1L)).thenReturn(List.of(testShipment));

        assignmentService.optimizeAssignmentOrder(1L);
        verify(entityManager, atLeastOnce()).clear();
    }



    // --- 21. UPDATE STATUS - NOT COMPLETED (OTHER STATUS) ---
    @Test
    void updateStatus_OtherStatus() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.save(any())).thenReturn(testAssignment);

        assignmentService.updateStatus(1L, "IN_PROGRESS");

        // Provjera da se statusi pošiljaka NISU promijenili u DELIVERED
        assertNotEquals(ShipmentStatus.DELIVERED, testShipment.getStatus());
        verify(assignmentRepository).save(testAssignment);
    }

    // --- 22. OPTIMIZE - ASSIGNMENT NOT FOUND ---
    @Test
    void optimizeAssignmentOrder_NotFound() {
        when(assignmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> assignmentService.optimizeAssignmentOrder(99L));
    }

    // --- 23. OPTIMIZE - EMPTY SHIPMENTS LIST ---
    @Test
    void optimizeAssignmentOrder_EmptyShipments() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(shipmentRepository.findByAssignmentId(1L)).thenReturn(Collections.emptyList());

        assertNotNull(assignmentService.optimizeAssignmentOrder(1L));
        verify(shipmentRepository, never()).saveAll(any()); // Ne smije spremati ništa ako je lista prazna
    }



    // --- 25. FIND BY DRIVER - NO RESULTS ---
    @Test
    void findAssignmentsByDriver_Empty() {
        when(assignmentRepository.findByDriverIdAndStatusIn(anyLong(), any())).thenReturn(Collections.emptyList());

        List<AssignmentResponseDTO> result = assignmentService.findAssignmentsByDriver(1L);

        assertTrue(result.isEmpty());
    }
}