package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RouteTest {

    private Route route;
    private Assignment assignment;

    @BeforeEach
    void setUp() {
        assignment = new Assignment();
        assignment.setId(1L);
        route = new Route();
    }

    @Test
    void setAndGetId_ShouldWork() {
        route.setId(1L);
        assertThat(route.getId()).isEqualTo(1L);
    }

    @Test
    void setAndGetOriginAddress_ShouldWork() {
        route.setOriginAddress("Zagreb, Croatia");
        assertThat(route.getOriginAddress()).isEqualTo("Zagreb, Croatia");
    }

    @Test
    void setAndGetOriginLatitude_ShouldWork() {
        route.setOriginLatitude(45.8150);
        assertThat(route.getOriginLatitude()).isEqualTo(45.8150);
    }

    @Test
    void setAndGetOriginLongitude_ShouldWork() {
        route.setOriginLongitude(15.9819);
        assertThat(route.getOriginLongitude()).isEqualTo(15.9819);
    }

    @Test
    void setAndGetDestinationAddress_ShouldWork() {
        route.setDestinationAddress("Split, Croatia");
        assertThat(route.getDestinationAddress()).isEqualTo("Split, Croatia");
    }

    @Test
    void setAndGetDestinationLatitude_ShouldWork() {
        route.setDestinationLatitude(43.5081);
        assertThat(route.getDestinationLatitude()).isEqualTo(43.5081);
    }

    @Test
    void setAndGetDestinationLongitude_ShouldWork() {
        route.setDestinationLongitude(16.4402);
        assertThat(route.getDestinationLongitude()).isEqualTo(16.4402);
    }

    @Test
    void setAndGetEstimatedDistanceKm_ShouldWork() {
        route.setEstimatedDistanceKm(380.0);
        assertThat(route.getEstimatedDistanceKm()).isEqualTo(380.0);
    }

    @Test
    void setAndGetEstimatedDurationMinutes_ShouldWork() {
        route.setEstimatedDurationMinutes(240L);
        assertThat(route.getEstimatedDurationMinutes()).isEqualTo(240L);
    }

    @Test
    void setAndGetStatus_ShouldWork() {
        route.setStatus(RouteStatus.DRAFT);
        assertThat(route.getStatus()).isEqualTo(RouteStatus.DRAFT);
    }

    @Test
    void setAndGetAssignment_ShouldWork() {
        route.setAssignment(assignment);
        assertThat(route.getAssignment()).isEqualTo(assignment);
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyRoute() {
        Route emptyRoute = new Route();
        assertThat(emptyRoute).isNotNull();
        assertThat(emptyRoute.getId()).isNull();
    }

    @Test
    void allArgsConstructor_ShouldCreateFullRoute() {
        Route fullRoute = new Route(
            1L, "Zagreb", 45.8150, 15.9819,
            "Split", 43.5081, 16.4402,
            380.0, 240L, RouteStatus.DRAFT, assignment
        );
        assertThat(fullRoute.getId()).isEqualTo(1L);
        assertThat(fullRoute.getOriginAddress()).isEqualTo("Zagreb");
        assertThat(fullRoute.getStatus()).isEqualTo(RouteStatus.DRAFT);
    }
}
