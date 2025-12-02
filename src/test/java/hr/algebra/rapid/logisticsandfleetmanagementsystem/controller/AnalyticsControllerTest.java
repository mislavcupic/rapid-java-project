package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleAnalyticsResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsController Tests")
class AnalyticsControllerTest {

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsController analyticsController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(analyticsController).build();
    }

    @Test
    @DisplayName("GET /api/analytics/shipments/average-active-weight - should return average")
    void getAverageActiveShipmentWeight_ShouldReturnAverage() throws Exception {
        when(analyticsService.getAverageActiveShipmentWeight()).thenReturn(1500.5);

        mockMvc.perform(get("/api/analytics/shipments/average-active-weight"))
                .andExpect(status().isOk())
                .andExpect(content().string("1500.5"));

        verify(analyticsService).getAverageActiveShipmentWeight();
    }

    @Test
    @DisplayName("POST /api/analytics/shipments/mark-overdue - should mark overdue")
    void bulkMarkOverdue_ShouldReturnUpdatedCount() throws Exception {
        when(analyticsService.bulkMarkOverdue()).thenReturn(15);

        mockMvc.perform(post("/api/analytics/shipments/mark-overdue"))
                .andExpect(status().isOk())
                .andExpect(content().string("Uspješno ažurirano 15 pošiljaka u status 'OVERDUE'."));

        verify(analyticsService).bulkMarkOverdue();
    }

    @Test
    @DisplayName("GET /api/analytics/vehicles/status - should return vehicle analytics")
    void getVehicleAlertStatus_ShouldReturnAnalytics() throws Exception {
        VehicleAnalyticsResponse response = new VehicleAnalyticsResponse();

        
        when(analyticsService.getVehicleAlertStatus()).thenReturn(response);

        mockMvc.perform(get("/api/analytics/vehicles/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overdueCount").value(5))
                .andExpect(jsonPath("$.warningCount").value(10))
                .andExpect(jsonPath("$.freeCount").value(15));

        verify(analyticsService).getVehicleAlertStatus();
    }
}
