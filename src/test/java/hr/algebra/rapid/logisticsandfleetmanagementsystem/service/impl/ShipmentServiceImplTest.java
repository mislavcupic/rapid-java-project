package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.*;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private ShipmentRequest request;
    private Route route;

    @BeforeEach
    void setUp() {
        shipment = new Shipment();
        shipment.setId(1L);
        shipment.setTrackingNumber("TRK-123");
        shipment.setStatus(ShipmentStatus.PENDING);

        request = new ShipmentRequest();
        request.setTrackingNumber("TRK-123");
        request.setOriginAddress("Zagreb");
        request.setDestinationAddress("Split");

        route = new Route();
        route.setId(100L);
        route.setOriginLatitude(45.815);
        route.setOriginLongitude(15.981);
    }

    @Nested
    @DisplayName("Create & Update Tests (Branch Coverage)")
    class CrudOperations {

        @Test
        @DisplayName("Create: Bacanje DuplicateResourceException")
        void createShipment_ThrowsDuplicate() {
            // Pokriva granu existsByTrackingNumber == true
            given(shipmentRepository.existsByTrackingNumber("TRK-123")).willReturn(true);

            assertThatThrownBy(() -> shipmentService.createShipment(request))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        @DisplayName("Create: Uspješno kreiranje s novom rutom")
        void createShipment_Success() {
            given(shipmentRepository.existsByTrackingNumber(anyString())).willReturn(false);
            given(routeService.calculateAndCreateRoute(any(), any(), any(), any(), any(), any())).willReturn(route);
            given(shipmentRepository.save(any())).willReturn(shipment);

            ShipmentResponse response = shipmentService.createShipment(request);

            assertThat(response).isNotNull();
            verify(routeService).calculateAndCreateRoute(any(), any(), any(), any(), any(), any());
            verify(shipmentRepository).save(any());
        }

        @Test
        @DisplayName("Update: ResourceNotFound za nepostojeći ID")
        void updateShipment_NotFound() {
            given(shipmentRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> shipmentService.updateShipment(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Update: Ažuriranje postojeće rute")
        void updateShipment_UpdatesExistingRoute() {
            shipment.setRoute(route);
            given(shipmentRepository.findById(1L)).willReturn(Optional.of(shipment));
            given(routeService.calculateAndCreateRoute(any(), any(), any(), any(), any(), any())).willReturn(route);
            given(shipmentRepository.save(any())).willReturn(shipment);

            shipmentService.updateShipment(1L, request);

            // Provjerava da je ID rute zadržan
            assertThat(shipment.getRoute().getId()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("Status Workflow Tests (Negative Paths)")
    class WorkflowTests {

        @Test
        @DisplayName("Start Delivery: Uspješna promjena statusa")
        void startDelivery_Success() {
            given(shipmentRepository.findById(1L)).willReturn(Optional.of(shipment));
            given(shipmentRepository.save(any())).willReturn(shipment);

            shipmentService.startDelivery(1L, 10L);

            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        }

        @Test
        @DisplayName("Complete Delivery: Postavljanje datuma isporuke")
        void completeDelivery_Success() {
            // 1. Priprema pošiljke u ispravnom stanju
            // Backend ne dopušta isporuku ako status nije IN_TRANSIT
            shipment.setStatus(ShipmentStatus.IN_TRANSIT);

            // 2. Mockanje repozitorija
            given(shipmentRepository.findById(1L)).willReturn(Optional.of(shipment));
            // Osiguravamo da save vrati modificirani objekt
            given(shipmentRepository.save(any(Shipment.class))).willAnswer(invocation -> invocation.getArgument(0));

            // 3. Priprema DTO-a s potrebnim podacima
            ProofOfDeliveryDTO pod = new ProofOfDeliveryDTO();
            pod.setRecipientName("Ivan Horvat");
            pod.setLatitude(45.815);
            pod.setLongitude(15.9819);
            // Ako tvoja metoda zahtijeva potpis ili napomenu, dodaj ih ovdje

            // 4. Act
            shipmentService.completeDelivery(1L, 10L, pod);

            // 5. Assert
            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
            assertThat(shipment.getActualDeliveryDate()).isNotNull();

            // Bonus provjera: Je li datum isporuke postavljen na današnji dan?
            assertThat(shipment.getActualDeliveryDate()).isAfter(java.time.LocalDateTime.now().minusMinutes(1));
        }

        @Test
        @DisplayName("Report Issue: Postavljanje statusa DELAYED")
        void reportIssue_Success() {
            // 1. Priprema pošiljke u ispravnom početnom statusu
            // Mora biti IN_TRANSIT da bi reportIssue prošao validaciju
            shipment.setStatus(ShipmentStatus.IN_TRANSIT);

            // 2. Mockanje repozitorija
            given(shipmentRepository.findById(1L)).willReturn(Optional.of(shipment));
            // Osiguravamo da save vrati pošiljku koju smo mu poslali
            given(shipmentRepository.save(any(Shipment.class))).willAnswer(invocation -> invocation.getArgument(0));

            // 3. Priprema DTO objekta s podacima
            IssueReportDTO issue = new IssueReportDTO();
            issue.setIssueType("VEHICLE_ISSUE");
            issue.setDescription("Kvar na kamionu");
            issue.setEstimatedDelay("2 sata");

            // 4. Act
            shipmentService.reportIssue(1L, 10L, issue);

            // 5. Assert
            // Provjeravamo je li status promijenjen u DELAYED
            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELAYED);
        }
    }

    @Nested
    @DisplayName("Mapping & Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("MapToResponse: Pokrivanje rute koja je null")
        void mapToResponse_NullRoute() {
            shipment.setRoute(null);
            ShipmentResponse res = shipmentService.mapToResponse(shipment);

            // Provjera da se ne dogodi NullPointerException i da su polja prazna
            assertThat(res.getRouteId()).isNull();
            assertThat(res.getOriginLatitude()).isNull();
        }

        @Test
        @DisplayName("Delete: ResourceNotFound")
        void delete_NotFound() {
            given(shipmentRepository.existsById(1L)).willReturn(false);

            assertThatThrownBy(() -> shipmentService.deleteShipment(1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("UpdateFields: Grananje s praznim statusom u requestu")
        void updateFields_NullStatusInRequest() {
            request.setStatus(null); // Pokriva granu: request.getStatus() == null
            given(shipmentRepository.findById(1L)).willReturn(Optional.of(shipment));
            given(routeService.calculateAndCreateRoute(any(), any(), any(), any(), any(), any())).willReturn(route);
            given(shipmentRepository.save(any())).willReturn(shipment);

            shipmentService.updateShipment(1L, request);

            // Treba ostati PENDING ako je bio null
            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PENDING);
        }
    }
}