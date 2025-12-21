package hr.algebra.rapid.logisticsandfleetmanagementsystem.integration;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ConflictException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AssignmentService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class AssignmentCreationIntegrationTest {

    @Autowired private AssignmentService assignmentService;
    @Autowired private ShipmentRepository shipmentRepository;
    @Autowired private DriverRepository driverRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private AssignmentRepository assignmentRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EntityManager entityManager;

    private Driver testDriver;
    private Vehicle testVehicle;
    private Shipment testShipment;

    @BeforeEach
    void setUp() {
        assignmentRepository.deleteAllInBatch();
        shipmentRepository.deleteAllInBatch();
        driverRepository.deleteAllInBatch();
        vehicleRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        userRoleRepository.deleteAllInBatch();

        UserRole driverRole = new UserRole();
        driverRole.setName("ROLE_DRIVER");
        driverRole = userRoleRepository.saveAndFlush(driverRole);

        UserInfo userInfo = new UserInfo();
        userInfo.setUsername("driver_" + System.currentTimeMillis());
        userInfo.setPassword("password");
        userInfo.setEmail("test" + System.currentTimeMillis() + "@rapid.hr");
        userInfo.setFirstName("Test");
        userInfo.setLastName("Driver");
        userInfo.setIsEnabled(true);
        userInfo.setRoles(List.of(driverRole));
        userInfo = userRepository.saveAndFlush(userInfo);

        testDriver = new Driver();
        testDriver.setUserInfo(userInfo);
        testDriver.setLicenseNumber("XYZ-123");
        testDriver = driverRepository.saveAndFlush(testDriver);

        testVehicle = new Vehicle();
        testVehicle.setLicensePlate("ZG-0000-AA");
        testVehicle.setMake("Mercedes");
        testVehicle.setModel("Sprinter");
        testVehicle.setLoadCapacityKg(BigDecimal.valueOf(1500));
        testVehicle = vehicleRepository.saveAndFlush(testVehicle);

        testShipment = createSimpleShipment("SHIP-123");

        entityManager.flush();
        entityManager.clear();

        testDriver = driverRepository.findById(testDriver.getId()).orElseThrow();
        testVehicle = vehicleRepository.findById(testVehicle.getId()).orElseThrow();
        testShipment = shipmentRepository.findById(testShipment.getId()).orElseThrow();
    }

    @Test
    void testAssignmentPersistence() {
        AssignmentRequestDTO request = createAssignmentRequest(testDriver.getId(), testVehicle.getId(), testShipment.getId(), 1);
        AssignmentResponseDTO response = assignmentService.createAssignment(request);

        assertNotNull(response.getId());
        assertEquals(testDriver.getId(), response.getDriver().getId());
    }

    @Test
    void testMultipleAssignmentsForDriver() {
        Shipment secondShipment = createSimpleShipment("SHIP-456");
        assignmentService.createAssignment(createAssignmentRequest(testDriver.getId(), testVehicle.getId(), testShipment.getId(), 1));
        assignmentService.createAssignment(createAssignmentRequest(testDriver.getId(), testVehicle.getId(), secondShipment.getId(), 5));

        List<AssignmentResponseDTO> assignments = assignmentService.findAssignmentsByDriver(testDriver.getId());
        assertEquals(2, assignments.size());
    }

    @Test
    void testFindAssignmentsByDriver() {
        assignmentService.createAssignment(createAssignmentRequest(testDriver.getId(), testVehicle.getId(), testShipment.getId(), 1));
        List<AssignmentResponseDTO> result = assignmentService.findAssignmentsByDriver(testDriver.getId());
        assertFalse(result.isEmpty());
        assertEquals(testDriver.getId(), result.get(0).getDriver().getId());
    }

    @Test
    void testCreateAssignmentThrowsConflictWhenShipmentAlreadyAssigned() {
        assignmentService.createAssignment(createAssignmentRequest(testDriver.getId(), testVehicle.getId(), testShipment.getId(), 1));

        AssignmentRequestDTO duplicateRequest = createAssignmentRequest(testDriver.getId(), testVehicle.getId(), testShipment.getId(), 2);
        assertThrows(ConflictException.class, () -> assignmentService.createAssignment(duplicateRequest));
    }

    @Test
    void testCreateAssignmentThrowsResourceNotFoundForInvalidDriver() {
        AssignmentRequestDTO request = createAssignmentRequest(999L, testVehicle.getId(), testShipment.getId(), 1);
        assertThrows(ResourceNotFoundException.class, () -> assignmentService.createAssignment(request));
    }

    @Test
    void testDeleteAssignmentFlow() {
        AssignmentResponseDTO response = assignmentService.createAssignment(createAssignmentRequest(testDriver.getId(), testVehicle.getId(), testShipment.getId(), 1));
        Long assignmentId = response.getId();

        assignmentService.deleteAssignment(assignmentId);
        assertFalse(assignmentRepository.existsById(assignmentId));
    }

    @Test
    void testStartAssignmentWorkflow() {
        AssignmentResponseDTO response = assignmentService.createAssignment(createAssignmentRequest(testDriver.getId(), testVehicle.getId(), testShipment.getId(), 1));

        // POPRAVAK: startAssignment sada vraća Optional
        Optional<AssignmentResponseDTO> result = assignmentService.startAssignment(response.getId(), testDriver.getId());

        assertTrue(result.isPresent());
        assertEquals("IN_PROGRESS", result.get().getAssignmentStatus());

        Shipment updatedShipment = shipmentRepository.findById(testShipment.getId()).orElseThrow();
        assertEquals(ShipmentStatus.IN_TRANSIT, updatedShipment.getStatus());
    }

    @Test
    void testUpdateAssignmentReturnsEmptyForNonExistentId() {
        // POPRAVAK: Prema novoj logici, update vraća Optional.empty() umjesto exceptiona za glavni ID
        AssignmentRequestDTO request = createAssignmentRequest(testDriver.getId(), testVehicle.getId(), testShipment.getId(), 2);
        Optional<AssignmentResponseDTO> result = assignmentService.updateAssignment(999L, request);

        assertTrue(result.isEmpty());
    }

    @Test
    void testStartAssignmentThrowsConflictForWrongDriver() {
        AssignmentResponseDTO response = assignmentService.createAssignment(createAssignmentRequest(testDriver.getId(), testVehicle.getId(), testShipment.getId(), 1));

        // ConflictException se i dalje baca unutar servisa ako vozač nije vlasnik
        assertThrows(ConflictException.class, () -> assignmentService.startAssignment(response.getId(), 888L));
    }

    private Shipment createSimpleShipment(String trackingNumber) {
        Route route = new Route();
        route.setOriginAddress("Zagreb");
        route.setDestinationAddress("Split");
        route.setStatus(RouteStatus.CALCULATED);
        route.setEstimatedDistanceKm(400.0);
        route.setEstimatedDurationMinutes(300L);
        entityManager.persist(route);

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(trackingNumber);
        shipment.setOriginAddress("Zagreb");
        shipment.setDestinationAddress("Split");
        shipment.setStatus(ShipmentStatus.SCHEDULED);
        shipment.setWeightKg(BigDecimal.valueOf(10));
        shipment.setVolumeM3(BigDecimal.valueOf(1));
        shipment.setRoute(route);
        return shipmentRepository.saveAndFlush(shipment);
    }

    private AssignmentRequestDTO createAssignmentRequest(Long driverId, Long vehicleId, Long shipmentId, int hoursOffset) {
        AssignmentRequestDTO request = new AssignmentRequestDTO();
        request.setDriverId(driverId);
        request.setVehicleId(vehicleId);
        request.setShipmentId(shipmentId);
        request.setStartTime(LocalDateTime.now().plusHours(hoursOffset));
        request.setEndTime(LocalDateTime.now().plusHours(hoursOffset + 4));
        return request;
    }
}