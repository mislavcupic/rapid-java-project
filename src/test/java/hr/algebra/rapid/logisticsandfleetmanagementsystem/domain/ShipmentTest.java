package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

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
    void setAndGetId_ShouldWork() {
        shipment.setId(1L);
        assertThat(shipment.getId()).isEqualTo(1L);
    }

    @Test
    void setAndGetTrackingNumber_ShouldWork() {
        shipment.setTrackingNumber("SHIP-001");
        assertThat(shipment.getTrackingNumber()).isEqualTo("SHIP-001");
    }

    @Test
    void setAndGetDescription_ShouldWork() {
        shipment.setDescription("Electronics");
        assertThat(shipment.getDescription()).isEqualTo("Electronics");
    }

    @Test
    void setAndGetWeightKg_ShouldWork() {
        shipment.setWeightKg(BigDecimal.valueOf(250.5));
        assertThat(shipment.getWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(250.5));
    }

    @Test
    void setAndGetVolumeM3_ShouldWork() {
        shipment.setVolumeM3(BigDecimal.valueOf(2.5));
        assertThat(shipment.getVolumeM3()).isEqualByComparingTo(BigDecimal.valueOf(2.5));
    }

    @Test
    void setAndGetShipmentValue_ShouldWork() {
        shipment.setShipmentValue(BigDecimal.valueOf(15000));
        assertThat(shipment.getShipmentValue()).isEqualByComparingTo(BigDecimal.valueOf(15000));
    }

    @Test
    void setAndGetStatus_ShouldWork() {
        shipment.setStatus(ShipmentStatus.PENDING);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PENDING);
    }

    @Test
    void setAndGetExpectedDeliveryDate_ShouldWork() {
        LocalDateTime date = LocalDateTime.of(2025, 2, 1, 10, 0);
        shipment.setExpectedDeliveryDate(date);
        assertThat(shipment.getExpectedDeliveryDate()).isEqualTo(date);
    }

    @Test
    void setAndGetActualDeliveryDate_ShouldWork() {
        LocalDateTime date = LocalDateTime.of(2025, 2, 1, 14, 30);
        shipment.setActualDeliveryDate(date);
        assertThat(shipment.getActualDeliveryDate()).isEqualTo(date);
    }

    @Test
    void setAndGetRoute_ShouldWork() {
        shipment.setRoute(route);
        assertThat(shipment.getRoute()).isEqualTo(route);
    }

    @Test
    void setAndGetOriginAddress_ShouldWork() {
        shipment.setOriginAddress("Zagreb");
        assertThat(shipment.getOriginAddress()).isEqualTo("Zagreb");
    }

    @Test
    void setAndGetDestinationAddress_ShouldWork() {
        shipment.setDestinationAddress("Split");
        assertThat(shipment.getDestinationAddress()).isEqualTo("Split");
    }
}
