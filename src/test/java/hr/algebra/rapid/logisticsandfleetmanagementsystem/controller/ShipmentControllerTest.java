package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.ShipmentStatus;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.DriverService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ShipmentControllerTest {

    @Mock private ShipmentService shipmentService;
    @Mock private DriverService driverService;
    @InjectMocks private ShipmentController shipmentController;

    private MockMvc mockMvc;
    private ShipmentResponse res;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        res = new ShipmentResponse();
        res.setId(1L);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // ⭐ OVO FALI!
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(shipmentController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override public boolean supportsParameter(org.springframework.core.MethodParameter p) {
                        return p.getParameterType().equals(UserDetails.class);
                    }
                    @Override public Object resolveArgument(org.springframework.core.MethodParameter p, org.springframework.web.method.support.ModelAndViewContainer mc, org.springframework.web.context.request.NativeWebRequest wr, org.springframework.web.bind.support.WebDataBinderFactory bf) {
                        return mock(UserDetails.class);
                    }
                }).build();
    }

    @Test void findAll_Coverage() throws Exception {
        when(shipmentService.findAll()).thenReturn(Collections.singletonList(res));
        mockMvc.perform(get("/api/shipments")).andExpect(status().isOk());
    }

    @Test void findById_Found_Coverage() throws Exception {
        when(shipmentService.findById(anyLong())).thenReturn(Optional.of(res));
        mockMvc.perform(get("/api/shipments/1")).andExpect(status().isOk());
    }

    @Test void createShipment_Coverage() throws Exception {
        when(shipmentService.createShipment(any(ShipmentRequest.class))).thenReturn(res);

        String validJson = """
        {
            "trackingNumber": "TRK-999",
            "originAddress": "Start 1",
            "destinationAddress": "End 2",
            "weightKg": 50.0,
            "expectedDeliveryDate": "2026-12-31T12:00"
        }
        """; // Promijenio sam godinu u 2026. da budemo sigurni

        mockMvc.perform(post("/api/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isCreated());
    }
    @Test
    @DisplayName("Update Shipment - Success (Coverage Test)")
    void updateShipment_Coverage() throws Exception {
        Long shipmentId = 1L;

        // ⭐ REQUEST - Potpuno prilagođen ShipmentRequest DTO-u
        ShipmentRequest updateRequest = ShipmentRequest.builder()
                .trackingNumber("SHIP-2026-001-UPD")
                .originAddress("Updated Origin Address, Zagreb")
                .originLatitude(45.8150)
                .originLongitude(15.9819)
                .destinationAddress("Updated Destination Address, Rijeka")
                .destinationLatitude(45.3271)
                .destinationLongitude(14.4422)
                .weightKg(BigDecimal.valueOf(200.0))
                .volumeM3(BigDecimal.valueOf(50.0))
                .shipmentValue(BigDecimal.valueOf(5000.00))
                .expectedDeliveryDate(LocalDateTime.now().plusDays(7))  // ⭐ Budući datum!
                .description("Updated shipment description")
                .status("SCHEDULED")
                .build();

        // ⭐ RESPONSE - Prilagođen ShipmentResponse DTO-u
        ShipmentResponse updatedResponse = ShipmentResponse.builder()
                .id(shipmentId)
                .trackingNumber("SHIP-2026-001-UPD")
                .description("Updated shipment description")
                // Adrese
                .originAddress("Updated Origin Address, Zagreb")
                .destinationAddress("Updated Destination Address, Rijeka")
                // Koordinate
                .originLatitude(45.8150)
                .originLongitude(15.9819)
                .destinationLatitude(45.3271)
                .destinationLongitude(14.4422)
                // Podaci o pošiljci
                .weightKg(BigDecimal.valueOf(200.0))
                .volumeM3(BigDecimal.valueOf(50.0))
                .shipmentValue(BigDecimal.valueOf(5000.00))
                .status(ShipmentStatus.SCHEDULED)
                // Datumi
                .expectedDeliveryDate(LocalDateTime.now().plusDays(7))
                .actualDeliveryDate(null)
                // Ruta podaci (opcionalno)
                .estimatedDistanceKm(150.5)
                .estimatedDurationMinutes(180L)
                .routeStatus("CALCULATED")
                .routeId(100L)
                .build();

        when(shipmentService.updateShipment(eq(shipmentId), any(ShipmentRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/shipments/{id}", shipmentId)
                        .with(user("dispatcher").roles("DISPATCHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(shipmentId))
                .andExpect(jsonPath("$.trackingNumber").value("SHIP-2026-001-UPD"))
                .andExpect(jsonPath("$.originAddress").value("Updated Origin Address, Zagreb"))
                .andExpect(jsonPath("$.destinationAddress").value("Updated Destination Address, Rijeka"))
                .andExpect(jsonPath("$.weightKg").value(200.0))
                .andExpect(jsonPath("$.volumeM3").value(50.0))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        verify(shipmentService).updateShipment(eq(shipmentId), any(ShipmentRequest.class));
    }

    @Test void deleteShipment_Coverage() throws Exception {
        doNothing().when(shipmentService).deleteShipment(anyLong());
        mockMvc.perform(delete("/api/shipments/1")).andExpect(status().isNoContent());
    }

    @Test void startDelivery_Coverage() throws Exception {
        when(shipmentService.startDelivery(anyLong(), anyLong())).thenReturn(res);
        mockMvc.perform(put("/api/shipments/1/start")).andExpect(status().isOk());
    }

    @Test void completeDelivery_Coverage() throws Exception {
        when(shipmentService.completeDelivery(anyLong(), anyLong(), any(ProofOfDeliveryDTO.class)))
                .thenReturn(res);

        String podJson = "{\"recipientName\":\"Mislav\", \"notes\":\"Uruceno\"}";

        mockMvc.perform(post("/api/shipments/1/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(podJson)).andExpect(status().isOk());
    }

    @Test void reportIssue_Coverage() throws Exception {
        when(shipmentService.reportIssue(anyLong(), anyLong(), any(IssueReportDTO.class)))
                .thenReturn(res);

        String issueJson = "{\"issueType\":\"ACCIDENT\", \"description\":\"Kvar motora\"}";

        mockMvc.perform(put("/api/shipments/1/report-issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(issueJson)).andExpect(status().isOk());
    }
}