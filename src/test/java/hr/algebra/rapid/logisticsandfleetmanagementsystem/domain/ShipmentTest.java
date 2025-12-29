package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ShipmentTest {

    private Shipment shipment;
    private Route route;

    @BeforeEach
    void setUp() {
        route = new Route();
        route.setId(1L);
        shipment = new Shipment();
    }

    @Test
    @DisplayName("1. ID - Get/Set")
    void setAndGetId_ShouldWork() {
        shipment.setId(1L);
        assertThat(shipment.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("2. Tracking Number - Get/Set")
    void setAndGetTrackingNumber_ShouldWork() {
        shipment.setTrackingNumber("SHIP-TEST-001");
        assertThat(shipment.getTrackingNumber()).isEqualTo("SHIP-TEST-001");
    }

    @Test
    @DisplayName("3. Description - Get/Set")
    void setAndGetDescription_ShouldWork() {
        shipment.setDescription("Electronics & Fragile");
        assertThat(shipment.getDescription()).isEqualTo("Electronics & Fragile");
    }

    @Test
    @DisplayName("4. Weight - Get/Set")
    void setAndGetWeightKg_ShouldWork() {
        BigDecimal weight = BigDecimal.valueOf(250.5);
        shipment.setWeightKg(weight);
        assertThat(shipment.getWeightKg()).isEqualByComparingTo(weight);
    }

    @Test
    @DisplayName("5. Volume - Get/Set")
    void setAndGetVolumeM3_ShouldWork() {
        BigDecimal volume = BigDecimal.valueOf(2.5);
        shipment.setVolumeM3(volume);
        assertThat(shipment.getVolumeM3()).isEqualByComparingTo(volume);
    }

    @Test
    @DisplayName("6. Value - Get/Set")
    void setAndGetShipmentValue_ShouldWork() {
        BigDecimal value = BigDecimal.valueOf(15000);
        shipment.setShipmentValue(value);
        assertThat(shipment.getShipmentValue()).isEqualByComparingTo(value);
    }

    @Test
    @DisplayName("7. Status - Get/Set")
    void setAndGetStatus_ShouldWork() {
        shipment.setStatus(ShipmentStatus.PENDING);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PENDING);
    }

    @Test
    @DisplayName("8. Dates - Get/Set")
    void setAndGetDates_ShouldWork() {
        LocalDateTime date = LocalDateTime.of(2025, 2, 1, 10, 0);
        shipment.setExpectedDeliveryDate(date);
        shipment.setActualDeliveryDate(date);
        assertAll(
                () -> assertThat(shipment.getExpectedDeliveryDate()).isEqualTo(date),
                () -> assertThat(shipment.getActualDeliveryDate()).isEqualTo(date)
        );
    }

    @Test
    @DisplayName("9. Route & Assignment - Relacije")
    void setAndGetRelations_ShouldWork() {
        Assignment assignment = new Assignment();
        assignment.setId(99L);

        shipment.setRoute(route);
        shipment.setAssignment(assignment);

        assertAll(
                () -> assertThat(shipment.getRoute()).isEqualTo(route),
                () -> assertThat(shipment.getAssignment()).isEqualTo(assignment),
                () -> assertThat(shipment.getAssignment().getId()).isEqualTo(99L)
        );
    }

    @Test
    @DisplayName("10. Addresses & Coordinates - Get/Set")
    void setAndGetLocationData_ShouldWork() {
        shipment.setOriginAddress("Zagreb");
        shipment.setDestinationAddress("Split");
        shipment.setOriginLatitude(45.815);
        shipment.setOriginLongitude(15.981);
        shipment.setDestinationLatitude(43.508);
        shipment.setDestinationLongitude(16.440);

        assertAll(
                () -> assertThat(shipment.getOriginAddress()).isEqualTo("Zagreb"),
                () -> assertThat(shipment.getDestinationAddress()).isEqualTo("Split"),
                () -> assertThat(shipment.getOriginLatitude()).isEqualTo(45.815),
                () -> assertThat(shipment.getDestinationLongitude()).isEqualTo(16.440)
        );
    }

    @Test
    @DisplayName("11. Sequence - Get/Set")
    void setAndGetDeliverySequence_ShouldWork() {
        shipment.setDeliverySequence(5);
        assertThat(shipment.getDeliverySequence()).isEqualTo(5);
    }

    @Test
    @DisplayName("12. Lombok Special (toString, equals, hashCode)")
    void testLombokMethods() {
        shipment.setTrackingNumber("SHIP-LOMBOK");
        shipment.setId(1L);

        // toString coverage
        String toStringResult = shipment.toString();

        // equals & hashCode coverage
        Shipment sameIdShipment = new Shipment();
        sameIdShipment.setId(1L);

        assertAll(
                () -> assertThat(toStringResult).contains("trackingNumber=SHIP-LOMBOK"),
                () -> assertThat(shipment.hashCode()).isNotZero(),
                () -> assertThat(shipment).isNotNull(),
                () -> assertThat(shipment).isNotEqualTo(new Object())
        );
    }
}