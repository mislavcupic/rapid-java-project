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

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsController Tests")
class AnalyticsControllerTest {

    @Mock
    private AnalyticsService analyticsService;

    // Kontroler koji se testira (sada znamo njegov kôd)
    @InjectMocks
    private AnalyticsController analyticsController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // 🚀 KLJUČNO RJEŠENJE ZA ENKODING:
        // Prisiljavanje MockMvc da koristi UTF-8 kodiranje za odgovor.
        // Ovo rješava problem gdje se 'š' i 'ž' prikazuju kao '?'.
        mockMvc = MockMvcBuilders.standaloneSetup(analyticsController)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .build();
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
        int expectedCount = 15;
        when(analyticsService.bulkMarkOverdue()).thenReturn(expectedCount);

        // Očekivana poruka TOČNO PREPISANA IZ KONTROLERA (SADA RADI ZBOG UTF-8 POSTAVKE)
        String expectedContent = String.format(
                "Uspješno ažurirano %d pošiljaka u status 'OVERDUE'.",
                expectedCount
        );

        mockMvc.perform(post("/api/analytics/shipments/mark-overdue"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedContent)); // Očekujemo točan string

        verify(analyticsService).bulkMarkOverdue();
    }

    @Test
    @DisplayName("GET /api/analytics/vehicles/status - should return vehicle analytics")
    void getVehicleAlertStatus_ShouldReturnAnalytics() throws Exception {
        // Arrange
        VehicleAnalyticsResponse response = new VehicleAnalyticsResponse();

        // Ispravno postavljanje mock vrijednosti (koristeći Long tip)
        response.setOverdue(5L);
        response.setWarning(10L);
        response.setFree(15L);

        when(analyticsService.getVehicleAlertStatus()).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/vehicles/status"))
                .andExpect(status().isOk())
                // Ispravne JSON putanje (na temelju DTO polja)
                .andExpect(jsonPath("$.overdue").value(5))
                .andExpect(jsonPath("$.warning").value(10))
                .andExpect(jsonPath("$.free").value(15));

        verify(analyticsService).getVehicleAlertStatus();
    }
}