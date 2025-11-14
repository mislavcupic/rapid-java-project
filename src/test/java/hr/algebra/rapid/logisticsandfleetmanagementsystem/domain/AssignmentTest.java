package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit testovi za Assignment domain entitet
 * Testira getters/setters i osnovne funkcionalnosti entiteta
 */
class AssignmentTest {

    private Assignment assignment;
    private Driver driver;
    private Vehicle vehicle;
    private Shipment shipment;
    private Route route;

    @BeforeEach
    void setUp() {
        // Arrange - kreiranje test objekata
        driver = new Driver();
        driver.setId(1L);

        vehicle = new Vehicle();
        vehicle.setId(2L);

        shipment = new Shipment();
        shipment.setId(3L);

        route = new Route();
        route.setId(4L);

        assignment = new Assignment();
    }

    @Test
    void getId_ShouldReturnId() {
        // Arrange
        assignment.setId(1L);

        // Act & Assert
        assertThat(assignment.getId()).isEqualTo(1L);
    }

    @Test
    void setAndGetDriver_ShouldWorkCorrectly() {
        // Act
        assignment.setDriver(driver);

        // Assert
        assertThat(assignment.getDriver()).isEqualTo(driver);
        assertThat(assignment.getDriver().getId()).isEqualTo(1L);
    }

    @Test
    void setAndGetVehicle_ShouldWorkCorrectly() {
        // Act
        assignment.setVehicle(vehicle);

        // Assert
        assertThat(assignment.getVehicle()).isEqualTo(vehicle);
        assertThat(assignment.getVehicle().getId()).isEqualTo(2L);
    }

    @Test
    void setAndGetShipment_ShouldWorkCorrectly() {
        // Act
        assignment.setShipment(shipment);

        // Assert
        assertThat(assignment.getShipment()).isEqualTo(shipment);
        assertThat(assignment.getShipment().getId()).isEqualTo(3L);
    }

    @Test
    void setAndGetStartTime_ShouldWorkCorrectly() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2025, 1, 15, 8, 0);

        // Act
        assignment.setStartTime(startTime);

        // Assert
        assertThat(assignment.getStartTime()).isEqualTo(startTime);
    }

    @Test
    void setAndGetEndTime_ShouldWorkCorrectly() {
        // Arrange
        LocalDateTime endTime = LocalDateTime.of(2025, 1, 15, 17, 0);

        // Act
        assignment.setEndTime(endTime);

        // Assert
        assertThat(assignment.getEndTime()).isEqualTo(endTime);
    }

    @Test
    void setAndGetStatus_ShouldWorkCorrectly() {
        // Arrange
        String status = "ACTIVE";

        // Act
        assignment.setStatus(status);

        // Assert
        assertThat(assignment.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void setAndGetRoute_ShouldWorkCorrectly() {
        // Act
        assignment.setRoute(route);

        // Assert
        assertThat(assignment.getRoute()).isEqualTo(route);
        assertThat(assignment.getRoute().getId()).isEqualTo(4L);
    }

    @Test
    void assignmentWithAllFields_ShouldBeCreatedCorrectly() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2025, 1, 15, 8, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 1, 15, 17, 0);

        // Act
        assignment.setId(100L);
        assignment.setDriver(driver);
        assignment.setVehicle(vehicle);
        assignment.setShipment(shipment);
        assignment.setStartTime(startTime);
        assignment.setEndTime(endTime);
        assignment.setStatus("COMPLETED");
        assignment.setRoute(route);

        // Assert
        assertThat(assignment.getId()).isEqualTo(100L);
        assertThat(assignment.getDriver()).isEqualTo(driver);
        assertThat(assignment.getVehicle()).isEqualTo(vehicle);
        assertThat(assignment.getShipment()).isEqualTo(shipment);
        assertThat(assignment.getStartTime()).isEqualTo(startTime);
        assertThat(assignment.getEndTime()).isEqualTo(endTime);
        assertThat(assignment.getStatus()).isEqualTo("COMPLETED");
        assertThat(assignment.getRoute()).isEqualTo(route);
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyAssignment() {
        // Act
        Assignment emptyAssignment = new Assignment();

        // Assert
        assertThat(emptyAssignment).isNotNull();
        assertThat(emptyAssignment.getId()).isNull();
        assertThat(emptyAssignment.getDriver()).isNull();
        assertThat(emptyAssignment.getVehicle()).isNull();
        assertThat(emptyAssignment.getShipment()).isNull();
    }

    @Test
    void allArgsConstructor_ShouldCreateFullAssignment() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2025, 1, 15, 8, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 1, 15, 17, 0);

        // Act
        Assignment fullAssignment = new Assignment(
            1L, driver, vehicle, shipment, startTime, endTime, "ACTIVE", route
        );

        // Assert
        assertThat(fullAssignment.getId()).isEqualTo(1L);
        assertThat(fullAssignment.getDriver()).isEqualTo(driver);
        assertThat(fullAssignment.getVehicle()).isEqualTo(vehicle);
        assertThat(fullAssignment.getShipment()).isEqualTo(shipment);
        assertThat(fullAssignment.getStartTime()).isEqualTo(startTime);
        assertThat(fullAssignment.getEndTime()).isEqualTo(endTime);
        assertThat(fullAssignment.getStatus()).isEqualTo("ACTIVE");
        assertThat(fullAssignment.getRoute()).isEqualTo(route);
    }

    @Test
    void toString_ShouldReturnStringRepresentation() {
        // Arrange
        assignment.setId(1L);
        assignment.setStatus("ACTIVE");

        // Act
        String result = assignment.toString();

        // Assert
        assertThat(result)
                .contains("ASSIGNMENT")
                .contains("id=1")
                .contains("status=ACTIVE");
    }

    @Test
    void equals_ShouldCompareById() {
        // Arrange
        Assignment assignment1 = new Assignment();
        assignment1.setId(1L);

        Assignment assignment2 = new Assignment();
        assignment2.setId(1L);

        Assignment assignment3 = new Assignment();
        assignment3.setId(2L);

        // Act & Assert
        assertThat(assignment1).isEqualTo(assignment2).isNotEqualTo(assignment3);
    }

    @Test
    void hashCode_ShouldBeConsistentWithEquals() {
        // Arrange
        Assignment assignment1 = new Assignment();
        assignment1.setId(1L);

        Assignment assignment2 = new Assignment();
        assignment2.setId(1L);

        // Act & Assert
        assertThat(assignment1.hashCode()).hasSameHashCodeAs(assignment2.hashCode());
    }
}
