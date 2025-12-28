package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AssignmentServiceImplTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private ShipmentRepository shipmentRepository;
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

        testAssignment = new Assignment();
        testAssignment.setId(1L);
        testAssignment.setDriver(testDriver);
        testAssignment.setVehicle(testVehicle);
        testAssignment.setShipments((List<Shipment>) testShipment);
        testAssignment.setStatus("SCHEDULED");

        testRequest = new AssignmentRequestDTO();
        testRequest.setDriverId(1L);
        testRequest.setVehicleId(1L);
        testRequest.setShipmentIds(Collections.singletonList(1L));
        testRequest.setStartTime(LocalDateTime.now().plusHours(2));

        // Default stubbing za mapiranje (koristi se u skoro svim testovima)
        when(vehicleService.mapToResponse(any())).thenReturn(new VehicleResponse());
        when(shipmentService.mapToResponse(any())).thenReturn(new ShipmentResponse());
    }

    @Test
    void findAll_Success() {
        when(assignmentRepository.findAll()).thenReturn(List.of(testAssignment));

        List<AssignmentResponseDTO> result = assignmentService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(assignmentRepository).findAll();
    }

    @Test
    void findById_Success() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));

        Optional<AssignmentResponseDTO> result = assignmentService.findById(1L);

        assertTrue(result.isPresent());
        verify(assignmentRepository).findById(1L);
    }

    @Test
    void createAssignment_Success() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        when(assignmentRepository.findByShipments_Id(1L)).thenReturn(Optional.empty());
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);

        AssignmentResponseDTO result = assignmentService.createAssignment(testRequest);

        assertNotNull(result);
        verify(assignmentRepository).save(any());
        // Provjera da je servis promijenio status pošiljke
        assertEquals(ShipmentStatus.SCHEDULED, testShipment.getStatus());
    }

    @Test
    void updateAssignment_Success() {
        // Mockamo sve što getDriver, getVehicle i getShipment u servisu traže
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        when(assignmentRepository.save(any())).thenReturn(testAssignment);

        Optional<AssignmentResponseDTO> result = assignmentService.updateAssignment(1L, testRequest);

        assertTrue(result.isPresent());
        verify(assignmentRepository).save(any());
    }

    @Test
    void deleteAssignment_Success() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));

        assignmentService.deleteAssignment(1L);

        // Provjera da se status vratio na PENDING nakon brisanja naloga
        assertEquals(ShipmentStatus.PENDING, testShipment.getStatus());
        verify(assignmentRepository).delete(testAssignment);
    }

    @Test
    void startAssignment_Success() {
        testAssignment.setStatus("SCHEDULED");
        testShipment.setStatus(ShipmentStatus.SCHEDULED); // Mora biti scheduled da bi krenuo

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.save(any())).thenReturn(testAssignment);

        Optional<AssignmentResponseDTO> result = assignmentService.startAssignment(1L, 1L);

        assertTrue(result.isPresent());
        assertEquals("IN_PROGRESS", testAssignment.getStatus());
        // Ključna ispravka: gledamo status shipmenta koji je zakačen na assignment
        assertEquals(ShipmentStatus.IN_TRANSIT, testAssignment.getShipments().get(0).getStatus());
    }

    @Test
    void completeAssignment_Success() {
        testAssignment.setStatus("IN_PROGRESS");
        // Za uspješan završetak, pošiljka MORA biti DELIVERED (ako ti je takva logika u servisu)
        testShipment.setStatus(ShipmentStatus.DELIVERED);

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.save(any())).thenReturn(testAssignment);

        Optional<AssignmentResponseDTO> result = assignmentService.completeAssignment(1L, 1L);

        assertTrue(result.isPresent());
        assertEquals("COMPLETED", testAssignment.getStatus());
        assertNotNull(testAssignment.getEndTime());
    }

    @Test
    void findAssignmentsByDriver_Success() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(assignmentRepository.findByDriverIdAndStatusIn(eq(1L), any()))
                .thenReturn(List.of(testAssignment));

        List<AssignmentResponseDTO> result = assignmentService.findAssignmentsByDriver(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(assignmentRepository).findByDriverIdAndStatusIn(anyLong(), any());
    }
}