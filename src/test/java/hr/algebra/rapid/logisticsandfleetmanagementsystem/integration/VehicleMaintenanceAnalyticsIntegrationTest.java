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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ✅ FIXED INTEGRACIJSKI TEST - Vehicle Maintenance Analytics
 *
 * IZMJENE:
 * - Maknut @Transactional s klase
 * - Dodana @Transactional na setUp() i svaki test
 * - Fixed List.of() -> ArrayList (immutable list problem)
 * - Dodani detaljniji assert messages
 */
@SpringBootTest
@ActiveProfiles("test")
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
    @Transactional
    void setUp() {
        // Setup driver
        UserRole driverRole = userRoleRepository.findByName("ROLE_DRIVER")
                .orElseGet(() -> {
                    UserRole role = new UserRole();
                    role.setName("ROLE_DRIVER");
                    return userRoleRepository.save(role);
                });

        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("maintenance_driver");
        userInfo.setPassword("$2a$10$hashedPassword");
        userInfo.setFirstName("Maintenance");
        userInfo.setLastName("Driver");
        userInfo.setEmail("maintenance@test.com");
        userInfo.setIsEnabled(true);

        // ✅ FIX: Koristi ArrayList, ne List.of() (immutable problem)
        List<UserRole> rolesList = new ArrayList<>();
        rolesList.add(driverRole);
        userInfo.setRoles(rolesList);

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
    @Transactional
    void testMaintenanceAnalytics_OverdueVehicle() {
        // Arrange - Vehicle past service deadline
        Vehicle overdue = new Vehicle();
        overdue.setLicensePlate("ZG-OVERDUE-001");
        overdue.setMake("Mercedes");
        overdue.setModel("Sprinter");
        overdue.setYear(2020);
        overdue.setFuelType("Diesel");
        overdue.setLoadCapacityKg(BigDecimal.valueOf(1000));
        overdue.setCurrentMileageKm(57000L);
        overdue.setNextServiceMileageKm(55000L);
        overdue.setLastServiceDate(LocalDate.now().minusMonths(6));
        overdue.setFuelConsumptionLitersPer100Km(BigDecimal.valueOf(8.5));

        vehicleRepository.save(overdue);

        // Act
        VehicleAnalyticsResponse analytics = analyticsService.getVehicleAlertStatus();

        // Assert
        assertEquals(1L, analytics.getOverdue(), "Should have 1 overdue vehicle");
        assertEquals(0L, analytics.getWarning(), "Should have 0 warning vehicles");
        assertEquals(1L, analytics.getFree(), "Should have 1 free vehicle");
        assertEquals(1L, analytics.getTotal(), "Should have 1 total vehicle");

        // Verify individual vehicle
        List<VehicleResponse> overdueVehicles = vehicleService.findOverdueMaintenanceVehicles();
        assertEquals(1, overdueVehicles.size(), "Should find 1 overdue vehicle");

        VehicleResponse vehicle = overdueVehicles.get(0);
        assertEquals("ZG-OVERDUE-001", vehicle.getLicensePlate(), "License plate should match");
        assertEquals(-2000L, vehicle.getRemainingKmToService(),
                "Remaining km should be -2000 (55000 - 57000)");
    }

    @Test
    @Transactional
    void testMaintenanceAnalytics_WarningVehicle() {
        // Arrange - Vehicle within warning threshold (0-5000 km)
        Vehicle warning = new Vehicle();
        warning.setLicensePlate("ZG-WARNING-001");
        warning.setMake("Volkswagen");
        warning.setModel("Transporter");
        warning.setYear(2021);
        warning.setFuelType("Diesel");
        warning.setLoadCapacityKg(BigDecimal.valueOf(800));
        warning.setCurrentMileageKm(52000L);
        warning.setNextServiceMileageKm(55000L);
        warning.setLastServiceDate(LocalDate.now().minusMonths(3));
        warning.setFuelConsumptionLitersPer100Km(BigDecimal.valueOf(7.5));

        vehicleRepository.save(warning);

        // Act
        VehicleAnalyticsResponse analytics = analyticsService.getVehicleAlertStatus();

        // Assert
        assertEquals(0L, analytics.getOverdue(), "Should have 0 overdue vehicles");
        assertEquals(1L, analytics.getWarning(), "Should have 1 warning vehicle");
        assertEquals(1L, analytics.getFree(), "Should have 1 free vehicle");
        assertEquals(1L, analytics.getTotal(), "Should have 1 total vehicle");

        // Verify individual vehicle
        List<VehicleResponse> warningVehicles = vehicleService.findWarningMaintenanceVehicles(5000L);
        assertEquals(1, warningVehicles.size(), "Should find 1 warning vehicle");

        VehicleResponse vehicle = warningVehicles.get(0);
        assertEquals("ZG-WARNING-001", vehicle.getLicensePlate(), "License plate should match");
        assertEquals(3000L, vehicle.getRemainingKmToService(),
                "Remaining km should be 3000 (55000 - 52000)");
    }

    @Test
    @Transactional
    void testMaintenanceAnalytics_HealthyVehicle() {
        // Arrange - Vehicle with plenty of km until service
        Vehicle healthy = new Vehicle();
        healthy.setLicensePlate("ZG-HEALTHY-001");
        healthy.setMake("Ford");
        healthy.setModel("Transit");
        healthy.setYear(2022);
        healthy.setFuelType("Diesel");
        healthy.setLoadCapacityKg(BigDecimal.valueOf(1200));
        healthy.setCurrentMileageKm(45000L);
        healthy.setNextServiceMileageKm(55000L);
        healthy.setLastServiceDate(LocalDate.now().minusMonths(1));
        healthy.setFuelConsumptionLitersPer100Km(BigDecimal.valueOf(9.0));

        vehicleRepository.save(healthy);

        // Act
        VehicleAnalyticsResponse analytics = analyticsService.getVehicleAlertStatus();

        // Assert
        assertEquals(0L, analytics.getOverdue(), "Should have 0 overdue vehicles");
        assertEquals(0L, analytics.getWarning(), "Should have 0 warning vehicles (> 5000 km)");
        assertEquals(1L, analytics.getFree(), "Should have 1 free vehicle");
        assertEquals(1L, analytics.getTotal(), "Should have 1 total vehicle");

        // Verify NOT in overdue list
        List<VehicleResponse> overdueVehicles = vehicleService.findOverdueMaintenanceVehicles();
        assertEquals(0, overdueVehicles.size(), "Should have no overdue vehicles");

        // Verify NOT in warning list
        List<VehicleResponse> warningVehicles = vehicleService.findWarningMaintenanceVehicles(5000L);
        assertEquals(0, warningVehicles.size(), "Should have no warning vehicles");
    }

    @Test
    @Transactional
    void testMaintenanceAnalytics_MixedFleet() {
        // Arrange - Create multiple vehicles with different statuses
        Vehicle overdue = createVehicle("ZG-OVER-001", 57000L, 55000L);
        Vehicle warning = createVehicle("ZG-WARN-001", 52000L, 55000L);
        Vehicle healthy = createVehicle("ZG-HLTH-001", 45000L, 55000L);

        vehicleRepository.saveAll(List.of(overdue, warning, healthy));

        // Act
        VehicleAnalyticsResponse analytics = analyticsService.getVehicleAlertStatus();

        // Assert
        assertEquals(1L, analytics.getOverdue(), "Should have 1 overdue vehicle");
        assertEquals(1L, analytics.getWarning(), "Should have 1 warning vehicle");
        assertEquals(3L, analytics.getFree(), "Should have 3 free vehicles");
        assertEquals(3L, analytics.getTotal(), "Should have 3 total vehicles");
    }

    @Test
    @Transactional
    void testRemainingKmCalculation_Positive() {
        // Arrange - Vehicle with 8000 km remaining
        Vehicle vehicle = createVehicle("ZG-TEST-001", 47000L, 55000L);
        vehicleRepository.save(vehicle);

        // Act
        VehicleResponse response = vehicleService.findVehicleById(vehicle.getId()).orElseThrow();

        // Assert
        assertEquals(8000L, response.getRemainingKmToService(),
                "Remaining km should be 8000 (55000 - 47000)");
    }

    @Test
    @Transactional
    void testRemainingKmCalculation_Negative() {
        // Arrange - Vehicle overdue by 3000 km
        Vehicle vehicle = createVehicle("ZG-TEST-002", 58000L, 55000L);
        vehicleRepository.save(vehicle);

        // Act
        VehicleResponse response = vehicleService.findVehicleById(vehicle.getId()).orElseThrow();

        // Assert
        assertEquals(-3000L, response.getRemainingKmToService(),
                "Remaining km should be -3000 (55000 - 58000)");
    }

    @Test
    @Transactional
    void testRemainingKmCalculation_ExactlyDue() {
        // Arrange - Vehicle at exact service mileage
        Vehicle vehicle = createVehicle("ZG-TEST-003", 55000L, 55000L);
        vehicleRepository.save(vehicle);

        // Act
        VehicleResponse response = vehicleService.findVehicleById(vehicle.getId()).orElseThrow();

        // Assert
        assertEquals(0L, response.getRemainingKmToService(),
                "Remaining km should be 0 (service due now)");
    }

    @Test
    @Transactional
    void testFreeVehicleDetection_NoDriver() {
        // Arrange - Vehicle without driver
        Vehicle freeVehicle = createVehicle("ZG-FREE-001", 45000L, 55000L);
        vehicleRepository.save(freeVehicle);

        // Act
        VehicleAnalyticsResponse analytics = analyticsService.getVehicleAlertStatus();

        // Assert
        assertEquals(1L, analytics.getFree(), "Should have 1 free vehicle (no driver assigned)");
    }

    @Test
    @Transactional
    void testOverdueMaintenanceList() {
        // Arrange - Create 3 overdue vehicles
        Vehicle overdue1 = createVehicle("ZG-OVER-001", 57000L, 55000L);
        Vehicle overdue2 = createVehicle("ZG-OVER-002", 60000L, 55000L);
        Vehicle overdue3 = createVehicle("ZG-OVER-003", 58000L, 55000L);

        vehicleRepository.saveAll(List.of(overdue1, overdue2, overdue3));

        // Act
        List<VehicleResponse> overdueList = vehicleService.findOverdueMaintenanceVehicles();

        // Assert
        assertEquals(3, overdueList.size(), "Should find 3 overdue vehicles");
        assertTrue(overdueList.stream()
                        .allMatch(v -> v.getRemainingKmToService() < 0),
                "All vehicles should have negative remaining km");
    }

    @Test
    @Transactional
    void testWarningMaintenanceList_WithThreshold() {
        // Arrange - Vehicles at different warning levels
        Vehicle warn1 = createVehicle("ZG-WARN-001", 54000L, 55000L); // 1000 km
        Vehicle warn2 = createVehicle("ZG-WARN-002", 52000L, 55000L); // 3000 km
        Vehicle warn3 = createVehicle("ZG-WARN-003", 50500L, 55000L); // 4500 km
        Vehicle healthy = createVehicle("ZG-HLTH-001", 45000L, 55000L); // 10000 km

        vehicleRepository.saveAll(List.of(warn1, warn2, warn3, healthy));

        // Act
        List<VehicleResponse> warningList = vehicleService.findWarningMaintenanceVehicles(5000L);

        // Assert
        assertEquals(3, warningList.size(),
                "Should find 3 vehicles within 5000 km warning threshold");
        assertTrue(warningList.stream()
                        .allMatch(v -> v.getRemainingKmToService() > 0 && v.getRemainingKmToService() <= 5000),
                "All vehicles should be in warning range (0-5000 km)");
    }

    @Test
    @Transactional
    void testEmptyFleet() {
        // Act - No vehicles in database
        VehicleAnalyticsResponse analytics = analyticsService.getVehicleAlertStatus();

        // Assert
        assertEquals(0L, analytics.getOverdue(), "Should have 0 overdue vehicles");
        assertEquals(0L, analytics.getWarning(), "Should have 0 warning vehicles");
        assertEquals(0L, analytics.getFree(), "Should have 0 free vehicles");
        assertEquals(0L, analytics.getTotal(), "Should have 0 total vehicles");
    }

    @Test
    @Transactional
    void testMaintenanceUpdate_StatusChange() {
        // Arrange - Create vehicle in healthy state
        Vehicle vehicle = createVehicle("ZG-UPDATE-001", 45000L, 55000L);
        vehicle = vehicleRepository.save(vehicle);

        // Verify initial state
        VehicleAnalyticsResponse initialAnalytics = analyticsService.getVehicleAlertStatus();
        assertEquals(0L, initialAnalytics.getOverdue(), "Initially should have 0 overdue");

        // Act - Simulate mileage increase to overdue
        vehicle.setCurrentMileageKm(58000L); // Now overdue
        vehicleRepository.save(vehicle);

        // Assert - Status should update
        VehicleAnalyticsResponse updatedAnalytics = analyticsService.getVehicleAlertStatus();
        assertEquals(1L, updatedAnalytics.getOverdue(), "Should now have 1 overdue vehicle");
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private Vehicle createVehicle(String licensePlate, Long currentMileage, Long nextServiceMileage) {
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(licensePlate);
        vehicle.setMake("Test Make");
        vehicle.setModel("Test Model");
        vehicle.setYear(2022);
        vehicle.setFuelType("Diesel");
        vehicle.setLoadCapacityKg(BigDecimal.valueOf(1000));
        vehicle.setCurrentMileageKm(currentMileage);
        vehicle.setNextServiceMileageKm(nextServiceMileage);
        vehicle.setLastServiceDate(LocalDate.now().minusMonths(3));
        vehicle.setFuelConsumptionLitersPer100Km(BigDecimal.valueOf(8.0));
        return vehicle;
    }
}