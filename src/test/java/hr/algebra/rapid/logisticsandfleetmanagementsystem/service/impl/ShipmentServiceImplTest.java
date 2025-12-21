package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ConflictException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.DuplicateResourceException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.AssignmentRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.ShipmentRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.RouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceImplTest {

    @Mock private ShipmentRepository shipmentRepository;
    @Mock private RouteService routeService;
    @Mock private AssignmentRepository assignmentRepository;

    @InjectMocks private ShipmentServiceImpl shipmentService;

    private Shipment shipment;
    private Assignment assignment;
    private Driver driver;

    @BeforeEach
    void setUp() {
        driver = new Driver();
        driver.setId(10L);

        shipment = new Shipment();
        shipment.setId(1L);
        shipment.setTrackingNumber("TRK123");
        shipment.setStatus(ShipmentStatus.PENDING);

        assignment = new Assignment();
        assignment.setDriver(driver);
        assignment.setStatus("SCHEDULED");
    }

    @Nested
    @DisplayName("Create & Update Branch Coverage")
    class CrudBranches {

        @Test
        void createShipment_ThrowsDuplicate() {
            given(shipmentRepository.findByTrackingNumber(anyString())).willReturn(Optional.of(shipment));
            ShipmentRequest req = new ShipmentRequest();
            req.setTrackingNumber("TRK123");

            assertThatThrownBy(() -> shipmentService.createShipment(req))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        void updateShipment_RecalculateRoute_AllConditions() {
            // Testiramo granu: recalculateRequired = true (kad je ruta null)
            shipment.setRoute(null);
            given(shipmentRepository.findById(1L)).willReturn(Optional.of(shipment));
            given(shipmentRepository.findByTrackingNumber(anyString())).willReturn(Optional.empty());
            given(routeService.calculateAndCreateRoute(any(), any(), any(), any(), any(), any())).willReturn(new Route());
            given(shipmentRepository.save(any())).willReturn(shipment);

            ShipmentRequest req = new ShipmentRequest();
            req.setTrackingNumber("NEW-TRK");
            req.setStatus("IN_TRANSIT");

            shipmentService.updateShipment(1L, req);
            verify(routeService, times(1)).calculateAndCreateRoute(any(), any(), any(), any(), any(), any());
        }

        @Test
        void updateShipment_InvalidStatus_ThrowsException() {
            given(shipmentRepository.findById(1L)).willReturn(Optional.of(shipment));
            ShipmentRequest req = new ShipmentRequest();
            req.setStatus("NEPOSTOJEĆI_STATUS"); // Ovo baca IllegalArgumentException u catch bloku

            assertThatThrownBy(() -> shipmentService.updateShipment(1L, req))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Driver Workflow Branch Coverage")
    class WorkflowBranches {

        @Test
        void startDelivery_WrongStatus_ThrowsConflict() {
            shipment.setStatus(ShipmentStatus.IN_TRANSIT); // Nije SCHEDULED
            given(shipmentRepository.findById(1L)).willReturn(Optional.of(shipment));
            given(assignmentRepository.findByShipmentId(1L)).willReturn(Optional.of(assignment));

            assertThatThrownBy(() -> shipmentService.startDelivery(1L, 10L))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Expected SCHEDULED");
        }

        @Test
        void startDelivery_WrongDriver_ThrowsConflict() {
            given(shipmentRepository.findById(1L)).willReturn(Optional.of(shipment));
            given(assignmentRepository.findByShipmentId(1L)).willReturn(Optional.of(assignment));

            assertThatThrownBy(() -> shipmentService.startDelivery(1L, 999L)) // Krivi vozač
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        void completeDelivery_Success_WithFullPOD() {
            shipment.setStatus(ShipmentStatus.IN_TRANSIT);
            given(shipmentRepository.findById(1L)).willReturn(Optional.of(shipment));
            given(assignmentRepository.findByShipmentId(1L)).willReturn(Optional.of(assignment));
            given(shipmentRepository.save(any())).willReturn(shipment);

            ProofOfDeliveryDTO pod = new ProofOfDeliveryDTO();
            pod.setRecipientName("Marko");
            pod.setNotes("Sve OK");
            pod.setLatitude(45.0);
            pod.setLongitude(15.0);

            shipmentService.completeDelivery(1L, 10L, pod);
            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        }

        @Test
        void reportIssue_Success_WithDelayAndGPS() {
            shipment.setStatus(ShipmentStatus.IN_TRANSIT);
            given(shipmentRepository.findById(1L)).willReturn(Optional.of(shipment));
            given(assignmentRepository.findByShipmentId(1L)).willReturn(Optional.of(assignment));
            given(shipmentRepository.save(any())).willReturn(shipment);

            IssueReportDTO issue = new IssueReportDTO();
            issue.setIssueType("KVAR");
            issue.setEstimatedDelay("2h");
            issue.setLatitude(45.0);
            issue.setLongitude(16.0);

            shipmentService.reportIssue(1L, 10L, issue);
            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELAYED);
        }
    }

    @Nested
    @DisplayName("Edge Cases Coverage")
    class EdgeCases {
        @Test
        void deleteShipment_NotFound_ThrowsException() {
            given(shipmentRepository.existsById(1L)).willReturn(false);
            assertThatThrownBy(() -> shipmentService.deleteShipment(1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void mapToResponse_LegacyData_NoRoute() {
            shipment.setRoute(null);
            shipment.setOriginAddress("Adresa 1");
            ShipmentResponse res = shipmentService.mapToResponse(shipment);
            assertThat(res.getOriginAddress()).isEqualTo("Adresa 1");
        }
    }
}