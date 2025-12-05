package hr.algebra.rapid.logisticsandfleetmanagementsystem.integration;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Vehicle;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleAnalyticsResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.DriverRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRoleRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.VehicleRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AnalyticsService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * INTEGRACIJSKI TEST - Vehicle Maintenance Analytics
 * 
 * Testira:
 * 1. Vehicle maintenance alert detection (overdue, warning, OK)
 * 2. remainingKmToService calculation
 * 3. Free vehicle detection
 * 4. Analytics aggregation
 * 5. Real-time status updates
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class VehicleMaintenanceAnalyticsIntegrationTest {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private Driver testDriver;

    @BeforeEach
    void setUp() {
        // Setup driver
        UserRole driverRole = userRoleRepository.findByName("ROLE_DRIVER")
                .orElseGet(() -> {
                    UserRole role = new UserRole();
                    role.setName("ROLE_DRIVER");
                    return userRoleRepository.save(role);
                });

        UserInfo userInfo = new UserInfo();
        userInfo.setUsername("maintenance_driver");
        userInfo.setPassword("hashedPassword");
        userInfo.setFirstName("Maintenance");
        userInfo.setLastName("Driver");
        userInfo.setEmail("maintenance@test.com");
        userInfo.setIsEnabled(true);
        userInfo.setRoles(List.of(driverRole));
        userInfo = userRepository.save(userInfo);

        testDriver = new Driver();
        testDriver.setUserInfo(userInfo);
        testDriver.setLicenseNumber("MAINT-001");
        testDriver.setPhoneNumber("+385991234567");
        testDriver = driverRepository.save(testDriver);
    }

    // ==========================================
    // MAINTENANCE STATUS DETECTION TESTS
    // ==========================================

    @Test
    void testMaintenanceAnalytics_OverdueVehicle() {
        // Arrange - Vehicle past service deadline
        Vehicle overdue = new Vehicle();
        overdue.setLicensePlate("ZG-OVERDUE-001");
        overdue.setMake("Mercedes");
        overdue.setModel("Sprinter");
        overdue.setYear(2020);
        overdue.setFuelType("Diesel");
        overdue.setLoadCapacityKg(BigDecimal.valueOf(1000));
        overdue.setCurrentMileageKm(57000L); // Current
        overdue.setNextServiceMileageKm(55000L); // Should have been at 55k
        overdue.setLastServiceDate(LocalDate.now().minusMonths(6));
        overdue.setFuelConsumptionLitersPer100Km(BigDecimal.valueOf(8.5));

        vehicleRepository.save(overdue);

        // Act
        VehicleAnalyticsResponse analytics = analyticsService.getVehicleAlertStatus();

        // Assert
        assertEquals(1L, analytics.getOverdue());
        assertEquals(0L, analytics.getWarning());
        assertEquals(1L, analytics.getFree()); // No driver assigned
        assertEquals(1L, analytics.getTotal());

        // Verify individual vehicle
        List<VehicleResponse> overdueVehicles = vehicleService.findOverdueMaintenanceVehicles();
        assertEquals(1, overdueVehicles.size());
        
        VehicleResponse vehicle = overdueVehicles.get(0);
        assertEquals("ZG-OVERDUE-001", vehicle.getLicensePlate());
        assertEquals(-2000L, vehicle.getRemainingKmToService()); // 55000 - 57000 = -2000
    }

    @Test
    void testMaintenanceAnalytics_WarningVehicle() {
        // Arrange - Vehicle within warning threshold (0-5000 km)
        Vehicle warning = new Vehicle();
        warning.setLicensePlate("ZG-WARNING-001");
        warning.setMake("Volkswagen");
        warning.setModel("Transporter");
        warning.setYear(2021);
        warning.setFuelType("Diesel");
        warning.setLoadCapacityKg(BigDecimal.valueOf(800));
        warning.setCurrentMileageKm(52000L); // Current
        warning.setNextServiceMileageKm(55000L); // 3000 km remaining
        warning.setLastServiceDate(LocalDate.now().minusMonths(3));
        warning.setFuelConsumptionLitersPer100Km(BigDecimal.valueOf(7.5));

        vehicleRepository.save(warning);

        // Act
        VehicleAnalyticsResponse analytics = analyticsService.getVehicleAlertStatus();

        // Assert
        assertEquals(0L, analytics.getOverdue());
        assertEquals(1L, analytics.getWarning());
        assertEquals(1L, analytics.getFree());
        assertEquals(1L, analytics.getTotal());

        // Verify individual vehicle
        List<VehicleResponse> warningVehicles = vehicleService.findWarningMaintenanceVehicles(5000L);
        assertEquals(1, warningVehicles.size());
        
        VehicleResponse vehicle = warningVehicles.get(0);
        assertEquals("ZG-WARNING-001", vehicle.getLicensePlate());
        assertEquals(3000L, vehicle.getRemainingKmToService()); // 55000 - 52000 = 3000
    }

    @Test
    void testMaintenanceAnalytics_HealthyVehicle() {
        // Arrange - Vehicle with plenty of km until service
        Vehicle healthy = new Vehicle();
        healthy.setLicensePlate("ZG-HEALTHY-001");
        healthy.setMake("Ford");
        healthy.setModel("Transit");
        healthy.setYear(2022);
        healthy.setFuelType("Diesel");
        healthy.setLoadCapacityKg(BigDecimal.valueOf(1200));
        healthy.setCurrentMileageKm(45000L); // Current
        healthy.setNextServiceMileageKm(55000L); // 10000 km remaining
        healthy.setLastServiceDate(LocalDate.now().minusMonths(1));
        healthy.setFuelConsumptionLitersPer100Km(BigDecimal.valueOf(9.0));

        vehicleRepository.save(healthy);

        // Act
        VehicleAnalyticsResponse analytics = analyticsService.getVehicleAlertStatus();

        // Assert
        assertEquals(0L, analytics.getOverdue());
        assertEquals(0L, analytics.getWarning()); // > 5000 km, not in warning
        assertEquals(1L, analytics.getFree());
        assertEquals(1L, analytics.getTotal());

        // Verify NOT in overdue list
        List<VehicleResponse> overdueVehicles = vehicleService.findOverdueMaintenanceVehicles();
        assertEquals(0, overdueVehicles.size());

        // Verify NOT in warning list
        List<VehicleResponse> warningVehicles = vehicleService.findWarningMaintenanceVehicles(5000L);
        assertEquals(0, warningVehicles.size());
    }

    @Test
    void testMaintenanceAnalytics_MultipleStatuses() {
        // Arrange - Create vehicles in all states
        Vehicle overdue = createVehicle("OVERDUE", 57000L, 55000L);
        Vehicle warning1 = createVehicle("WARNING1", 52000L, 55000L);
        Vehicle warning2 = createVehicle("WARNING2", 54000L, 55000L);
        Vehicle healthy1 = createVehicle("HEALTHY1", 45000L, 55000L);
        Vehicle healthy2 = createVehicle("HEALTHY2", 40000L, 55000L);

        vehicleRepository.saveAll(List.of(overdue, warning1, warning2, healthy1, healthy2));

        // Act
        VehicleAnalyticsResponse analytics = analyticsService.getVehicleAlertStatus();

        // Assert
        assertEquals(1L, analytics.getOverdue());
        assertEquals(2L, analytics.getWarning());
        assertEquals(5L, analytics.getFree()); // All unassigned
        assertEquals(5L, analytics.getTotal());
    }

    // ==========================================
    // FREE VEHICLE DETECTION TESTS
    // ==========================================

    @Test
    void testFreeVehicles_AllUnassigned() {
        // Arrange - Create 3 vehicles, none assigned
        Vehicle v1 = createVehicle("FREE1", 50000L, 55000L);
        Vehicle v2 = createVehicle("FREE2", 51000L, 56000L);
        Vehicle v3 = createVehicle("FREE3", 52000L, 57000L);

        vehicleRepository.saveAll(List.of(v1, v2, v3));

        // Act
        Long freeCount = vehicleService.countFreeVehicles();
        List<VehicleResponse> freeVehicles = vehicleService.findFreeVehiclesDetails();

        // Assert
        assertEquals(3L, freeCount);
        assertEquals(3, freeVehicles.size());

        // All should have null driver
        assertTrue(freeVehicles.stream().allMatch(v -> v.getCurrentDriver() == null));
    }

    @Test
    void testFreeVehicles_OneAssigned() {
        // Arrange - Create 3 vehicles, assign 1
        Vehicle v1 = createVehicle("ASSIGNED", 50000L, 55000L);
        v1.setCurrentDriver(testDriver);
        
        Vehicle v2 = createVehicle("FREE1", 51000L, 56000L);
        Vehicle v3 = createVehicle("FREE2", 52000L, 57000L);

        vehicleRepository.saveAll(List.of(v1, v2, v3));

        // Act
        Long freeCount = vehicleService.countFreeVehicles();
        List<VehicleResponse> freeVehicles = vehicleService.findFreeVehiclesDetails();

        // Assert
        assertEquals(2L, freeCount); // Only 2 free
        assertEquals(2, freeVehicles.size());

        // Verify assigned vehicle is not in free list
        assertFalse(freeVehicles.stream()
                .anyMatch(v -> v.getLicensePlate().equals("ZG-ASSIGNED-001")));
    }

    @Test
    void testFreeVehicles_NoneAvailable() {
        // Arrange - Create 2 vehicles, assign both
        Vehicle v1 = createVehicle("BUSY1", 50000L, 55000L);
        v1.setCurrentDriver(testDriver);
        
        Vehicle v2 = createVehicle("BUSY2", 51000L, 56000L);
        v2.setCurrentDriver(testDriver); // Same driver, multiple vehicles

        vehicleRepository.saveAll(List.of(v1, v2));

        // Act
        Long freeCount = vehicleService.countFreeVehicles();
        List<VehicleResponse> freeVehicles = vehicleService.findFreeVehiclesDetails();

        // Assert
        assertEquals(0L, freeCount);
        assertEquals(0, freeVehicles.size());
    }

    // ==========================================
    // REMAINING KM CALCULATION TESTS
    // ==========================================

    @Test
    void testRemainingKmCalculation_Positive() {
        // Arrange
        Vehicle vehicle = createVehicle("POS", 45000L, 55000L);
        vehicleRepository.save(vehicle);

        // Act
        VehicleResponse response = vehicleService.findVehicleById(vehicle.getId()).orElseThrow();

        // Assert
        assertEquals(10000L, response.getRemainingKmToService());
    }

    @Test
    void testRemainingKmCalculation_Negative() {
        // Arrange
        Vehicle vehicle = createVehicle("NEG", 57000L, 55000L);
        vehicleRepository.save(vehicle);

        // Act
        VehicleResponse response = vehicleService.findVehicleById(vehicle.getId()).orElseThrow();

        // Assert
        assertEquals(-2000L, response.getRemainingKmToService());
    }

    @Test
    void testRemainingKmCalculation_Zero() {
        // Arrange
        Vehicle vehicle = createVehicle("ZERO", 55000L, 55000L);
        vehicleRepository.save(vehicle);

        // Act
        VehicleResponse response = vehicleService.findVehicleById(vehicle.getId()).orElseThrow();

        // Assert
        assertEquals(0L, response.getRemainingKmToService());
    }

    @Test
    void testRemainingKmCalculation_ExactlyWarningThreshold() {
        // Arrange - Exactly 5000 km remaining
        Vehicle vehicle = createVehicle("THRESH", 50000L, 55000L);
        vehicleRepository.save(vehicle);

        // Act
        List<VehicleResponse> warningVehicles = vehicleService.findWarningMaintenanceVehicles(5000L);

        // Assert - Should be included in warning (≤ 5000)
        assertEquals(1, warningVehicles.size());
        assertEquals(5000L, warningVehicles.get(0).getRemainingKmToService());
    }

    // ==========================================
    // DYNAMIC THRESHOLD TESTS
    // ==========================================

    @Test
    void testWarningThreshold_DifferentValues() {
        // Arrange
        Vehicle v1 = createVehicle("V1", 48000L, 55000L); // 7000 km remaining
        Vehicle v2 = createVehicle("V2", 52000L, 55000L); // 3000 km remaining
        Vehicle v3 = createVehicle("V3", 54500L, 55000L); // 500 km remaining

        vehicleRepository.saveAll(List.of(v1, v2, v3));

        // Act & Assert - Threshold 5000 km
        List<VehicleResponse> threshold5k = vehicleService.findWarningMaintenanceVehicles(5000L);
        assertEquals(2, threshold5k.size()); // v2, v3

        // Act & Assert - Threshold 1000 km
        List<VehicleResponse> threshold1k = vehicleService.findWarningMaintenanceVehicles(1000L);
        assertEquals(1, threshold1k.size()); // only v3

        // Act & Assert - Threshold 10000 km
        List<VehicleResponse> threshold10k = vehicleService.findWarningMaintenanceVehicles(10000L);
        assertEquals(3, threshold10k.size()); // all 3
    }

    // ==========================================
    // REAL-TIME STATUS UPDATE TESTS
    // ==========================================

    @Test
    void testMaintenanceStatus_AfterMileageUpdate() {
        // Arrange - Start with healthy vehicle
        Vehicle vehicle = createVehicle("UPDATE", 50000L, 55000L);
        vehicle = vehicleRepository.save(vehicle);

        // Initial state
        VehicleAnalyticsResponse initial = analyticsService.getVehicleAlertStatus();
        assertEquals(0L, initial.getOverdue());
        assertEquals(0L, initial.getWarning());

        // Act - Update mileage to warning zone
        vehicle.setCurrentMileageKm(53000L); // Now only 2000 km remaining
        vehicleRepository.save(vehicle);

        // Assert - Should now be in warning
        VehicleAnalyticsResponse afterUpdate = analyticsService.getVehicleAlertStatus();
        assertEquals(0L, afterUpdate.getOverdue());
        assertEquals(1L, afterUpdate.getWarning());

        // Act - Update mileage past service
        vehicle.setCurrentMileageKm(56000L); // Now -1000 km (overdue)
        vehicleRepository.save(vehicle);

        // Assert - Should now be overdue
        VehicleAnalyticsResponse afterOverdue = analyticsService.getVehicleAlertStatus();
        assertEquals(1L, afterOverdue.getOverdue());
        assertEquals(0L, afterOverdue.getWarning());
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private Vehicle createVehicle(String identifier, Long currentKm, Long nextServiceKm) {
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate("ZG-" + identifier + "-001");
        vehicle.setMake("Test Make");
        vehicle.setModel("Test Model");
        vehicle.setYear(2020);
        vehicle.setFuelType("Diesel");
        vehicle.setLoadCapacityKg(BigDecimal.valueOf(1000));
        vehicle.setCurrentMileageKm(currentKm);
        vehicle.setNextServiceMileageKm(nextServiceKm);
        vehicle.setLastServiceDate(LocalDate.now().minusMonths(2));
        vehicle.setFuelConsumptionLitersPer100Km(BigDecimal.valueOf(8.0));
        return vehicle;
    }
}
