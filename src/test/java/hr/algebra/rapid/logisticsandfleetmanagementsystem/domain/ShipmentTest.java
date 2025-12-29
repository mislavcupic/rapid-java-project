package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

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
    @DisplayName("Unit test klase Shipment za 100% Domain Coverage")
    void testShipmentClass() {
        // Kreiranjem objekta direktno testiraš 'Class' i 'Constructor' coverage


        // Postavljanje svih polja osigurava pokrivenost svih Setter metoda
        shipment.setId(1L);
        shipment.setTrackingNumber("TRK-123");
        shipment.setWeightKg(BigDecimal.TEN);
        shipment.setStatus(ShipmentStatus.PENDING);

        // Provjere
        assertAll(
                () -> assertEquals(1L, shipment.getId()),
                () -> assertEquals("TRK-123", shipment.getTrackingNumber()),
                () -> assertNotNull(shipment.getStatus())
        );
    }

    @Test
    @DisplayName("Lombok Equals i HashCode - Sonar Compliant")
    void testEqualsAndHashCode() {
        Shipment s1 = new Shipment();
        s1.setId(1L);
        s1.setTrackingNumber("TRK-100"); // Postavi ista polja da hashCode bude isti

        Shipment s2 = new Shipment();
        s2.setId(1L);
        s2.setTrackingNumber("TRK-100");

        // 1. Refleksivnost
        assertEquals(s1, s1);

        // 2. Jednakost i HashCode (Sada će biti isti jer su polja ista)
        assertEquals(s1, s2, "Objekti s istim podacima moraju biti jednaki");
        assertEquals(s1.hashCode(), s2.hashCode(), "HashCode mora biti isti za jednake objekte");

        // 3. NULL provjera - Sonar sretan s assertNotEquals
        assertNotEquals(null, s1);

        // 4. Različiti tipovi - Sonar sretan s assertNotEquals i bez dissimilar types warninga
        Object stringType = "Neki String";
        assertNotEquals(s1, stringType);
    }

    @Test
    @DisplayName("Lombok ToString Coverage")
    void testToString() {
        Shipment s = new Shipment();
        s.setTrackingNumber("TEST-TOSTRING");

        // Poziv toString() metode puni coverage za tu metodu
        String result = s.toString();

        assertNotNull(result);
        assertTrue(result.contains("TEST-TOSTRING"));
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
    @Test
    @DisplayName("Lombok: No-Args Constructor Coverage")
    void noArgsConstructor_ShouldCreateEmptyShipment() {
        // Eksplicitno pozivanje praznog konstruktora za Class i Constructor coverage
        Shipment emptyShipment = new Shipment();

        assertNotNull(emptyShipment);
        assertThat(emptyShipment.getId()).isNull();
    }

    @Test
    @DisplayName("Lombok: All-Args Constructor Coverage - Usklađen s poljima")
    void allArgsConstructor_ShouldCreateFullShipment() {
        // Priprema podataka
        Long id = 1L;
        String tracking = "TRK-ALL-ARGS";
        BigDecimal weight = BigDecimal.valueOf(100.5);
        BigDecimal volume = BigDecimal.valueOf(2.0);
        BigDecimal value = BigDecimal.valueOf(500.0);
        LocalDateTime expected = LocalDateTime.now().plusDays(1);
        LocalDateTime actual = LocalDateTime.now().plusDays(2);

        Assignment mockAssignment = new Assignment();
        mockAssignment.setId(10L);

        Route mockRoute = new Route();
        mockRoute.setId(5L);

        // Pozivanje All-Args konstruktora točnim redoslijedom iz klase:
        // id, trackingNumber, description, weightKg, volumeM3, shipmentValue, status,
        // expectedDeliveryDate, actualDeliveryDate, route, originAddress, destinationAddress,
        // originLatitude, originLongitude, destinationLatitude, destinationLongitude, assignment, deliverySequence
        Shipment fullShipment = new Shipment(
                id,                     // id
                tracking,               // trackingNumber
                "Paket elektronike",    // description
                weight,                 // weightKg
                volume,                 // volumeM3
                value,                  // shipmentValue
                ShipmentStatus.PENDING, // status
                expected,               // expectedDeliveryDate
                actual,                 // actualDeliveryDate
                mockRoute,              // route
                "Ilica 1, Zagreb",      // originAddress
                "Riva 1, Split",        // destinationAddress
                45.815,                 // originLatitude
                15.981,                 // originLongitude
                43.508,                 // destinationLatitude
                16.440,                 // destinationLongitude
                mockAssignment,         // assignment
                1                       // deliverySequence
        );

        // Provjera
        assertAll(
                () -> assertThat(fullShipment.getId()).isEqualTo(id),
                () -> assertThat(fullShipment.getTrackingNumber()).isEqualTo(tracking),
                () -> assertThat(fullShipment.getRoute().getId()).isEqualTo(5L),
                () -> assertThat(fullShipment.getAssignment().getId()).isEqualTo(10L),
                () -> assertThat(fullShipment.getDeliverySequence()).isEqualTo(1)
        );
    }
}