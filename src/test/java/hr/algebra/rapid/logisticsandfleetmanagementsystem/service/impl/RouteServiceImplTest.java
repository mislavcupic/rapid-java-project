package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Route;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.RouteStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.assertAll;

class RouteServiceImplTest {

    private RouteServiceImpl routeService;

    @BeforeEach
    void setUp() {
        routeService = new RouteServiceImpl();
    }

    @Test
    @DisplayName("Branch: Računanje rute s ispravnim koordinatama (Haversine)")
    void calculateAndCreateRoute_WithValidCoordinates_ShouldCalculateDistance() {
        // Given: Koordinate za Zagreb i Split
        String origin = "Zagreb";
        Double oLat = 45.8150;
        Double oLon = 15.9819;
        String destination = "Split";
        Double dLat = 43.5081;
        Double dLon = 16.4402;

        // When
        Route result = routeService.calculateAndCreateRoute(origin, oLat, oLon, destination, dLat, dLon);

        // Then
        assertAll(
                () -> assertThat(result.getOriginAddress()).isEqualTo("Zagreb"),
                () -> assertThat(result.getStatus()).isEqualTo(RouteStatus.CALCULATED),
                // Udaljenost ZG-ST je cca 250-260km zračne linije
                () -> assertThat(result.getEstimatedDistanceKm()).isGreaterThan(250.0),
                () -> assertThat(result.getEstimatedDurationMinutes()).isNotNull()
        );
    }

    @Test
    @DisplayName("Branch: Koordinate su null (Pokrivanje 'if' uvjeta)")
    void calculateAndCreateRoute_WithNullCoordinates_ShouldSetDistanceToZero() {


        Route result = routeService.calculateAndCreateRoute("Start", null, 15.0, "End", 44.0, null);

        assertAll(
                () -> assertThat(result.getEstimatedDistanceKm()).isEqualTo(0.0),
                () -> assertThat(result.getEstimatedDurationMinutes()).isZero(),
                () -> assertThat(result.getOriginLatitude()).isNull()
        );
    }

    @Test
    @DisplayName("Branch: Ista točka (Udaljenost 0)")
    void calculateAndCreateRoute_SameLocation_ShouldReturnZeroDistance() {
        Route result = routeService.calculateAndCreateRoute("A", 45.0, 15.0, "B", 45.0, 15.0);

        assertThat(result.getEstimatedDistanceKm()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Branch: Samo jedna koordinata je null (LHS of &&)")
    void calculateAndCreateRoute_OneCoordinateNull_ShouldSkipDistanceCalculation() {

        Route result = routeService.calculateAndCreateRoute("A", null, 15.0, "B", 45.0, 16.0);
        assertThat(result.getEstimatedDistanceKm()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Branch: Odredišne koordinate su null (RHS of &&)")
    void calculateAndCreateRoute_DestinationCoordsNull_ShouldSkipDistance() {
        // Pokriva drugu polovicu if uvjeta
        Route result = routeService.calculateAndCreateRoute("A", 45.0, 15.0, "B", null, null);
        assertThat(result.getEstimatedDistanceKm()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Branch: Ekstremne koordinate (Haversine preciznost)")
    void calculateAndCreateRoute_ExtremeCoordinates_ShouldWork() {
        // Testira metodu s koordinatama na suprotnim stranama svijeta (gađa matematičke grane)
        Route result = routeService.calculateAndCreateRoute("NP", 90.0, 0.0, "SP", -90.0, 0.0);
        assertThat(result.getEstimatedDistanceKm()).isCloseTo(20003.93, within(10.0));
    }
}