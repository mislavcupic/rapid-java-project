package hr.algebra.rapid.logisticsandfleetmanagementsystem.integration;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.*;
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
 * INTEGRACIJSKI TEST - Driver Delivery Complete Workflow
 * 
 * Testira KOMPLETAN lifecycle pošiljke:
 * 1. Assignment Creation (PENDING → SCHEDULED)
 * 2. Start Delivery (SCHEDULED → IN_TRANSIT)
 * 3. Complete Delivery (IN_TRANSIT → DELIVERED)
 * 4. Complete Assignment (IN_PROGRESS → COMPLETED)
 * 
 * Također testira alternativni scenarij:
 * - Report Issue (IN_TRANSIT → DELAYED)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DriverDeliveryWorkflowIntegrationTest {

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
    private Long assignmentId;
    private Long shipmentId;

    @BeforeEach
    void setUp() {
        // Setup Driver
        UserRole driverRole = userRoleRepository.findByName("ROLE_DRIVER")
                .orElseGet(() -> {
                    UserRole role = new UserRole();
                    role.setName("ROLE_DRIVER");
                    return userRoleRepository.save(role);
                });

        UserInfo userInfo = new UserInfo();
        userInfo.setUsername("workflow_driver");
        userInfo.setPassword("hashedPassword");
        userInfo.setFirstName("Workflow");
        userInfo.setLastName("Driver");
        userInfo.setEmail("workflow@test.com");
        userInfo.setIsEnabled(true);
        userInfo.setRoles(java.util.Arrays.asList(driverRole));
        userInfo = userRepository.save(userInfo);

        testDriver = new Driver();
        testDriver.setUserInfo(userInfo);
        testDriver.setLicenseNumber("WORKFLOW-001");
        testDriver.setPhoneNumber("+385991234567");
        testDriver = driverRepository.save(testDriver);

        // Setup Vehicle
        testVehicle = new Vehicle();
        testVehicle.setLicensePlate("ZG-WORK-001");
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

        // Setup Shipment
        ShipmentRequest shipmentRequest = new ShipmentRequest();
        shipmentRequest.setTrackingNumber("WORKFLOW-SHIP-001");
        shipmentRequest.setDescription("Workflow test shipment");
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
        shipmentId = shipmentResponse.getId();

        // Create Assignment
        AssignmentRequestDTO assignmentRequest = new AssignmentRequestDTO();
        assignmentRequest.setDriverId(testDriver.getId());
        assignmentRequest.setVehicleId(testVehicle.getId());
        assignmentRequest.setShipmentId(shipmentId);
        assignmentRequest.setStartTime(LocalDateTime.now().plusHours(1));

        AssignmentResponseDTO assignmentResponse = assignmentService.createAssignment(assignmentRequest);
        assignmentId = assignmentResponse.getId();
    }

    // ==========================================
    // HAPPY PATH - COMPLETE DELIVERY WORKFLOW
    // ==========================================

    @Test
    void testCompleteDeliveryWorkflow_HappyPath() {
        // Step 1: Verify initial state
        Shipment initialShipment = shipmentRepository.findById(shipmentId).orElseThrow();
        assertEquals(ShipmentStatus.SCHEDULED, initialShipment.getStatus());

        Assignment initialAssignment = assignmentRepository.findById(assignmentId).orElseThrow();
        assertEquals("SCHEDULED", initialAssignment.getStatus());

        // Step 2: Driver starts delivery
        AssignmentResponseDTO startedAssignment = assignmentService.startAssignment(assignmentId, testDriver.getId());
        assertEquals("IN_PROGRESS", startedAssignment.getAssignmentStatus());

        ShipmentResponse startedShipment = shipmentService.findById(shipmentId).orElseThrow();
        assertEquals(ShipmentStatus.IN_TRANSIT, startedShipment.getStatus());

        // Step 3: Driver completes delivery
        ProofOfDeliveryDTO pod = new ProofOfDeliveryDTO();
        pod.setRecipientName("John Doe");
        pod.setNotes("Delivered successfully");
        pod.setLatitude(43.5081);
        pod.setLongitude(16.4402);

        ShipmentResponse completedShipment = shipmentService.completeDelivery(shipmentId, testDriver.getId(), pod);
        assertEquals(ShipmentStatus.DELIVERED, completedShipment.getStatus());

        // Step 4: Driver completes assignment
        AssignmentResponseDTO completedAssignment = assignmentService.completeAssignment(assignmentId, testDriver.getId());
        assertEquals("COMPLETED", completedAssignment.getAssignmentStatus());
        assertNotNull(completedAssignment.getEndTime());

        // Final verification
        Shipment finalShipment = shipmentRepository.findById(shipmentId).orElseThrow();
        assertEquals(ShipmentStatus.DELIVERED, finalShipment.getStatus());

        Assignment finalAssignment = assignmentRepository.findById(assignmentId).orElseThrow();
        assertEquals("COMPLETED", finalAssignment.getStatus());
    }

    // ==========================================
    // ISSUE REPORTING WORKFLOW
    // ==========================================

    @Test
    void testIssueReportingWorkflow() {
        // Step 1: Start delivery
        assignmentService.startAssignment(assignmentId, testDriver.getId());

        // Step 2: Report issue
        IssueReportDTO issue = new IssueReportDTO();
        issue.setIssueType("Vehicle Issue");
        issue.setDescription("Engine problem");
        issue.setEstimatedDelay("2 hours");
        issue.setLatitude(45.5);
        issue.setLongitude(16.0);

        ShipmentResponse delayedShipment = shipmentService.reportIssue(shipmentId, testDriver.getId(), issue);
        assertEquals(ShipmentStatus.DELAYED, delayedShipment.getStatus());

        // Verify persistence
        Shipment persistedShipment = shipmentRepository.findById(shipmentId).orElseThrow();
        assertEquals(ShipmentStatus.DELAYED, persistedShipment.getStatus());
    }

    // ==========================================
    // DRIVER DASHBOARD TESTS
    // ==========================================

    @Test
    void testDriverDashboard_ScheduledAssignments() {
        // Create additional assignments
        ShipmentRequest req2 = createShipmentRequest("DASHBOARD-002");
        ShipmentResponse ship2 = shipmentService.createShipment(req2);

        AssignmentRequestDTO assign2 = new AssignmentRequestDTO();
        assign2.setDriverId(testDriver.getId());
        assign2.setVehicleId(testVehicle.getId());
        assign2.setShipmentId(ship2.getId());
        assign2.setStartTime(LocalDateTime.now().plusHours(3));
        assignmentService.createAssignment(assign2);

        // Act
        var driverAssignments = assignmentService.findAssignmentsByDriver(testDriver.getId());

        // Assert
        assertEquals(2, driverAssignments.size());
        assertTrue(driverAssignments.stream().allMatch(a -> 
            a.getAssignmentStatus().equals("SCHEDULED") || 
            a.getAssignmentStatus().equals("IN_PROGRESS")
        ));
    }

    @Test
    void testDriverDashboard_InProgressAssignments() {
        // Start the assignment
        assignmentService.startAssignment(assignmentId, testDriver.getId());

        // Act
        var driverAssignments = assignmentService.findAssignmentsByDriver(testDriver.getId());

        // Assert
        assertEquals(1, driverAssignments.size());
        assertEquals("IN_PROGRESS", driverAssignments.get(0).getAssignmentStatus());
    }

    @Test
    void testDriverDashboard_CompletedNotShown() {
        // Complete the entire workflow
        assignmentService.startAssignment(assignmentId, testDriver.getId());

        ProofOfDeliveryDTO pod = new ProofOfDeliveryDTO();
        pod.setRecipientName("Test Recipient");
        shipmentService.completeDelivery(shipmentId, testDriver.getId(), pod);

        assignmentService.completeAssignment(assignmentId, testDriver.getId());

        // Act
        var driverAssignments = assignmentService.findAssignmentsByDriver(testDriver.getId());

        // Assert - Completed assignments should not appear
        assertEquals(0, driverAssignments.size());
    }

    // ==========================================
    // ERROR SCENARIOS
    // ==========================================

    @Test
    void testCannotCompleteDeliveryWithoutStarting() {
        // Try to complete delivery without starting
        ProofOfDeliveryDTO pod = new ProofOfDeliveryDTO();
        pod.setRecipientName("John Doe");

        // Assert - Should throw exception
        assertThrows(Exception.class, () -> {
            shipmentService.completeDelivery(shipmentId, testDriver.getId(), pod);
        });
    }

    @Test
    void testCannotCompleteAssignmentBeforeDelivery() {
        // Start assignment
        assignmentService.startAssignment(assignmentId, testDriver.getId());

        // Try to complete assignment without completing delivery
        assertThrows(Exception.class, () -> {
            assignmentService.completeAssignment(assignmentId, testDriver.getId());
        });
    }

    @Test
    void testCannotReportIssueBeforeStarting() {
        // Try to report issue without starting delivery
        IssueReportDTO issue = new IssueReportDTO();
        issue.setIssueType("Test Issue");
        issue.setDescription("Test");

        // Assert
        assertThrows(Exception.class, () -> {
            shipmentService.reportIssue(shipmentId, testDriver.getId(), issue);
        });
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private ShipmentRequest createShipmentRequest(String trackingNumber) {
        ShipmentRequest request = new ShipmentRequest();
        request.setTrackingNumber(trackingNumber);
        request.setDescription("Additional test shipment");
        request.setWeightKg(BigDecimal.valueOf(50.0));
        request.setVolumeM3(BigDecimal.valueOf(2.0));
        request.setOriginAddress("Zagreb, Croatia");
        request.setOriginLatitude(45.8150);
        request.setOriginLongitude(15.9819);
        request.setDestinationAddress("Rijeka, Croatia");
        request.setDestinationLatitude(45.3271);
        request.setDestinationLongitude(14.4422);
        request.setExpectedDeliveryDate(LocalDateTime.now().plusDays(1));
        return request;
    }
}
