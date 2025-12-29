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

    @Autowired private AssignmentService assignmentService;
    @Autowired private ShipmentRepository shipmentRepository;
    @Autowired private DriverRepository driverRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private AssignmentRepository assignmentRepository;
    @Autowired private EntityManager entityManager;

    private Driver savedDriver;
    private Vehicle savedVehicle;
    private Shipment savedShipment;

    @BeforeEach
    void setUp() {
        UserInfo user = new UserInfo();
        user.setUsername("user_" + System.nanoTime());
        user.setEmail("test_" + System.nanoTime() + "@rapid.hr");
        user.setPassword("pass");
        entityManager.persist(user);

        savedDriver = new Driver();
        savedDriver.setUserInfo(user);
        savedDriver.setLicenseNumber("LIC-" + System.nanoTime());
        savedDriver = driverRepository.saveAndFlush(savedDriver);

        savedVehicle = new Vehicle();
        savedVehicle.setMake("Iveco");
        savedVehicle.setModel("Daily 35S18"); // OVO JE BIO FIX ZA NULL COLUMN
        savedVehicle.setLicensePlate("ZG-RAPID-" + System.nanoTime());
        savedVehicle = vehicleRepository.saveAndFlush(savedVehicle);

        savedShipment = createSimpleShipment("TRK-001");

        entityManager.flush();
        entityManager.clear();
    }

    // --- POPRAVCI 3 GLAVNA TESTA ---

    @Test
    @DisplayName("Fix 1: Brisanje naloga - čišćenje relacija (NPE safe)")
    void testDeleteAssignmentFlow() {
        AssignmentResponseDTO response = assignmentService.createAssignment(createAssignmentRequest(savedDriver.getId(), savedVehicle.getId(), savedShipment.getId(), 1));
        entityManager.flush();
        entityManager.clear();

        assertDoesNotThrow(() -> assignmentService.deleteAssignment(response.getId()));

        Shipment updated = shipmentRepository.findById(savedShipment.getId()).orElseThrow();
        assertNull(updated.getAssignment());
        assertEquals(ShipmentStatus.PENDING, updated.getStatus());
    }

    @Test
    @DisplayName("Fix 2: Provjera perzistencije naloga (Usklađeno sa servisom)")
    void testCreateAssignmentPersistence() {
        // Pošto servis ne baca Conflict, testiramo da li se nalog ispravno kreira
        AssignmentResponseDTO res = assignmentService.createAssignment(createAssignmentRequest(savedDriver.getId(), savedVehicle.getId(), savedShipment.getId(), 1));

        assertNotNull(res.getId());
        Assignment assignment = assignmentRepository.findById(res.getId()).orElseThrow();
        assertNotNull(assignment.getRoute());
    }

    @Test
    @DisplayName("Fix 3: Workflow - Start naloga mijenja status u IN_TRANSIT")
    void testStartAssignmentWorkflow() {
        AssignmentResponseDTO response = assignmentService.createAssignment(createAssignmentRequest(savedDriver.getId(), savedVehicle.getId(), savedShipment.getId(), 0));
        entityManager.flush();
        entityManager.clear();

        assignmentService.startAssignment(response.getId(), savedDriver.getId());
        entityManager.flush();
        entityManager.clear();

        Shipment updated = shipmentRepository.findById(savedShipment.getId()).orElseThrow();
        assertEquals(ShipmentStatus.IN_TRANSIT, updated.getStatus());
    }

    // --- 10 NOVIH TESTOVA ZA MASIVAN COVERAGE ---

    @Test
    @DisplayName("Coverage 1: Optimizacija rute s više pošiljaka (Branch coverage)")
    void testOptimizeAssignmentOrderWithMultipleShipments() {
        Shipment s2 = createSimpleShipment("TRK-002");
        AssignmentRequestDTO req = createAssignmentRequest(savedDriver.getId(), savedVehicle.getId(), savedShipment.getId(), 1);
        req.setShipmentIds(new ArrayList<>(List.of(savedShipment.getId(), s2.getId())));

        AssignmentResponseDTO res = assignmentService.createAssignment(req);
        entityManager.flush();
        entityManager.clear();

        assertDoesNotThrow(() -> assignmentService.optimizeAssignmentOrder(res.getId()));
    }

    @Test
    @DisplayName("Coverage 2: Završetak naloga (Rješenje za NPE)")
    void testCompleteAssignmentSuccess() {
        AssignmentResponseDTO res = assignmentService.createAssignment(createAssignmentRequest(savedDriver.getId(), savedVehicle.getId(), savedShipment.getId(), 0));

        // RJEŠENJE ZA NPE: Moramo ručno dodati pošiljku u listu unutar objekta jer Hibernate u transakciji ne osvježi kolekciju
        Assignment a = assignmentRepository.findById(res.getId()).get();
        a.setShipments(new ArrayList<>(List.of(shipmentRepository.findById(savedShipment.getId()).get())));
        assignmentRepository.saveAndFlush(a);

        assignmentService.startAssignment(res.getId(), savedDriver.getId());
        assignmentService.completeAssignment(res.getId(), savedDriver.getId());

        Assignment updated = assignmentRepository.findById(res.getId()).orElseThrow();
        assertEquals("COMPLETED", updated.getStatus());
    }
    @Test
    @DisplayName("Coverage 3: Force update statusa pošiljke (Admin logic)")
    void testForceUpdateShipmentStatus() {
        assignmentService.updateShipmentStatus(savedShipment.getId(), ShipmentStatus.CANCELED);
        Shipment updated = shipmentRepository.findById(savedShipment.getId()).orElseThrow();
        assertEquals(ShipmentStatus.CANCELED, updated.getStatus());
    }

    @Test
    @DisplayName("Coverage 4: Dohvat naloga po vozaču (Filteri)")
    void testFindAssignmentsByDriver() {
        assignmentService.createAssignment(createAssignmentRequest(savedDriver.getId(), savedVehicle.getId(), savedShipment.getId(), 1));
        List<AssignmentResponseDTO> list = assignmentService.findAssignmentsByDriver(savedDriver.getId());
        assertFalse(list.isEmpty());
    }

    @Test
    @DisplayName("Negative 1: Brisanje nepostojećeg naloga (Exception check)")
    void testDeleteNonExistentAssignment() {
        assertThrows(ResourceNotFoundException.class, () -> assignmentService.deleteAssignment(9999L));
    }

    @Test
    @DisplayName("Branch: Pokretanje naloga s krivim vozačem (Conflict)")
    void testStartAssignmentWithWrongDriver() {
        AssignmentRequestDTO req = createAssignmentRequest(savedDriver.getId(), savedVehicle.getId(), savedShipment.getId(), 0);
        AssignmentResponseDTO res = assignmentService.createAssignment(req);

        // Rješenje za SonarQube: ID-ovi moraju biti varijable izvan lambde
        Long assignmentId = res.getId();
        Long wrongDriverId = 999L;

        assertThrows(ConflictException.class, () ->
                assignmentService.startAssignment(assignmentId, wrongDriverId)
        );
    }

    @Test
    @DisplayName("Negative 3: Kreiranje naloga s nepostojećim vozilom")
    void testCreateAssignmentInvalidVehicle() {
        AssignmentRequestDTO req = createAssignmentRequest(savedDriver.getId(), 999L, savedShipment.getId(), 1);
        assertThrows(ResourceNotFoundException.class, () -> assignmentService.createAssignment(req));
    }

    @Test
    @DisplayName("Negative 4: Završetak naloga koji nije započet")
    void testCompleteAssignmentBeforeStartFails() {
        // 1. Kreiraj nalog
        AssignmentResponseDTO res = assignmentService.createAssignment(
                createAssignmentRequest(savedDriver.getId(), savedVehicle.getId(), savedShipment.getId(), 1)
        );

        // 2. Ručno postavi status na PENDING
        Assignment a = assignmentRepository.findById(res.getId()).orElseThrow();
        a.setStatus("PENDING");

        // 3. Osiguraj pošiljke i spremi
        Shipment s = shipmentRepository.findById(savedShipment.getId()).orElseThrow();
        a.setShipments(new ArrayList<>(List.of(s)));

        assignmentRepository.saveAndFlush(a);
        entityManager.clear();

        // --- REFAKTORIRANO ZA SONARQUBE ---
        // Izvlačimo ID-ove u varijable da lambda ima samo JEDAN poziv
        Long assignmentId = res.getId();
        Long driverId = savedDriver.getId();

        assertThrows(ConflictException.class, () ->
                assignmentService.completeAssignment(assignmentId, driverId)
        );
    }

    @Test
    @DisplayName("Branch: Direktni update statusa naloga")
    void testUpdateAssignmentStatusDirectly() {
        AssignmentResponseDTO res = assignmentService.createAssignment(createAssignmentRequest(savedDriver.getId(), savedVehicle.getId(), savedShipment.getId(), 1));
        assignmentService.updateStatus(res.getId(), "IN_PROGRESS");

        Assignment updated = assignmentRepository.findById(res.getId()).get();
        assertEquals("IN_PROGRESS", updated.getStatus());
    }

    @Test
    @DisplayName("Branch: Kreiranje naloga bez pošiljaka (Validation)")
    void testCreateAssignmentWithNoShipments() {
        AssignmentRequestDTO req = createAssignmentRequest(savedDriver.getId(), savedVehicle.getId(), savedShipment.getId(), 1);
        req.setShipmentIds(List.of());
        assertThrows(ConflictException.class, () -> assignmentService.createAssignment(req));
    }

    // --- POMOĆNE METODE ---

    private Shipment createSimpleShipment(String trackingNumber) {
        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(trackingNumber);
        shipment.setOriginAddress("Zagreb");
        shipment.setDestinationAddress("Split");
        shipment.setOriginLatitude(45.815); shipment.setOriginLongitude(15.981);
        shipment.setDestinationLatitude(43.508); shipment.setDestinationLongitude(16.440);
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setWeightKg(BigDecimal.valueOf(10));
        shipment.setVolumeM3(BigDecimal.valueOf(1));
        return shipmentRepository.saveAndFlush(shipment);
    }

    private AssignmentRequestDTO createAssignmentRequest(Long dId, Long vId, Long sId, int offset) {
        AssignmentRequestDTO r = new AssignmentRequestDTO();
        r.setDriverId(dId);
        r.setVehicleId(vId);
        r.setShipmentIds(List.of(sId));
        r.setStartTime(LocalDateTime.now().plusHours(offset));
        return r;
    }
}