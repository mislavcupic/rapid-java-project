package hr.algebra.rapid.logisticsandfleetmanagementsystem.integration;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AssignmentService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ✅ POPRAVLJEN INTEGRACIJSKI TEST - Assignment Creation Complete Workflow
 */
@SpringBootTest
@ActiveProfiles("test")
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

    @Autowired
    private EntityManager entityManager;  // ✅ DODANO

    private Driver testDriver;
    private Vehicle testVehicle;
    private Shipment testShipment;

    @BeforeEach
    void setUp() {  // ✅ BEZ @Transactional
        // Očisti bazu
        assignmentRepository.deleteAll();
        shipmentRepository.deleteAll();
        driverRepository.deleteAll();
        vehicleRepository.deleteAll();

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
        userInfo.setPassword("$2a$10$hashedPassword");
        userInfo.setFirstName("Integration");
        userInfo.setLastName("Test");
        userInfo.setEmail("integration@test.com");
        userInfo.setIsEnabled(true);

        List<UserRole> rolesList = new ArrayList<>();
        rolesList.add(driverRole);
        userInfo.setRoles(rolesList);

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
        testVehicle.setLoadCapacityKg(BigDecimal.valueOf(1000));
        testVehicle.setCurrentMileageKm(50000L);
        testVehicle.setNextServiceMileageKm(55000L);
        testVehicle.setLastServiceDate(java.time.LocalDate.now().minusMonths(2));
        testVehicle.setFuelConsumptionLitersPer100Km(BigDecimal.valueOf(8.5));
        testVehicle = vehicleRepository.save(testVehicle);

        // 5. Create and save Shipment
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
        testShipment = shipmentRepository.findById(shipmentResponse.getId()).orElseThrow(
                () -> new AssertionError("Test Shipment not found after creation")
        );

        // ✅ KRITIČNA IZMJENA: Promijeni status iz PENDING u SCHEDULED
        testShipment.setStatus(ShipmentStatus.SCHEDULED);
        shipmentRepository.save(testShipment);

        // ✅ Forsiraj flush u bazu i očisti cache
        entityManager.flush();
        entityManager.clear();

        // ✅ Ponovno učitaj testShipment da bude fresh iz baze
        testShipment = shipmentRepository.findById(testShipment.getId()).orElseThrow();
    }

    @Test
    @Transactional
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
        assertNotNull(result, "Assignment result should not be null");
        assertNotNull(result.getId(), "Assignment ID should be assigned");
        assertEquals("SCHEDULED", result.getAssignmentStatus(), "Initial status should be SCHEDULED");

        // Verify Driver
        assertNotNull(result.getDriver(), "Driver should not be null");
        assertEquals(testDriver.getId(), result.getDriver().getId(), "Driver ID should match");
        assertEquals("Integration Test", result.getDriver().getFullName(), "Driver full name should match");

        // Verify Vehicle
        assertNotNull(result.getVehicle(), "Vehicle should not be null");
        assertEquals(testVehicle.getId(), result.getVehicle().getId(), "Vehicle ID should match");
        assertEquals("ZG-TEST-001", result.getVehicle().getLicensePlate(), "License plate should match");

        // Verify Shipment
        assertNotNull(result.getShipment(), "Shipment should not be null");
        assertEquals(testShipment.getId(), result.getShipment().getId(), "Shipment ID should match");
        assertEquals("INT-SHIP-001", result.getShipment().getTrackingNumber(), "Tracking number should match");
        assertEquals(ShipmentStatus.SCHEDULED, result.getShipment().getStatus(), "Shipment status should remain SCHEDULED");

        // Verify Route was calculated
        assertNotNull(result.getShipment().getEstimatedDistanceKm(), "Estimated distance should not be null");
        assertTrue(result.getShipment().getEstimatedDistanceKm().doubleValue() > 0, "Distance should be positive");
    }

    @Test
    @Transactional
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
        Assignment persisted = assignmentRepository.findById(created.getId())
                .orElseThrow(() -> new AssertionError("Assignment not found in database"));

        assertNotNull(persisted, "Persisted assignment should not be null");
        assertEquals("SCHEDULED", persisted.getStatus(), "Status should be SCHEDULED");
        assertEquals(testDriver.getId(), persisted.getDriver().getId(), "Driver ID should match");
        assertEquals(testVehicle.getId(), persisted.getVehicle().getId(), "Vehicle ID should match");
        assertEquals(testShipment.getId(), persisted.getShipment().getId(), "Shipment ID should match");
    }

    @Test
    @Transactional
    void testShipmentStatusTransition() {
        // Assert - Initial status should be SCHEDULED (postavljeno u setUp)
        assertEquals(ShipmentStatus.SCHEDULED, testShipment.getStatus(), "Initial status should be SCHEDULED");

        // Arrange
        AssignmentRequestDTO request = new AssignmentRequestDTO();
        request.setDriverId(testDriver.getId());
        request.setVehicleId(testVehicle.getId());
        request.setShipmentId(testShipment.getId());
        request.setStartTime(LocalDateTime.now().plusHours(2));

        // Act
        assignmentService.createAssignment(request);

        // Assert - Status should remain SCHEDULED after assignment
        Shipment afterAssignment = shipmentRepository.findById(testShipment.getId()).orElseThrow(
                () -> new AssertionError("Shipment not found after assignment")
        );
        assertEquals(ShipmentStatus.SCHEDULED, afterAssignment.getStatus(), "Status should remain SCHEDULED");
    }

    @Test
    @Transactional
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
        assertNotNull(result.getEstimatedDistanceKm(), "Distance should be calculated");
        assertNotNull(result.getEstimatedDurationMinutes(), "Duration should be calculated");
        assertEquals("CALCULATED", result.getRouteStatus(), "Route status should be CALCULATED");

        // Distance Zagreb-Rijeka should be ~100-150 km
        assertTrue(result.getEstimatedDistanceKm().doubleValue() > 80, "Distance should be > 80km");
        assertTrue(result.getEstimatedDistanceKm().doubleValue() < 200, "Distance should be < 200km");
    }

    @Test
    @Transactional
    void testMultipleAssignmentsForDriver() {
        // Arrange - Create 3 simplified shipments
        Shipment ship1 = createSimpleShipment("MULTI-001");
        Shipment ship2 = createSimpleShipment("MULTI-002");
        Shipment ship3 = createSimpleShipment("MULTI-003");

        // Act - Create 3 assignments for same driver
        AssignmentRequestDTO assign1 = createAssignmentRequest(testDriver.getId(), testVehicle.getId(), ship1.getId(), 2);
        AssignmentRequestDTO assign2 = createAssignmentRequest(testDriver.getId(), testVehicle.getId(), ship2.getId(), 4);
        AssignmentRequestDTO assign3 = createAssignmentRequest(testDriver.getId(), testVehicle.getId(), ship3.getId(), 6);

        assignmentService.createAssignment(assign1);
        assignmentService.createAssignment(assign2);
        assignmentService.createAssignment(assign3);

        // Assert - Driver should have 3 assignments
        var driverAssignments = assignmentService.findAssignmentsByDriver(testDriver.getId());
        assertEquals(3, driverAssignments.size(), "Driver should have 3 assignments");
    }

    // Helper methods
    private Shipment createSimpleShipment(String trackingNumber) {
        Route route = new Route();
        route.setOriginAddress("Zagreb");
        route.setOriginLatitude(45.8150);
        route.setOriginLongitude(15.9819);
        route.setDestinationAddress("Split");
        route.setDestinationLatitude(43.5081);
        route.setDestinationLongitude(16.4402);
        route.setStatus(RouteStatus.CALCULATED);
        route.setEstimatedDistanceKm(300.0);
        route.setEstimatedDurationMinutes(180L);

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(trackingNumber);
        shipment.setDescription("Simple test shipment");
        shipment.setWeightKg(BigDecimal.valueOf(10.0));
        shipment.setVolumeM3(BigDecimal.valueOf(1.0));
        shipment.setOriginAddress("Zagreb");
        shipment.setDestinationAddress("Split");
        shipment.setStatus(ShipmentStatus.SCHEDULED);  // ✅ DIREKTNO SCHEDULED
        shipment.setExpectedDeliveryDate(LocalDateTime.now().plusDays(2));
        shipment.setRoute(route);

        return shipmentRepository.save(shipment);
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