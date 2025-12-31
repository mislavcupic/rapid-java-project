package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.DriverService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ShipmentControllerTest {

    @Mock private ShipmentService shipmentService;
    @Mock private DriverService driverService;
    @InjectMocks private ShipmentController shipmentController;

    private MockMvc mockMvc;
    private ShipmentResponse res;

    @BeforeEach
    void setUp() {
        res = new ShipmentResponse();
        res.setId(1L);

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
    @Test void updateShipment_Coverage() throws Exception {
        when(shipmentService.updateShipment(anyLong(), any(ShipmentRequest.class))).thenReturn(res);

        String updateJson = """
            {
                "trackingNumber": "TRK-UPD",
                "originAddress": "New Start",
                "destinationAddress": "New End",
                "weightKg": 25.5,
                "expectedDeliveryDate": "2025-12-31T20:00",
                "status": "IN_TRANSIT"
            }
            """;

        mockMvc.perform(put("/api/shipments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)).andExpect(status().isOk());
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