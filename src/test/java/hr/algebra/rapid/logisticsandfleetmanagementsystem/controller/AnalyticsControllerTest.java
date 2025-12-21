package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleAnalyticsResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AnalyticsService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock
    private AnalyticsService analyticsService;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private AnalyticsController analyticsController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Prisiljavamo UTF-8 enkodiranje kroz filter kako bi izbjegli upitnike umjesto š i ž
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);

        mockMvc = MockMvcBuilders.standaloneSetup(analyticsController)
                .addFilters(filter)
                .build();
    }

    @Test
    void shouldReturnAverageActiveShipmentWeight() throws Exception {
        when(analyticsService.getAverageActiveShipmentWeight()).thenReturn(1500.5);

        mockMvc.perform(get("/api/analytics/shipments/average-active-weight"))
                .andExpect(status().isOk())
                .andExpect(content().string("1500.5"));
    }

    @Test
    void shouldHandleExceptionInAverageWeight() {
        when(analyticsService.getAverageActiveShipmentWeight()).thenThrow(new RuntimeException("Error"));

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            mockMvc.perform(get("/api/analytics/shipments/average-active-weight"));
        });
    }

    @Test
    void shouldReturnVehicleAnalyticsStatus() throws Exception {
        VehicleAnalyticsResponse response = new VehicleAnalyticsResponse();
        response.setOverdue(5L);
        response.setWarning(10L);
        response.setFree(15L);

        when(analyticsService.getVehicleAlertStatus()).thenReturn(response);

        mockMvc.perform(get("/api/analytics/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overdue").value(5))
                .andExpect(jsonPath("$.warning").value(10));
    }

    @Test
    void shouldBulkMarkShipmentsAsOverdue() throws Exception {
        int count = 3;
        when(analyticsService.bulkMarkOverdue()).thenReturn(count);

        mockMvc.perform(post("/api/analytics/shipments/mark-overdue"))
                .andExpect(status().isOk())
                // Provjeravamo samo dijelove koji nemaju specijalne znakove radi stabilnosti testa
                .andExpect(content().string(containsString(String.valueOf(count))))
                .andExpect(content().string(containsString("OVERDUE")));
    }

    @Test
    void shouldGetOverdueVehiclesList() throws Exception {
        when(vehicleService.findOverdueMaintenanceVehicles()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/analytics/vehicles/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetWarningVehiclesWithThreshold() throws Exception {
        when(vehicleService.findWarningMaintenanceVehicles(5000L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/analytics/vehicles/warning"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetAvailableVehicles() throws Exception {
        when(vehicleService.findFreeVehiclesDetails()).thenReturn(List.of(new VehicleResponse()));

        mockMvc.perform(get("/api/analytics/vehicles/free"))
                .andExpect(status().isOk());
    }
}