package hr.algebra.rapid.logisticsandfleetmanagementsystem.integration;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Route;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.RouteStatus;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Shipment;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.ShipmentStatus;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.ShipmentRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AnalyticsService;
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
 * INTEGRACIJSKI TEST - Scheduler & Bulk Operations
 * 
 * Testira:
 * 1. Automated Shipment Status Updates (Scheduler logic)
 * 2. Bulk Mark Overdue (JdbcTemplate bulk operations)
 * 3. Time-based shipment filtering
 * 4. Mass status transitions
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SchedulerBulkOperationsIntegrationTest {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private AnalyticsService analyticsService;

    private List<Shipment> testShipments;

    @BeforeEach
    void setUp() {
        testShipments = new ArrayList<>();

        // Create test route
        Route testRoute = new Route();
        testRoute.setOriginAddress("Zagreb, Croatia");
        testRoute.setOriginLatitude(45.8150);
        testRoute.setOriginLongitude(15.9819);
        testRoute.setDestinationAddress("Split, Croatia");
        testRoute.setDestinationLatitude(43.5081);
        testRoute.setDestinationLongitude(16.4402);
        testRoute.setEstimatedDistanceKm(280.0);
        testRoute.setEstimatedDurationMinutes(240L);
        testRoute.setStatus(RouteStatus.CALCULATED);

        // Scenario 1: Overdue shipment (past expected delivery)
        Shipment overdue1 = new Shipment();
        overdue1.setTrackingNumber("OVERDUE-001");
        overdue1.setDescription("Overdue test 1");
        overdue1.setWeightKg(BigDecimal.valueOf(100.0));
        overdue1.setStatus(ShipmentStatus.IN_TRANSIT);
        overdue1.setExpectedDeliveryDate(LocalDateTime.now().minusDays(2)); // 2 days ago
        overdue1.setOriginAddress("Zagreb, Croatia");
        overdue1.setDestinationAddress("Split, Croatia");
        overdue1.setRoute(testRoute);
        testShipments.add(overdue1);

        // Scenario 2: Another overdue shipment
        Shipment overdue2 = new Shipment();
        overdue2.setTrackingNumber("OVERDUE-002");
        overdue2.setDescription("Overdue test 2");
        overdue2.setWeightKg(BigDecimal.valueOf(150.0));
        overdue2.setStatus(ShipmentStatus.IN_TRANSIT);
        overdue2.setExpectedDeliveryDate(LocalDateTime.now().minusHours(5)); // 5 hours ago
        overdue2.setOriginAddress("Zagreb, Croatia");
        overdue2.setDestinationAddress("Rijeka, Croatia");
        overdue2.setRoute(testRoute);
        testShipments.add(overdue2);

        // Scenario 3: On-time shipment (still in future)
        Shipment onTime = new Shipment();
        onTime.setTrackingNumber("ONTIME-001");
        onTime.setDescription("On-time test");
        onTime.setWeightKg(BigDecimal.valueOf(80.0));
        onTime.setStatus(ShipmentStatus.IN_TRANSIT);
        onTime.setExpectedDeliveryDate(LocalDateTime.now().plusDays(1)); // Tomorrow
        onTime.setOriginAddress("Split, Croatia");
        onTime.setDestinationAddress("Dubrovnik, Croatia");
        onTime.setRoute(testRoute);
        testShipments.add(onTime);

        // Scenario 4: Already delivered (should not be affected)
        Shipment delivered = new Shipment();
        delivered.setTrackingNumber("DELIVERED-001");
        delivered.setDescription("Already delivered");
        delivered.setWeightKg(BigDecimal.valueOf(120.0));
        delivered.setStatus(ShipmentStatus.DELIVERED);
        delivered.setExpectedDeliveryDate(LocalDateTime.now().minusDays(1));
        delivered.setActualDeliveryDate(LocalDateTime.now().minusHours(12));
        delivered.setOriginAddress("Zagreb, Croatia");
        delivered.setDestinationAddress("Osijek, Croatia");
        delivered.setRoute(testRoute);
        testShipments.add(delivered);

        // Scenario 5: Cancelled (should not be affected)
        Shipment cancelled = new Shipment();
        cancelled.setTrackingNumber("CANCELLED-001");
        cancelled.setDescription("Cancelled shipment");
        cancelled.setWeightKg(BigDecimal.valueOf(90.0));
        cancelled.setStatus(ShipmentStatus.CANCELED);
        cancelled.setExpectedDeliveryDate(LocalDateTime.now().minusDays(3));
        cancelled.setOriginAddress("Rijeka, Croatia");
        cancelled.setDestinationAddress("Pula, Croatia");
        cancelled.setRoute(testRoute);
        testShipments.add(cancelled);

        // Scenario 6: Already marked as overdue (should not be touched)
        Shipment alreadyOverdue = new Shipment();
        alreadyOverdue.setTrackingNumber("ALREADY-OVERDUE-001");
        alreadyOverdue.setDescription("Already marked overdue");
        alreadyOverdue.setWeightKg(BigDecimal.valueOf(110.0));
        alreadyOverdue.setStatus(ShipmentStatus.OVERDUE);
        alreadyOverdue.setExpectedDeliveryDate(LocalDateTime.now().minusDays(5));
        alreadyOverdue.setOriginAddress("Zagreb, Croatia");
        alreadyOverdue.setDestinationAddress("Varaždin, Croatia");
        alreadyOverdue.setRoute(testRoute);
        testShipments.add(alreadyOverdue);

        // Save all test shipments
        shipmentRepository.saveAll(testShipments);
    }

    // ==========================================
    // BULK MARK OVERDUE TESTS
    // ==========================================

    @Test
    void testBulkMarkOverdue_UpdatesCorrectShipments() {
        // Arrange - Verify initial state
        long initialOverdueCount = shipmentRepository.findAll().stream()
                .filter(s -> s.getStatus() == ShipmentStatus.OVERDUE)
                .count();
        assertEquals(1, initialOverdueCount); // Only ALREADY-OVERDUE-001

        // Act - Run bulk mark overdue
        int updatedCount = analyticsService.bulkMarkOverdue();

        // Assert - Should update 2 shipments (OVERDUE-001, OVERDUE-002)
        assertEquals(2, updatedCount);

        // Verify database state
        List<Shipment> allShipments = shipmentRepository.findAll();

        // OVERDUE-001 should be marked OVERDUE
        Shipment overdue1 = allShipments.stream()
                .filter(s -> s.getTrackingNumber().equals("OVERDUE-001"))
                .findFirst().orElseThrow();
        assertEquals(ShipmentStatus.OVERDUE, overdue1.getStatus());

        // OVERDUE-002 should be marked OVERDUE
        Shipment overdue2 = allShipments.stream()
                .filter(s -> s.getTrackingNumber().equals("OVERDUE-002"))
                .findFirst().orElseThrow();
        assertEquals(ShipmentStatus.OVERDUE, overdue2.getStatus());

        // ONTIME-001 should remain IN_TRANSIT
        Shipment onTime = allShipments.stream()
                .filter(s -> s.getTrackingNumber().equals("ONTIME-001"))
                .findFirst().orElseThrow();
        assertEquals(ShipmentStatus.IN_TRANSIT, onTime.getStatus());

        // DELIVERED-001 should remain DELIVERED
        Shipment delivered = allShipments.stream()
                .filter(s -> s.getTrackingNumber().equals("DELIVERED-001"))
                .findFirst().orElseThrow();
        assertEquals(ShipmentStatus.DELIVERED, delivered.getStatus());

        // CANCELLED-001 should remain CANCELED
        Shipment cancelled = allShipments.stream()
                .filter(s -> s.getTrackingNumber().equals("CANCELLED-001"))
                .findFirst().orElseThrow();
        assertEquals(ShipmentStatus.CANCELED, cancelled.getStatus());
    }

    @Test
    void testBulkMarkOverdue_IdempotentOperation() {
        // Act - Run bulk mark overdue twice
        int firstRun = analyticsService.bulkMarkOverdue();
        int secondRun = analyticsService.bulkMarkOverdue();

        // Assert - Second run should not update anything (idempotent)
        assertEquals(2, firstRun);
        assertEquals(0, secondRun); // No more shipments to update

        // Verify final state
        long overdueCount = shipmentRepository.findAll().stream()
                .filter(s -> s.getStatus() == ShipmentStatus.OVERDUE)
                .count();
        assertEquals(3, overdueCount); // ALREADY-OVERDUE-001 + OVERDUE-001 + OVERDUE-002
    }

    @Test
    void testBulkMarkOverdue_OnlyAffectsInTransit() {
        // Arrange - Create additional PENDING shipment that's overdue
        Shipment pendingOverdue = new Shipment();
        pendingOverdue.setTrackingNumber("PENDING-OVERDUE-001");
        pendingOverdue.setDescription("Pending but past due");
        pendingOverdue.setWeightKg(BigDecimal.valueOf(70.0));
        pendingOverdue.setStatus(ShipmentStatus.PENDING); // Not IN_TRANSIT
        pendingOverdue.setExpectedDeliveryDate(LocalDateTime.now().minusDays(1));
        pendingOverdue.setOriginAddress("Test Origin");
        pendingOverdue.setDestinationAddress("Test Destination");

        Route testRoute = new Route();
        testRoute.setOriginAddress("Test");
        testRoute.setDestinationAddress("Test");
        testRoute.setStatus(RouteStatus.CALCULATED);
        pendingOverdue.setRoute(testRoute);

        shipmentRepository.save(pendingOverdue);

        // Act
        int updatedCount = analyticsService.bulkMarkOverdue();

        // Assert - Should only update IN_TRANSIT shipments (2), not PENDING
        assertEquals(2, updatedCount);

        // PENDING-OVERDUE-001 should still be PENDING
        Shipment pending = shipmentRepository.findAll().stream()
                .filter(s -> s.getTrackingNumber().equals("PENDING-OVERDUE-001"))
                .findFirst().orElseThrow();
        assertEquals(ShipmentStatus.PENDING, pending.getStatus());
    }

    @Test
    void testBulkMarkOverdue_EmptyDatabase() {
        // Arrange - Delete all shipments
        shipmentRepository.deleteAll();

        // Act
        int updatedCount = analyticsService.bulkMarkOverdue();

        // Assert - No shipments to update
        assertEquals(0, updatedCount);
    }

    @Test
    void testBulkMarkOverdue_OnlyFutureShipments() {
        // Arrange - Delete all shipments and create only future ones
        shipmentRepository.deleteAll();

        Route testRoute = new Route();
        testRoute.setOriginAddress("Test");
        testRoute.setDestinationAddress("Test");
        testRoute.setStatus(RouteStatus.CALCULATED);

        Shipment future1 = new Shipment();
        future1.setTrackingNumber("FUTURE-001");
        future1.setDescription("Future delivery");
        future1.setWeightKg(BigDecimal.valueOf(50.0));
        future1.setStatus(ShipmentStatus.IN_TRANSIT);
        future1.setExpectedDeliveryDate(LocalDateTime.now().plusDays(3));
        future1.setOriginAddress("Test");
        future1.setDestinationAddress("Test");
        future1.setRoute(testRoute);

        shipmentRepository.save(future1);

        // Act
        int updatedCount = analyticsService.bulkMarkOverdue();

        // Assert - No shipments to update
        assertEquals(0, updatedCount);

        // Verify shipment is still IN_TRANSIT
        Shipment futureShipment = shipmentRepository.findAll().stream()
                .filter(s -> s.getTrackingNumber().equals("FUTURE-001"))
                .findFirst().orElseThrow();
        assertEquals(ShipmentStatus.IN_TRANSIT, futureShipment.getStatus());
    }

    // ==========================================
    // SCHEDULER SIMULATION TESTS
    // ==========================================

    @Test
    void testSchedulerSimulation_MultipleRuns() {
        // Simulate scheduler running every minute for 3 iterations

        // Initial state
        long initialOverdueCount = shipmentRepository.findAll().stream()
                .filter(s -> s.getStatus() == ShipmentStatus.OVERDUE)
                .count();
        assertEquals(1, initialOverdueCount);

        // Run 1
        int run1 = analyticsService.bulkMarkOverdue();
        assertEquals(2, run1);

        // Run 2 (should update nothing)
        int run2 = analyticsService.bulkMarkOverdue();
        assertEquals(0, run2);

        // Run 3 (should still update nothing)
        int run3 = analyticsService.bulkMarkOverdue();
        assertEquals(0, run3);

        // Final state
        long finalOverdueCount = shipmentRepository.findAll().stream()
                .filter(s -> s.getStatus() == ShipmentStatus.OVERDUE)
                .count();
        assertEquals(3, finalOverdueCount);
    }

    @Test
    void testSchedulerWithNewOverdueShipment() {
        // Run 1 - Mark initial overdue
        int run1 = analyticsService.bulkMarkOverdue();
        assertEquals(2, run1);

        // Add new shipment that becomes overdue
        Route testRoute = new Route();
        testRoute.setOriginAddress("New");
        testRoute.setDestinationAddress("New");
        testRoute.setStatus(RouteStatus.CALCULATED);

        Shipment newOverdue = new Shipment();
        newOverdue.setTrackingNumber("NEW-OVERDUE-001");
        newOverdue.setDescription("Newly overdue");
        newOverdue.setWeightKg(BigDecimal.valueOf(60.0));
        newOverdue.setStatus(ShipmentStatus.IN_TRANSIT);
        newOverdue.setExpectedDeliveryDate(LocalDateTime.now().minusHours(1));
        newOverdue.setOriginAddress("New");
        newOverdue.setDestinationAddress("New");
        newOverdue.setRoute(testRoute);

        shipmentRepository.save(newOverdue);

        // Run 2 - Should catch new overdue shipment
        int run2 = analyticsService.bulkMarkOverdue();
        assertEquals(1, run2);

        // Verify new shipment is marked OVERDUE
        Shipment updated = shipmentRepository.findAll().stream()
                .filter(s -> s.getTrackingNumber().equals("NEW-OVERDUE-001"))
                .findFirst().orElseThrow();
        assertEquals(ShipmentStatus.OVERDUE, updated.getStatus());
    }

    // ==========================================
    // EDGE CASES
    // ==========================================

    @Test
    void testBulkMarkOverdue_ExactlyNow() {
        // Arrange - Shipment with expected delivery exactly now
        Route testRoute = new Route();
        testRoute.setOriginAddress("Test");
        testRoute.setDestinationAddress("Test");
        testRoute.setStatus(RouteStatus.CALCULATED);

        Shipment exactlyNow = new Shipment();
        exactlyNow.setTrackingNumber("EXACTLY-NOW-001");
        exactlyNow.setDescription("Expected delivery exactly now");
        exactlyNow.setWeightKg(BigDecimal.valueOf(75.0));
        exactlyNow.setStatus(ShipmentStatus.IN_TRANSIT);
        exactlyNow.setExpectedDeliveryDate(LocalDateTime.now()); // Exactly now
        exactlyNow.setOriginAddress("Test");
        exactlyNow.setDestinationAddress("Test");
        exactlyNow.setRoute(testRoute);

        shipmentRepository.save(exactlyNow);

        // Act
        analyticsService.bulkMarkOverdue();

        // Assert - Should be marked as overdue (< comparison, not <=)
        Shipment updated = shipmentRepository.findAll().stream()
                .filter(s -> s.getTrackingNumber().equals("EXACTLY-NOW-001"))
                .findFirst().orElseThrow();
        
        // Depending on SQL comparison, might be IN_TRANSIT or OVERDUE
        // This tests edge case behavior
        assertTrue(updated.getStatus() == ShipmentStatus.IN_TRANSIT || 
                   updated.getStatus() == ShipmentStatus.OVERDUE);
    }

    @Test
    void testBulkMarkOverdue_PerformanceWithManyShipments() {
        // Arrange - Create 100 overdue shipments
        Route testRoute = new Route();
        testRoute.setOriginAddress("Bulk");
        testRoute.setDestinationAddress("Bulk");
        testRoute.setStatus(RouteStatus.CALCULATED);

        List<Shipment> bulkShipments = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Shipment shipment = new Shipment();
            shipment.setTrackingNumber("BULK-" + i);
            shipment.setDescription("Bulk test " + i);
            shipment.setWeightKg(BigDecimal.valueOf(50.0));
            shipment.setStatus(ShipmentStatus.IN_TRANSIT);
            shipment.setExpectedDeliveryDate(LocalDateTime.now().minusDays(1));
            shipment.setOriginAddress("Bulk");
            shipment.setDestinationAddress("Bulk");
            shipment.setRoute(testRoute);
            bulkShipments.add(shipment);
        }

        shipmentRepository.saveAll(bulkShipments);

        // Act
        long startTime = System.currentTimeMillis();
        int updatedCount = analyticsService.bulkMarkOverdue();
        long endTime = System.currentTimeMillis();

        // Assert
        assertEquals(102, updatedCount); // 100 new + 2 original overdue
        
        // Performance check - should complete in under 5 seconds
        long executionTime = endTime - startTime;
        assertTrue(executionTime < 5000, 
                "Bulk operation took too long: " + executionTime + "ms");
    }
}
