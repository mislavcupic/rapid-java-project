package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Route;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl.RouteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    private RouteService routeService;

    @BeforeEach
    void setUp() {
        routeService = new RouteServiceImpl();
    }

    @Test
    void calculateAndCreateRoute_ShouldCreateRoute() {
        Route result = routeService.calculateAndCreateRoute(
            "Zagreb", 45.8150, 15.9819,
            "Split", 43.5081, 16.4402
        );
        
        assertThat(result).isNotNull();
        assertThat(result.getOriginAddress()).isEqualTo("Zagreb");
        assertThat(result.getDestinationAddress()).isEqualTo("Split");
        assertThat(result.getEstimatedDistanceKm()).isGreaterThan(0);
    }
}
