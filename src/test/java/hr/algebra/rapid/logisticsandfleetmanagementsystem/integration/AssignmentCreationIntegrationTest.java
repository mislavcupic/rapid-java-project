package hr.algebra.rapid.logisticsandfleetmanagementsystem.integration;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ConflictException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AssignmentService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class AssignmentCreationIntegrationTest {

    @Autowired
    private AssignmentService assignmentService;
    @Autowired
    private ShipmentRepository shipmentRepository;
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private EntityManager entityManager;

    private Long driverId;
    private Long vehicleId;
    private Long shipmentId;

    @BeforeEach
    void setUp() {
        // 1. Korisnik
        UserInfo user = new UserInfo();
        user.setUsername("test_user_" + System.nanoTime());
        user.setEmail("email_" + System.nanoTime() + "@test.com");
        user.setPassword("password");
        entityManager.persist(user);

        // 2. Vozač
        Driver driver = new Driver();
        driver.setUserInfo(user);
        driver.setLicenseNumber("LIC-123");
        driver = driverRepository.saveAndFlush(driver);
        this.driverId = driver.getId();

        // 3. Vozilo
        Vehicle vehicle = new Vehicle();
        vehicle.setMake("Iveco");
        vehicle.setModel("Daily");
        vehicle.setLicensePlate("ZG-1234-AA");
        vehicle = vehicleRepository.saveAndFlush(vehicle);
        this.vehicleId = vehicle.getId();

        // 4. Pošiljka - Koordinate Zagreb → Split
        Shipment shipment = new Shipment();
        shipment.setTrackingNumber("TRK-001");
        shipment.setOriginAddress("Ulica 1, Zagreb");
        shipment.setDestinationAddress("Ulica 2, Split");
        shipment.setOriginLatitude(45.815);
        shipment.setOriginLongitude(15.981);
        shipment.setDestinationLatitude(43.508);
        shipment.setDestinationLongitude(16.440);
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setWeightKg(BigDecimal.valueOf(10));
        shipment.setVolumeM3(BigDecimal.valueOf(1));
        shipment = shipmentRepository.saveAndFlush(shipment);
        this.shipmentId = shipment.getId();

        entityManager.flush();
        entityManager.clear();
    }

    private AssignmentRequestDTO getValidReq() {
        AssignmentRequestDTO req = new AssignmentRequestDTO();
        req.setDriverId(driverId);
        req.setVehicleId(vehicleId);
        req.setShipmentIds(new ArrayList<>(List.of(shipmentId)));
        req.setStartTime(LocalDateTime.now().plusDays(1));
        return req;
    }

    // ========================================
    // TESTOVI
    // ========================================

    @Test
    @DisplayName("1. Brisanje naloga - Vraćanje statusa pošiljaka")
    void testDeleteAssignmentFlow() {
        // 1. Kreiranje rute
        Route route = new Route();
        route.setOriginAddress("Zagreb");
        route.setDestinationAddress("Split");
        route.setEstimatedDistanceKm(400.0);
        route.setEstimatedDurationMinutes(300L);
        route.setStatus(RouteStatus.DRAFT);
        final Route savedRoute = routeRepository.saveAndFlush(route);

        // 2. Povezivanje pošiljke s rutom
        Shipment s = shipmentRepository.findById(this.shipmentId).orElseThrow();
        s.setRoute(savedRoute);
        shipmentRepository.saveAndFlush(s);

        entityManager.flush();
        entityManager.clear();

        // 3. Kreiranje assignmenta
        AssignmentRequestDTO req = new AssignmentRequestDTO();
        req.setDriverId(this.driverId);
        req.setVehicleId(this.vehicleId);
        req.setShipmentIds(new ArrayList<>(List.of(this.shipmentId)));
        req.setStartTime(LocalDateTime.now().plusHours(1));

        AssignmentResponseDTO res = assignmentService.createAssignment(req);
        // ✅ RELOAD - Force refresh from DB
        entityManager.flush();
        entityManager.clear();
        res = assignmentService.findById(res.getId()).orElseThrow();

        assertNotNull(res);
        Long assignmentId = res.getId();

        // 4. Brisanje assignmenta
        assertDoesNotThrow(() -> assignmentService.deleteAssignment(assignmentId));

        entityManager.flush();
        assertFalse(assignmentRepository.existsById(assignmentId));

        // 5. Provjeri da je pošiljka vraćena u PENDING status
        Shipment deletedShipment = shipmentRepository.findById(shipmentId).orElseThrow();
        assertEquals(ShipmentStatus.PENDING, deletedShipment.getStatus(),
                "Pošiljka mora biti vraćena u PENDING status nakon brisanja assignmenta");
    }

    @Test
    @DisplayName("2. Kreiranje assignmenta - Provjera persistencije")
    void testCreateAssignmentPersistence() {
        AssignmentResponseDTO res = assignmentService.createAssignment(getValidReq());
        // ✅ RELOAD
        entityManager.flush();
        entityManager.clear();
        res = assignmentService.findById(res.getId()).orElseThrow();

        assertNotNull(res.getId(), "Assignment mora imati ID");

        Assignment saved = assignmentRepository.findById(res.getId()).orElseThrow();
        assertNotNull(saved.getRoute(), "Ruta mora biti kreirana i povezana");
        assertEquals("SCHEDULED", saved.getStatus(), "Status mora biti SCHEDULED");
    }

    @Test
    @DisplayName("3. Početak assignmenta - Workflow test")
    void testStartAssignmentWorkflow() {
        AssignmentResponseDTO res = assignmentService.createAssignment(getValidReq());
        // ✅ RELOAD
        entityManager.flush();
        entityManager.clear();
        res = assignmentService.findById(res.getId()).orElseThrow();

        Long aId = res.getId();

        // Pokreni assignment
        assignmentService.startAssignment(aId, driverId);

        // Provjeri da je pošiljka u IN_TRANSIT statusu
        Shipment shipment = shipmentRepository.findById(shipmentId).orElseThrow();
        assertEquals(ShipmentStatus.IN_TRANSIT, shipment.getStatus(),
                "Pošiljka mora biti u IN_TRANSIT statusu nakon pokretanja assignmenta");

        // Provjeri da je assignment u IN_PROGRESS statusu
        Assignment assignment = assignmentRepository.findById(aId).orElseThrow();
        assertEquals("IN_PROGRESS", assignment.getStatus(),
                "Assignment mora biti u IN_PROGRESS statusu");
    }


    @Test
    @DisplayName("5. Dovršavanje assignmenta - Success scenario")
    void testCompleteAssignmentSuccess() {
        AssignmentResponseDTO res = assignmentService.createAssignment(getValidReq());
        // ✅ RELOAD
        entityManager.flush();
        entityManager.clear();
        res = assignmentService.findById(res.getId()).orElseThrow();

        // 1. Pokreni assignment
        assignmentService.startAssignment(res.getId(), driverId);

        // 2. Postavi pošiljku kao isporučenu
        Shipment s = shipmentRepository.findById(shipmentId).orElseThrow();
        s.setStatus(ShipmentStatus.DELIVERED);
        shipmentRepository.saveAndFlush(s);

        // 3. Završi assignment
        assignmentService.completeAssignment(res.getId(), driverId);

        // 4. Provjeri status
        Assignment completed = assignmentRepository.findById(res.getId()).orElseThrow();
        assertEquals("COMPLETED", completed.getStatus(),
                "Assignment mora biti u COMPLETED statusu");
        assertNotNull(completed.getEndTime(),
                "EndTime mora biti postavljen");
    }

    @Test
    @DisplayName("6. Forsirana promjena statusa pošiljke")
    void testForceUpdateShipmentStatus() {
        assignmentService.updateShipmentStatus(shipmentId, ShipmentStatus.CANCELED);

        Shipment updated = shipmentRepository.findById(shipmentId).orElseThrow();
        assertEquals(ShipmentStatus.CANCELED, updated.getStatus(),
                "Status pošiljke mora biti CANCELED");
    }

    @Test
    @DisplayName("7. Dohvaćanje assignmenta po vozaču")
    void testFindAssignmentsByDriver() {
        assignmentService.createAssignment(getValidReq());

        List<AssignmentResponseDTO> assignments = assignmentService.findAssignmentsByDriver(driverId);

        assertFalse(assignments.isEmpty(), "Vozač mora imati barem jedan assignment");
        assertEquals(driverId, assignments.get(0).getDriver().getId(),
                "Assignment mora pripadati ispravnom vozaču");
    }

    @Test
    @DisplayName("8. Pokretanje assignmenta - Pogrešan vozač")
    void testStartAssignmentWithWrongDriver() {
        AssignmentResponseDTO res = assignmentService.createAssignment(getValidReq());
        // ✅ RELOAD
        entityManager.flush();
        entityManager.clear();
        res = assignmentService.findById(res.getId()).orElseThrow();

        Long aId = res.getId();
        Long wrongId = 9999L;

        assertThrows(ConflictException.class,
                () -> assignmentService.startAssignment(aId, wrongId),
                "Mora baciti ConflictException za pogrešnog vozača");
    }

    @Test
    @DisplayName("9. Kreiranje assignmenta bez pošiljaka")
    void testCreateAssignmentWithNoShipments() {
        AssignmentRequestDTO req = getValidReq();
        req.setShipmentIds(List.of());

        assertThrows(ConflictException.class,
                () -> assignmentService.createAssignment(req),
                "Mora baciti ConflictException ako nema pošiljaka");
    }

    @Test
    @DisplayName("10. Brisanje nepostojećeg assignmenta")
    void testDeleteNonExistentAssignment() {
        assertThrows(ResourceNotFoundException.class,
                () -> assignmentService.deleteAssignment(9999L),
                "Mora baciti ResourceNotFoundException za nepostojeći assignment");
    }

    @Test
    @DisplayName("11. Dovršavanje assignmenta prije pokretanja")
    void testCompleteAssignmentBeforeStartFails() {
        AssignmentResponseDTO res = assignmentService.createAssignment(getValidReq());
        // ✅ RELOAD
        entityManager.flush();
        entityManager.clear();
        res = assignmentService.findById(res.getId()).orElseThrow();

        Long aId = res.getId();

        assertThrows(ConflictException.class,
                () -> assignmentService.completeAssignment(aId, driverId),
                "Mora baciti ConflictException ako assignment nije pokrenut");
    }

    @Test
    @DisplayName("12. Direktno ažuriranje statusa assignmenta")
    void testUpdateAssignmentStatusDirectly() {
        AssignmentResponseDTO res = assignmentService.createAssignment(getValidReq());
        // ✅ RELOAD
        entityManager.flush();
        entityManager.clear();
        res = assignmentService.findById(res.getId()).orElseThrow();

        // Postavi status direktno na COMPLETED
        assignmentService.updateStatus(res.getId(), "COMPLETED");

        // Provjeri da su pošiljke također ažurirane
        Shipment shipment = shipmentRepository.findById(shipmentId).orElseThrow();
        assertEquals(ShipmentStatus.DELIVERED, shipment.getStatus(),
                "Status pošiljke mora biti DELIVERED kada je assignment COMPLETED");
    }

    @Test
    @DisplayName("13. Kreiranje assignmenta - Nepostojeće vozilo")
    void testCreateAssignmentInvalidVehicle() {
        AssignmentRequestDTO req = getValidReq();
        req.setVehicleId(9999L);

        assertThrows(ResourceNotFoundException.class,
                () -> assignmentService.createAssignment(req),
                "Mora baciti ResourceNotFoundException za nepostojeće vozilo");
    }


}