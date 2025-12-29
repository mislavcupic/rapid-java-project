package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleAnalyticsResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.BulkMarkOverdueException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.VehicleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JdbcAnalyticsServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private JdbcAnalyticsServiceImpl analyticsService;

    @Nested
    @DisplayName("Branch Coverage: getAverageActiveShipmentWeight")
    class AverageWeightBranches {

        @Test
        @DisplayName("Branch: Result is NOT null")
        void givenValidResult_whenGetAverageWeight_thenReturnsValue() {
            given(jdbcTemplate.queryForObject(anyString(), eq(Double.class))).willReturn(75.5);

            Double result = analyticsService.getAverageActiveShipmentWeight();

            assertThat(result).isEqualTo(75.5);
        }

        @Test
        @DisplayName("Branch: Result IS null")
        void givenNullResult_whenGetAverageWeight_thenReturnsZero() {
            // Pokriva granu: return result != null ? result : 0.0; (kad je result null)
            given(jdbcTemplate.queryForObject(anyString(), eq(Double.class))).willReturn(null);

            Double result = analyticsService.getAverageActiveShipmentWeight();

            assertThat(result).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Branch: Exception catch block")
        void givenDatabaseError_whenGetAverageWeight_thenReturnsZero() {
            given(jdbcTemplate.queryForObject(anyString(), eq(Double.class)))
                    .willThrow(new RuntimeException("DB Down"));

            Double result = analyticsService.getAverageActiveShipmentWeight();

            assertThat(result).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Branch Coverage: bulkMarkOverdue")
    class BulkUpdateBranches {

        @Test
        @DisplayName("Branch: Success path")
        void givenSuccess_whenBulkMarkOverdue_thenReturnsCount() {
            given(jdbcTemplate.update(anyString(), Optional.ofNullable(any()))).willReturn(5);

            int rows = analyticsService.bulkMarkOverdue();

            assertThat(rows).isEqualTo(5);
        }

        @Test
        @DisplayName("Branch: Exception catch and throw")
        void givenError_whenBulkMarkOverdue_thenThrowsCustomException() {
            given(jdbcTemplate.update(anyString(), Optional.ofNullable(any())))
                    .willThrow(new RuntimeException("SQL Error"));

            assertThatThrownBy(() -> analyticsService.bulkMarkOverdue())
                    .isInstanceOf(BulkMarkOverdueException.class);
        }
    }

    @Nested
    @DisplayName("Branch Coverage: getVehicleAlertStatus")
    class VehicleAlertBranches {

        @Test
        @DisplayName("Full path coverage")
        void whenGetVehicleAlertStatus_thenReturnsAggregatedData() {
            // Ova metoda nema if-else, ali pozivamo sve mockove da pokrijemo linije
            given(vehicleService.countVehiclesOverdueForService()).willReturn(1L);
            given(vehicleService.countVehiclesInServiceWarning(5000L)).willReturn(2L);
            given(vehicleService.countFreeVehicles()).willReturn(3L);
            given(vehicleService.countTotalVehicles()).willReturn(10L);

            VehicleAnalyticsResponse response = analyticsService.getVehicleAlertStatus();

            assertThat(response.getOverdue()).isEqualTo(1L);
            assertThat(response.getWarning()).isEqualTo(2L);
            assertThat(response.getFree()).isEqualTo(3L);
            assertThat(response.getTotal()).isEqualTo(10L);
        }
    }
}