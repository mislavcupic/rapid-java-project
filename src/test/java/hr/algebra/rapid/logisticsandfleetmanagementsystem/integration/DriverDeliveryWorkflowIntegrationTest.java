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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ✅ FIXED INTEGRACIJSKI TEST - Driver Delivery Complete Workflow
 *
 * IZMJENE:
 * - Maknut @Transactional s klase
 * - Dodana @Transactional samo na setUp() i svaki test
 * - Eksplicitno korištenje ArrayList za roles
 * - Dodani detaljniji asserts
 */
@SpringBootTest
@ActiveProfiles("test")
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
    @Transactional
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
        userInfo.setPassword("$2a$10$hashedPassword");
        userInfo.setFirstName("Workflow");
        userInfo.setLastName("Driver");
        userInfo.setEmail("workflow@test.com");
        userInfo.setIsEnabled(true);

        List<UserRole> rolesList = new ArrayList<>();
        rolesList.add(driverRole);
        userInfo.setRoles(rolesList);

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
        testVehicle.setLoadCapacityKg(BigDecimal.valueOf(1000));
        testVehicle.setCurrentMileageKm(50000L);
        testVehicle.setNextServiceMileageKm(55000L);
        testVehicle.setLastServiceDate(java.time.LocalDate.now().minusMonths(2));
        testVehicle.setFuelConsumptionLitersPer100Km(BigDecimal.valueOf(8.5));
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

        // ✅ FIX: Promijeni status iz PENDING u SCHEDULED prije assignanja
        Shipment shipment = shipmentRepository.findById(shipmentId).orElseThrow();
        shipment.setStatus(ShipmentStatus.SCHEDULED);
        shipmentRepository.save(shipment);

        // Create Assignment (sada će proći jer shipment nije PENDING)
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
    @Transactional
    void testCompleteDeliveryWorkflow_HappyPath() {
        // Step 1: Verify initial state
        Shipment initialShipment = shipmentRepository.findById(shipmentId).orElseThrow();
        assertEquals(ShipmentStatus.SCHEDULED, initialShipment.getStatus(), "Initial shipment status should be SCHEDULED");

        Assignment initialAssignment = assignmentRepository.findById(assignmentId).orElseThrow();
        assertEquals("SCHEDULED", initialAssignment.getStatus(), "Initial assignment status should be SCHEDULED");

        // Step 2: Driver starts assignment
        Optional<AssignmentResponseDTO> startedAssignment = assignmentService.startAssignment(assignmentId, testDriver.getId());
        assertEquals("IN_PROGRESS", startedAssignment.get().getAssignmentStatus(), "Assignment should be IN_PROGRESS");

        ShipmentResponse startedShipment = shipmentService.findById(shipmentId).orElseThrow();
        assertEquals(ShipmentStatus.IN_TRANSIT, startedShipment.getStatus(), "Shipment should be IN_TRANSIT");

        // Step 3: Driver completes delivery
        ProofOfDeliveryDTO pod = new ProofOfDeliveryDTO();
        pod.setRecipientName("John Doe");
        pod.setNotes("Delivered successfully");
        pod.setLatitude(43.5081);
        pod.setLongitude(16.4402);

        ShipmentResponse completedShipment = shipmentService.completeDelivery(shipmentId, testDriver.getId(), pod);
        assertEquals(ShipmentStatus.DELIVERED, completedShipment.getStatus(), "Shipment should be DELIVERED");

        // Step 4: Driver completes assignment
        Optional<AssignmentResponseDTO> completedAssignment = assignmentService.completeAssignment(assignmentId, testDriver.getId());
        assertEquals("COMPLETED", completedAssignment.get().getAssignmentStatus(), "Assignment should be COMPLETED");
        assertNotNull(completedAssignment.get().getEndTime(), "End time should be set");

        // Final verification
        Shipment finalShipment = shipmentRepository.findById(shipmentId).orElseThrow();
        assertEquals(ShipmentStatus.DELIVERED, finalShipment.getStatus(), "Final shipment status should be DELIVERED");

        Assignment finalAssignment = assignmentRepository.findById(assignmentId).orElseThrow();
        assertEquals("COMPLETED", finalAssignment.getStatus(), "Final assignment status should be COMPLETED");
    }

    // ==========================================
    // ISSUE REPORTING WORKFLOW
    // ==========================================

    @Test
    @Transactional
    void testIssueReportingWorkflow() {
        // Step 1: Start assignment
        assignmentService.startAssignment(assignmentId, testDriver.getId());

        // Step 2: Report issue
        IssueReportDTO issue = new IssueReportDTO();
        issue.setIssueType("Vehicle Issue");
        issue.setDescription("Engine problem");
        issue.setEstimatedDelay("2 hours");
        issue.setLatitude(45.5);
        issue.setLongitude(16.0);

        ShipmentResponse delayedShipment = shipmentService.reportIssue(shipmentId, testDriver.getId(), issue);
        assertEquals(ShipmentStatus.DELAYED, delayedShipment.getStatus(), "Shipment should be DELAYED");

        // Verify persistence
        Shipment persistedShipment = shipmentRepository.findById(shipmentId).orElseThrow();
        assertEquals(ShipmentStatus.DELAYED, persistedShipment.getStatus(), "Persisted shipment should be DELAYED");
    }

    // ==========================================
    // DRIVER DASHBOARD TESTS
    // ==========================================



    @Test
    @Transactional
    void testDriverDashboard_InProgressAssignments() {
        // Start the assignment
        assignmentService.startAssignment(assignmentId, testDriver.getId());

        // Act
        var driverAssignments = assignmentService.findAssignmentsByDriver(testDriver.getId());

        // Assert
        assertEquals(1, driverAssignments.size(), "Driver should have 1 assignment");
        assertEquals("IN_PROGRESS", driverAssignments.get(0).getAssignmentStatus(),
                "Assignment should be IN_PROGRESS");
    }

    @Test
    @Transactional
    void testDriverDashboard_CompletedNotShown() {
        // Complete the entire workflow
        assignmentService.startAssignment(assignmentId, testDriver.getId());

        ProofOfDeliveryDTO pod = new ProofOfDeliveryDTO();
        pod.setRecipientName("Test Recipient");
        pod.setLatitude(43.5081);
        pod.setLongitude(16.4402);
        shipmentService.completeDelivery(shipmentId, testDriver.getId(), pod);

        assignmentService.completeAssignment(assignmentId, testDriver.getId());

        // Act
        var driverAssignments = assignmentService.findAssignmentsByDriver(testDriver.getId());

        // Assert - Completed assignments should not appear
        assertEquals(0, driverAssignments.size(), "Completed assignments should not appear in dashboard");
    }

    // ==========================================
    // ERROR SCENARIOS
    // ==========================================

    @Test
    @Transactional
    void testCannotCompleteDeliveryWithoutStarting() {
        // Try to complete delivery without starting
        ProofOfDeliveryDTO pod = new ProofOfDeliveryDTO();
        pod.setRecipientName("John Doe");
        pod.setLatitude(43.5081);
        pod.setLongitude(16.4402);

        // Assert - Should throw exception
        assertThrows(Exception.class, () -> {
            shipmentService.completeDelivery(shipmentId, testDriver.getId(), pod);
        }, "Should not be able to complete delivery without starting");
    }

    @Test
    @Transactional
    void testCannotCompleteAssignmentBeforeDelivery() {
        // Start assignment
        assignmentService.startAssignment(assignmentId, testDriver.getId());

        // Try to complete assignment without completing delivery
        assertThrows(Exception.class, () -> {
            assignmentService.completeAssignment(assignmentId, testDriver.getId());
        }, "Should not be able to complete assignment before delivery");
    }

    @Test
    @Transactional
    void testCannotReportIssueBeforeStarting() {
        // Try to report issue without starting delivery
        IssueReportDTO issue = new IssueReportDTO();
        issue.setIssueType("Test Issue");
        issue.setDescription("Test");

        // Assert
        assertThrows(Exception.class, () -> {
            shipmentService.reportIssue(shipmentId, testDriver.getId(), issue);
        }, "Should not be able to report issue before starting");
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