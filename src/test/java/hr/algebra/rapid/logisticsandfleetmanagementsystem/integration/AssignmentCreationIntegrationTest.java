package hr.algebra.rapid.logisticsandfleetmanagementsystem.integration;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AssignmentService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * INTEGRACIJSKI TEST - Assignment Creation Complete Workflow
 * 
 * Testira kompletan workflow:
 * 1. Kreiranje Driver-a
 * 2. Kreiranje Vehicle-a
 * 3. Kreiranje Shipment-a (s Route-om)
 * 4. Kreiranje Assignment-a (spaja sve zajedno)
 * 5. Provjera status transitions
 * 
 * NAPOMENA: Ovaj test zahtijeva aktivnu bazu podataka i Spring Context
 * Dodaj @ActiveProfiles("test") ako imaš test profil
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AssignmentCreationIntegrationTest {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    private Driver testDriver;
    private Vehicle testVehicle;
    private Shipment testShipment;

    @BeforeEach
    void setUp() {
        // 1. Setup and save UserRole
        UserRole driverRole = userRoleRepository.findByName("ROLE_DRIVER")
                .orElseGet(() -> {
                    UserRole role = new UserRole();
                    role.setName("ROLE_DRIVER");
                    return userRoleRepository.save(role);
                });

        // 2. Create and save UserInfo
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername("integrationtest_driver");
        userInfo.setPassword("hashedPassword");
        userInfo.setFirstName("Integration");
        userInfo.setLastName("Test");
        userInfo.setEmail("integration@test.com");
        userInfo.setIsEnabled(true);
        userInfo.setRoles(java.util.Arrays.asList(driverRole));
        userInfo = userRepository.save(userInfo);

        // 3. Create and save Driver
        testDriver = new Driver();
        testDriver.setUserInfo(userInfo);
        testDriver.setLicenseNumber("INT-TEST-001");
        testDriver.setPhoneNumber("+385991234567");
        testDriver = driverRepository.save(testDriver);

        // 4. Create and save Vehicle
        testVehicle = new Vehicle();
        testVehicle.setLicensePlate("ZG-TEST-001");
        testVehicle.setMake("Mercedes");
        testVehicle.setModel("Sprinter");
        testVehicle.setYear(2022);
        testVehicle.setFuelType("Diesel");
        testVehicle.setLoadCapacityKg(java.math.BigDecimal.valueOf(1000));
        testVehicle.setCurrentMileageKm(50000L);
        testVehicle.setNextServiceMileageKm(55000L);
        testVehicle.setLastServiceDate(java.time.LocalDate.now().minusMonths(2));
        testVehicle.setFuelConsumptionLitersPer100Km(java.math.BigDecimal.valueOf(8.5));
        testVehicle = vehicleRepository.save(testVehicle);

        // 5. Create and save Shipment (via ShipmentService to trigger Route creation)
        ShipmentRequest shipmentRequest = new ShipmentRequest();
        shipmentRequest.setTrackingNumber("INT-SHIP-001");
        shipmentRequest.setDescription("Integration test shipment");
        shipmentRequest.setWeightKg(BigDecimal.valueOf(100.0));
        shipmentRequest.setVolumeM3(BigDecimal.valueOf(5.0));
        shipmentRequest.setOriginAddress("Zagreb, Croatia");
        shipmentRequest.setOriginLatitude(45.8150);
        shipmentRequest.setOriginLongitude(15.9819);
        shipmentRequest.setDestinationAddress("Split, Croatia");
        shipmentRequest.setDestinationLatitude(43.5081);
        shipmentRequest.setDestinationLongitude(16.4402);
        shipmentRequest.setExpectedDeliveryDate(LocalDateTime.now().plusDays(2));

        ShipmentResponse shipmentResponse = shipmentService.createShipment(shipmentRequest);
        testShipment = shipmentRepository.findById(shipmentResponse.getId()).orElseThrow();
    }

    @Test
    void testCompleteAssignmentCreationWorkflow() {
        // Arrange
        AssignmentRequestDTO request = new AssignmentRequestDTO();
        request.setDriverId(testDriver.getId());
        request.setVehicleId(testVehicle.getId());
        request.setShipmentId(testShipment.getId());
        request.setStartTime(LocalDateTime.now().plusHours(2));

        // Act
        AssignmentResponseDTO result = assignmentService.createAssignment(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("SCHEDULED", result.getAssignmentStatus());

        // Verify Driver
        assertNotNull(result.getDriver());
        assertEquals(testDriver.getId(), result.getDriver().getId());
        assertEquals("Integration Test", result.getDriver().getFullName());

        // Verify Vehicle
        assertNotNull(result.getVehicle());
        assertEquals(testVehicle.getId(), result.getVehicle().getId());
        assertEquals("ZG-TEST-001", result.getVehicle().getLicensePlate());

        // Verify Shipment
        assertNotNull(result.getShipment());
        assertEquals(testShipment.getId(), result.getShipment().getId());
        assertEquals("INT-SHIP-001", result.getShipment().getTrackingNumber());
        assertEquals(ShipmentStatus.SCHEDULED, result.getShipment().getStatus());

        // Verify Route was calculated
        assertNotNull(result.getShipment().getEstimatedDistanceKm());
        assertTrue(result.getShipment().getEstimatedDistanceKm() > 0);
    }

    @Test
    void testAssignmentPersistence() {
        // Arrange
        AssignmentRequestDTO request = new AssignmentRequestDTO();
        request.setDriverId(testDriver.getId());
        request.setVehicleId(testVehicle.getId());
        request.setShipmentId(testShipment.getId());
        request.setStartTime(LocalDateTime.now().plusHours(2));

        // Act
        AssignmentResponseDTO created = assignmentService.createAssignment(request);

        // Assert - Read from database
        Assignment persisted = assignmentRepository.findById(created.getId()).orElseThrow();
        assertNotNull(persisted);
        assertEquals("SCHEDULED", persisted.getStatus());
        assertEquals(testDriver.getId(), persisted.getDriver().getId());
        assertEquals(testVehicle.getId(), persisted.getVehicle().getId());
        assertEquals(testShipment.getId(), persisted.getShipment().getId());
    }

    @Test
    void testShipmentStatusTransition() {
        // Arrange
        AssignmentRequestDTO request = new AssignmentRequestDTO();
        request.setDriverId(testDriver.getId());
        request.setVehicleId(testVehicle.getId());
        request.setShipmentId(testShipment.getId());
        request.setStartTime(LocalDateTime.now().plusHours(2));

        // Assert - Initial status
        Shipment beforeAssignment = shipmentRepository.findById(testShipment.getId()).orElseThrow();
        assertEquals(ShipmentStatus.PENDING, beforeAssignment.getStatus());

        // Act
        assignmentService.createAssignment(request);

        // Assert - Status after assignment
        Shipment afterAssignment = shipmentRepository.findById(testShipment.getId()).orElseThrow();
        assertEquals(ShipmentStatus.SCHEDULED, afterAssignment.getStatus());
    }

    @Test
    void testRouteCalculation() {
        // Arrange
        ShipmentRequest shipmentRequest = new ShipmentRequest();
        shipmentRequest.setTrackingNumber("ROUTE-TEST-001");
        shipmentRequest.setDescription("Route calculation test");
        shipmentRequest.setWeightKg(BigDecimal.valueOf(50.0));
        shipmentRequest.setVolumeM3(BigDecimal.valueOf(2.0));
        shipmentRequest.setOriginAddress("Zagreb, Croatia");
        shipmentRequest.setOriginLatitude(45.8150);
        shipmentRequest.setOriginLongitude(15.9819);
        shipmentRequest.setDestinationAddress("Rijeka, Croatia");
        shipmentRequest.setDestinationLatitude(45.3271);
        shipmentRequest.setDestinationLongitude(14.4422);
        shipmentRequest.setExpectedDeliveryDate(LocalDateTime.now().plusDays(1));

        // Act
        ShipmentResponse result = shipmentService.createShipment(shipmentRequest);

        // Assert
        assertNotNull(result.getEstimatedDistanceKm());
        assertNotNull(result.getEstimatedDurationMinutes());
        assertEquals("CALCULATED", result.getRouteStatus());

        // Distance Zagreb-Rijeka should be ~100-150 km
        assertTrue(result.getEstimatedDistanceKm() > 80);
        assertTrue(result.getEstimatedDistanceKm() < 200);
    }

    @Test
    void testMultipleAssignmentsForDriver() {
        // Arrange - Create 3 shipments
        ShipmentRequest req1 = createShipmentRequest("MULTI-001", "Zagreb", "Split");
        ShipmentRequest req2 = createShipmentRequest("MULTI-002", "Split", "Rijeka");
        ShipmentRequest req3 = createShipmentRequest("MULTI-003", "Rijeka", "Pula");

        ShipmentResponse ship1 = shipmentService.createShipment(req1);
        ShipmentResponse ship2 = shipmentService.createShipment(req2);
        ShipmentResponse ship3 = shipmentService.createShipment(req3);

        // Act - Create 3 assignments for same driver
        AssignmentRequestDTO assign1 = createAssignmentRequest(testDriver.getId(), testVehicle.getId(), ship1.getId(), 2);
        AssignmentRequestDTO assign2 = createAssignmentRequest(testDriver.getId(), testVehicle.getId(), ship2.getId(), 4);
        AssignmentRequestDTO assign3 = createAssignmentRequest(testDriver.getId(), testVehicle.getId(), ship3.getId(), 6);

        assignmentService.createAssignment(assign1);
        assignmentService.createAssignment(assign2);
        assignmentService.createAssignment(assign3);

        // Assert - Driver should have 3 assignments
        var driverAssignments = assignmentService.findAssignmentsByDriver(testDriver.getId());
        assertEquals(3, driverAssignments.size());
    }

    // Helper methods
    private ShipmentRequest createShipmentRequest(String trackingNumber, String origin, String destination) {
        ShipmentRequest request = new ShipmentRequest();
        request.setTrackingNumber(trackingNumber);
        request.setDescription("Test shipment");
        request.setWeightKg(BigDecimal.valueOf(100.0));
        request.setVolumeM3(BigDecimal.valueOf(5.0));
        request.setOriginAddress(origin + ", Croatia");
        request.setOriginLatitude(45.8150);
        request.setOriginLongitude(15.9819);
        request.setDestinationAddress(destination + ", Croatia");
        request.setDestinationLatitude(43.5081);
        request.setDestinationLongitude(16.4402);
        request.setExpectedDeliveryDate(LocalDateTime.now().plusDays(2));
        return request;
    }

    private AssignmentRequestDTO createAssignmentRequest(Long driverId, Long vehicleId, Long shipmentId, int hoursOffset) {
        AssignmentRequestDTO request = new AssignmentRequestDTO();
        request.setDriverId(driverId);
        request.setVehicleId(vehicleId);
        request.setShipmentId(shipmentId);
        request.setStartTime(LocalDateTime.now().plusHours(hoursOffset));
        return request;
    }
}
