package hr.algebra.rapid.logisticsandfleetmanagementsystem.integration;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.ShipmentRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.schedule.ShipmentMaintenanceScheduler;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * ✅ POPRAVLJEN INTEGRACIJSKI TEST - Scheduler Bulk Operations
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SchedulerBulkOperationsIntegrationTest {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private ShipmentMaintenanceScheduler scheduler;

    @Autowired
    private EntityManager entityManager;  // ✅ DODANO

    @BeforeEach
    void setUp() {  // ✅ BEZ @Transactional
        // Očisti bazu prije svakog testa
        shipmentRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Transactional
    void testMarkOverdueShipments() {
        // Arrange - Kreiraj shipmente sa prošlim datumima
        Shipment overdue1 = createShipment("OVERDUE-001", ShipmentStatus.IN_TRANSIT,
                LocalDateTime.now().minusDays(2));
        Shipment overdue2 = createShipment("OVERDUE-002", ShipmentStatus.SCHEDULED,
                LocalDateTime.now().minusDays(1));
        Shipment onTime = createShipment("ONTIME-001", ShipmentStatus.IN_TRANSIT,
                LocalDateTime.now().plusDays(1));

        shipmentRepository.save(overdue1);
        shipmentRepository.save(overdue2);
        shipmentRepository.save(onTime);

        // ✅ Flush promjene u bazu
        entityManager.flush();
        entityManager.clear();

        // Act - Pokreni scheduler metodu
        scheduler.markOverdueShipments();

        // ✅ Flush nakon scheduler akcije
        entityManager.flush();
        entityManager.clear();

        // Assert - Provjeri da su overdue shipmenti označeni
        Shipment result1 = shipmentRepository.findById(overdue1.getId()).orElseThrow();
        Shipment result2 = shipmentRepository.findById(overdue2.getId()).orElseThrow();
        Shipment result3 = shipmentRepository.findById(onTime.getId()).orElseThrow();

        assertEquals(ShipmentStatus.OVERDUE, result1.getStatus(),
                "Shipment past expected delivery should be marked OVERDUE");
        assertEquals(ShipmentStatus.OVERDUE, result2.getStatus(),
                "Shipment past expected delivery should be marked OVERDUE");
        assertEquals(ShipmentStatus.IN_TRANSIT, result3.getStatus(),
                "Shipment not past expected delivery should remain IN_TRANSIT");
    }

    @Test
    @Transactional
    void testBulkStatusUpdate() {
        // Arrange - Kreiraj 10 shipmenata u PENDING statusu
        for (int i = 1; i <= 10; i++) {
            Shipment shipment = createShipment("BULK-" + String.format("%03d", i),
                    ShipmentStatus.PENDING,
                    LocalDateTime.now().plusDays(1));
            shipmentRepository.save(shipment);
        }

        // ✅ Flush svih shipmenata u bazu
        entityManager.flush();
        entityManager.clear();

        // Act - Bulk update svih PENDING u SCHEDULED
        List<Shipment> pendingShipments = shipmentRepository.findByStatus(ShipmentStatus.PENDING);
        assertEquals(10, pendingShipments.size(), "Should have 10 PENDING shipments");

        pendingShipments.forEach(s -> s.setStatus(ShipmentStatus.SCHEDULED));
        shipmentRepository.saveAll(pendingShipments);

        // ✅ Flush bulk update u bazu
        entityManager.flush();
        entityManager.clear();

        // Assert - Provjeri da su svi updatani
        List<Shipment> scheduledShipments = shipmentRepository.findByStatus(ShipmentStatus.SCHEDULED);
        List<Shipment> remainingPending = shipmentRepository.findByStatus(ShipmentStatus.PENDING);

        assertEquals(10, scheduledShipments.size(), "All 10 shipments should be SCHEDULED");
        assertEquals(0, remainingPending.size(), "No shipments should remain PENDING");
    }

    @Test
    @Transactional
    void testCleanupOldDeliveredShipments() {
        // Arrange - Kreiraj delivered shipmente (stare i nove)
        Shipment old1 = createShipment("OLD-001", ShipmentStatus.DELIVERED,
                LocalDateTime.now().minusDays(100));
        old1.setActualDeliveryDate(LocalDateTime.now().minusDays(100));

        Shipment old2 = createShipment("OLD-002", ShipmentStatus.DELIVERED,
                LocalDateTime.now().minusDays(95));
        old2.setActualDeliveryDate(LocalDateTime.now().minusDays(95));

        Shipment recent = createShipment("RECENT-001", ShipmentStatus.DELIVERED,
                LocalDateTime.now().minusDays(10));
        recent.setActualDeliveryDate(LocalDateTime.now().minusDays(10));

        shipmentRepository.save(old1);
        shipmentRepository.save(old2);
        shipmentRepository.save(recent);

        // ✅ Flush u bazu
        entityManager.flush();
        entityManager.clear();

        // Act - Očisti shipmente starije od 90 dana
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        List<Shipment> oldDelivered = shipmentRepository
                .findByStatusAndActualDeliveryDateBefore(ShipmentStatus.DELIVERED, cutoffDate);

        assertEquals(2, oldDelivered.size(), "Should find 2 old delivered shipments");

        shipmentRepository.deleteAll(oldDelivered);

        // ✅ Flush brisanja u bazu
        entityManager.flush();
        entityManager.clear();

        // Assert - Provjeri da su stari obrisani, a novi ostao
        List<Shipment> remaining = shipmentRepository.findByStatus(ShipmentStatus.DELIVERED);
        assertEquals(1, remaining.size(), "Only recent delivered shipment should remain");
        assertEquals("RECENT-001", remaining.get(0).getTrackingNumber());
    }

    @Test
    @Transactional
    void testFindShipmentsWithoutRoute() {
        // Arrange - Kreiraj shipmente sa i bez rute
        Shipment withRoute = createShipment("WITH-ROUTE-001", ShipmentStatus.PENDING,
                LocalDateTime.now().plusDays(1));
        Route route = new Route();
        route.setOriginAddress("Zagreb");
        route.setDestinationAddress("Split");
        route.setStatus(RouteStatus.CALCULATED);
        route.setEstimatedDistanceKm(300.0);
        route.setEstimatedDurationMinutes(180L);
        withRoute.setRoute(route);

        Shipment withoutRoute1 = createShipment("NO-ROUTE-001", ShipmentStatus.PENDING,
                LocalDateTime.now().plusDays(1));
        Shipment withoutRoute2 = createShipment("NO-ROUTE-002", ShipmentStatus.PENDING,
                LocalDateTime.now().plusDays(1));

        shipmentRepository.save(withRoute);
        shipmentRepository.save(withoutRoute1);
        shipmentRepository.save(withoutRoute2);

        // ✅ Flush u bazu
        entityManager.flush();
        entityManager.clear();

        // Act - Pronađi shipmente bez rute
        List<Shipment> shipmentsWithoutRoute = shipmentRepository.findByRouteIsNull();

        // Assert
        assertEquals(2, shipmentsWithoutRoute.size(), "Should find 2 shipments without route");
        assertTrue(shipmentsWithoutRoute.stream()
                        .allMatch(s -> s.getRoute() == null),
                "All found shipments should have null route");
    }

    @Test
    @Transactional
    void testSchedulerDoesNotAffectCompletedShipments() {
        // Arrange - Kreiraj completed/cancelled shipmente sa prošlim datumima
        Shipment delivered = createShipment("DELIVERED-001", ShipmentStatus.DELIVERED,
                LocalDateTime.now().minusDays(5));
        Shipment cancelled = createShipment("CANCELLED-001", ShipmentStatus.CANCELED,
                LocalDateTime.now().minusDays(5));

        shipmentRepository.save(delivered);
        shipmentRepository.save(cancelled);

        // ✅ Flush u bazu
        entityManager.flush();
        entityManager.clear();

        // Act - Pokreni scheduler
        scheduler.markOverdueShipments();

        // ✅ Flush nakon schedulera
        entityManager.flush();
        entityManager.clear();

        // Assert - Statusi se ne bi trebali promijeniti
        Shipment result1 = shipmentRepository.findById(delivered.getId()).orElseThrow();
        Shipment result2 = shipmentRepository.findById(cancelled.getId()).orElseThrow();

        assertEquals(ShipmentStatus.DELIVERED, result1.getStatus(),
                "DELIVERED status should not change");
        assertEquals(ShipmentStatus.CANCELED, result2.getStatus(),
                "CANCELLED status should not change");
    }

    @Test
    @Transactional
    void testCountShipmentsByStatus() {
        // Arrange - Kreiraj različite statuse
        shipmentRepository.save(createShipment("PENDING-001", ShipmentStatus.PENDING,
                LocalDateTime.now().plusDays(1)));
        shipmentRepository.save(createShipment("PENDING-002", ShipmentStatus.PENDING,
                LocalDateTime.now().plusDays(1)));
        shipmentRepository.save(createShipment("SCHEDULED-001", ShipmentStatus.SCHEDULED,
                LocalDateTime.now().plusDays(1)));
        shipmentRepository.save(createShipment("INTRANSIT-001", ShipmentStatus.IN_TRANSIT,
                LocalDateTime.now().plusDays(1)));
        shipmentRepository.save(createShipment("INTRANSIT-002", ShipmentStatus.IN_TRANSIT,
                LocalDateTime.now().plusDays(1)));
        shipmentRepository.save(createShipment("INTRANSIT-003", ShipmentStatus.IN_TRANSIT,
                LocalDateTime.now().plusDays(1)));
        shipmentRepository.save(createShipment("DELIVERED-001", ShipmentStatus.DELIVERED,
                LocalDateTime.now().plusDays(1)));

        // ✅ Flush u bazu
        entityManager.flush();
        entityManager.clear();

        // Act & Assert
        assertEquals(2, shipmentRepository.findByStatus(ShipmentStatus.PENDING).size());
        assertEquals(1, shipmentRepository.findByStatus(ShipmentStatus.SCHEDULED).size());
        assertEquals(3, shipmentRepository.findByStatus(ShipmentStatus.IN_TRANSIT).size());
        assertEquals(1, shipmentRepository.findByStatus(ShipmentStatus.DELIVERED).size());
        assertEquals(7, shipmentRepository.findAll().size());
    }

    // Helper method
    private Shipment createShipment(String trackingNumber, ShipmentStatus status,
                                    LocalDateTime expectedDeliveryDate) {
        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(trackingNumber);
        shipment.setDescription("Test shipment");
        shipment.setWeightKg(BigDecimal.valueOf(10.0));
        shipment.setVolumeM3(BigDecimal.valueOf(1.0));
        shipment.setOriginAddress("Test Origin");
        shipment.setDestinationAddress("Test Destination");
        shipment.setStatus(status);
        shipment.setExpectedDeliveryDate(expectedDeliveryDate);
        return shipment;
    }
}