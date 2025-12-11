package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.IssueReportDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ProofOfDeliveryDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.DriverService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShipmentController Tests")
class ShipmentControllerTest {

    @Mock
    private ShipmentService shipmentService;

    @Mock
    private DriverService driverService;

    @InjectMocks
    private ShipmentController shipmentController;

    private MockMvc mockMvc;
    private ShipmentResponse testShipmentResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(shipmentController).build();

        testShipmentResponse = new ShipmentResponse();
        testShipmentResponse.setId(1L);
        testShipmentResponse.setTrackingNumber("SH-2024-001");
        testShipmentResponse.setOriginAddress("Zagreb");
        testShipmentResponse.setDestinationAddress("Split");
        testShipmentResponse.setWeightKg(BigDecimal.valueOf(500));
    }

    @Nested
    @DisplayName("GET /api/shipments")
    class GetAllShipments {

        @Test
        @DisplayName("Should return all shipments")
        void getAllShipments_ShouldReturnList() throws Exception {
            List<ShipmentResponse> shipments = Arrays.asList(testShipmentResponse);
            when(shipmentService.findAll()).thenReturn(shipments);

            mockMvc.perform(get("/api/shipments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].trackingNumber").value("SH-2024-001"));

            verify(shipmentService).findAll();
        }
    }

    @Nested
    @DisplayName("GET /api/shipments/{id}")
    class GetShipmentById {

        @Test
        @DisplayName("Should return shipment when exists")
        void getShipmentById_WhenExists_ShouldReturnShipment() throws Exception {
            when(shipmentService.findById(1L)).thenReturn(Optional.of(testShipmentResponse));

            mockMvc.perform(get("/api/shipments/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            verify(shipmentService).findById(1L);
        }

        @Test
        @DisplayName("Should return 404 when shipment not found")
        void getShipmentById_WhenNotFound_ShouldReturn404() throws Exception {
            when(shipmentService.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/shipments/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/shipments")
    class CreateShipment {

        @Test
        @DisplayName("Should create shipment")
        void createShipment_ShouldReturnCreated() throws Exception {
            when(shipmentService.createShipment(any(ShipmentRequest.class))).thenReturn(testShipmentResponse);

            String json = """
                {
                    "trackingNumber": "SH-2024-001",
                    "originAddress": "Zagreb",
                    "destinationAddress": "Split",
                    "weightKg": 500
                }
                """;

            mockMvc.perform(post("/api/shipments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));

            verify(shipmentService).createShipment(any(ShipmentRequest.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/shipments/{id}")
    class UpdateShipment {

        @Test
        @DisplayName("Should update shipment")
        void updateShipment_ShouldReturnUpdated() throws Exception {
            when(shipmentService.updateShipment(eq(1L), any(ShipmentRequest.class)))
                    .thenReturn(testShipmentResponse);

            String json = """
                {
                    "originAddress": "Zagreb",
                    "destinationAddress": "Rijeka"
                }
                """;

            mockMvc.perform(put("/api/shipments/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());

            verify(shipmentService).updateShipment(eq(1L), any(ShipmentRequest.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/shipments/{id}")
    class DeleteShipment {

        @Test
        @DisplayName("Should delete shipment")
        void deleteShipment_ShouldReturnNoContent() throws Exception {
            doNothing().when(shipmentService).deleteShipment(1L);

            mockMvc.perform(delete("/api/shipments/1"))
                    .andExpect(status().isNoContent());

            verify(shipmentService).deleteShipment(1L);
        }
    }

    @Nested
    @DisplayName("Driver Actions")
    class DriverActions {

        @Test
        @DisplayName("PUT /api/shipments/{id}/start - should start delivery")
        void startDelivery_ShouldReturnUpdated() throws Exception {
            when(shipmentService.startDelivery(eq(1L), anyLong())).thenReturn(testShipmentResponse);

            mockMvc.perform(put("/api/shipments/1/start"))
                    .andExpect(status().isOk());

            verify(shipmentService).startDelivery(eq(1L), anyLong());
        }

        @Test
        @DisplayName("POST /api/shipments/{id}/complete - should complete delivery")
        void completeDelivery_ShouldReturnCompleted() throws Exception {
            when(shipmentService.completeDelivery(1L, anyLong(), any(ProofOfDeliveryDTO.class)))
                    .thenReturn(testShipmentResponse);

            String json = """
                {
                    "recipientName": "John Doe",
                    "recipientSignature": "signature_data"
                }
                """;

            mockMvc.perform(post("/api/shipments/1/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());

            verify(shipmentService).completeDelivery(eq(1L), anyLong(), any(ProofOfDeliveryDTO.class));
        }

        @Test
        @DisplayName("PUT /api/shipments/{id}/report-issue - should report issue")
        void reportIssue_ShouldReturnUpdated() throws Exception {
            when(shipmentService.reportIssue(1L, anyLong(), any(IssueReportDTO.class)))
                    .thenReturn(testShipmentResponse);

            String json = """
                {
                    "issueDescription": "Traffic delay"
                }
                """;

            mockMvc.perform(put("/api/shipments/1/report-issue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());

            verify(shipmentService).reportIssue(1L, anyLong(), any(IssueReportDTO.class));
        }
    }
}
