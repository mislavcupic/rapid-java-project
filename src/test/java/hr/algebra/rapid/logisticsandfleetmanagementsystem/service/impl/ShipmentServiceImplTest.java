package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.IssueReportDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ProofOfDeliveryDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentResponse;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShipmentService Unit Tests")
class ShipmentServiceImplTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private RouteService routeService;

    @InjectMocks
    private ShipmentServiceImpl shipmentService;

    private Shipment testShipment;
    private ShipmentRequest shipmentRequest;
    private Route testRoute;
    private Driver testDriver;
    private Assignment testAssignment;

    @BeforeEach
    void setUp() {
        // Setup test Route
        testRoute = new Route();
        testRoute.setId(1L);
        testRoute.setOriginAddress("Zagreb, Croatia");
        testRoute.setDestinationAddress("Split, Croatia");
        testRoute.setOriginLatitude(45.8150);
        testRoute.setOriginLongitude(15.9819);
        testRoute.setDestinationLatitude(43.5081);
        testRoute.setDestinationLongitude(16.4402);
        testRoute.setEstimatedDistanceKm(380.0);
        testRoute.setEstimatedDurationMinutes(Long.valueOf(240));
        testRoute.setStatus(RouteStatus.CALCULATED);

        // Setup test Shipment
        testShipment = new Shipment();
        testShipment.setId(1L);
        testShipment.setTrackingNumber("SHIP-001");
        testShipment.setDescription("Test shipment");
        testShipment.setWeightKg(BigDecimal.valueOf(1000.0));
        testShipment.setVolumeM3(BigDecimal.valueOf(5.0));
        testShipment.setStatus(ShipmentStatus.PENDING);
        testShipment.setOriginAddress("Zagreb, Croatia");
        testShipment.setDestinationAddress("Split, Croatia");
        testShipment.setExpectedDeliveryDate(LocalDateTime.now().plusDays(2));
        testShipment.setRoute(testRoute);

        // Setup UserInfo for Driver
        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("driver1");
        userInfo.setFirstName("John");
        userInfo.setLastName("Doe");
        userInfo.setEmail("john.doe@example.com");

        // Setup test Driver
        testDriver = new Driver();
        testDriver.setId(1L);
        testDriver.setUserInfo(userInfo);  // ← KLJUČNO!
        testDriver.setLicenseNumber("DL123456");

        // Setup test Assignment
        testAssignment = new Assignment();
        testAssignment.setId(1L);
        testAssignment.setDriver(testDriver);
        testAssignment.setShipment(testShipment);
        testAssignment.setStatus("IN_PROGRESS");

        // Setup request DTO
        shipmentRequest = new ShipmentRequest();
        shipmentRequest.setTrackingNumber("SHIP-001");
        shipmentRequest.setDescription("Test shipment");
        shipmentRequest.setWeightKg(BigDecimal.valueOf(1000.0));
        shipmentRequest.setVolumeM3(BigDecimal.valueOf(5.0));
        shipmentRequest.setOriginAddress("Zagreb, Croatia");
        shipmentRequest.setDestinationAddress("Split, Croatia");
        shipmentRequest.setOriginLatitude(45.8150);
        shipmentRequest.setOriginLongitude(15.9819);
        shipmentRequest.setDestinationLatitude(43.5081);
        shipmentRequest.setDestinationLongitude(16.4402);
        shipmentRequest.setExpectedDeliveryDate(LocalDateTime.now().plusDays(2));
    }

    // ========================================================================
    // CRUD OPERATIONS TESTS
    // ========================================================================

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {

        @Test
        @DisplayName("Should return all shipments")
        void findAll_ShouldReturnAllShipments() {
            // Given
            List<Shipment> shipments = Arrays.asList(testShipment);
            when(shipmentRepository.findAll()).thenReturn(shipments);

            // When
            List<ShipmentResponse> result = shipmentService.findAll();

            // Then

            assertThat(result.get(0).getTrackingNumber()).isNotEmpty().hasSize(1).isEqualTo("SHIP-001");
            verify(shipmentRepository).findAll();
        }

        @Test
        @DisplayName("Should return shipment by ID when exists")
        void findById_WhenExists_ShouldReturnShipment() {
            // Given
            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));

            // When
            Optional<ShipmentResponse> result = shipmentService.findById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
            assertThat(result.get().getTrackingNumber()).isEqualTo("SHIP-001");
            verify(shipmentRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return empty when shipment not found")
        void findById_WhenNotExists_ShouldReturnEmpty() {
            // Given
            when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<ShipmentResponse> result = shipmentService.findById(999L);

            // Then
            assertThat(result).isEmpty();
            verify(shipmentRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("Create Shipment")
    class CreateShipment {

        @Test
        @DisplayName("Should create shipment with route calculation")
        void createShipment_WithValidData_ShouldCreateSuccessfully() {
            // Given
            when(shipmentRepository.findByTrackingNumber("SHIP-001")).thenReturn(Optional.empty());
            when(routeService.calculateAndCreateRoute(
                    anyString(), anyDouble(), anyDouble(), anyString(), anyDouble(), anyDouble()))
                    .thenReturn(testRoute);
            when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);

            // When
            ShipmentResponse result = shipmentService.createShipment(shipmentRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTrackingNumber()).isEqualTo("SHIP-001");
            assertThat(result.getStatus()).isEqualTo(ShipmentStatus.PENDING);
            verify(routeService).calculateAndCreateRoute(
                    anyString(), anyDouble(), anyDouble(), anyString(), anyDouble(), anyDouble());
            verify(shipmentRepository).save(any(Shipment.class));
        }

        @Test
        @DisplayName("Should throw exception when tracking number already exists")
        void createShipment_WhenTrackingNumberExists_ShouldThrowException() {
            // Given
            when(shipmentRepository.findByTrackingNumber("SHIP-001"))
                    .thenReturn(Optional.of(testShipment));

            // When & Then
            assertThatThrownBy(() -> shipmentService.createShipment(shipmentRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("SHIP-001")
                    .hasMessageContaining("already exists");

            verify(shipmentRepository).findByTrackingNumber("SHIP-001");
            verify(shipmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should set default PENDING status on creation")
        void createShipment_ShouldSetDefaultPendingStatus() {
            // Given
            when(shipmentRepository.findByTrackingNumber("SHIP-001")).thenReturn(Optional.empty());
            when(routeService.calculateAndCreateRoute(
                    anyString(), anyDouble(), anyDouble(), anyString(), anyDouble(), anyDouble()))
                    .thenReturn(testRoute);
            when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
                Shipment saved = invocation.getArgument(0);
                assertThat(saved.getStatus()).isEqualTo(ShipmentStatus.PENDING);
                return saved;
            });

            // When
            shipmentService.createShipment(shipmentRequest);

            // Then
            verify(shipmentRepository).save(argThat(shipment ->
                    shipment.getStatus() == ShipmentStatus.PENDING
            ));
        }
    }

    @Nested
    @DisplayName("Update Shipment")
    class UpdateShipment {

        @Test
        @DisplayName("Should update shipment successfully")
        void updateShipment_WithValidData_ShouldUpdateSuccessfully() {
            // Given
            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
            when(shipmentRepository.findByTrackingNumber("SHIP-001"))
                    .thenReturn(Optional.of(testShipment));
            when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);

            // When
            ShipmentResponse result = shipmentService.updateShipment(1L, shipmentRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(shipmentRepository).save(any(Shipment.class));
        }

        @Test
        @DisplayName("Should throw exception when shipment not found")
        void updateShipment_WhenNotFound_ShouldThrowException() {
            // Given
            when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shipmentService.updateShipment(999L, shipmentRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shipment")
                    .hasMessageContaining("999");

            verify(shipmentRepository).findById(999L);
            verify(shipmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should recalculate route when coordinates change")
        void updateShipment_WhenCoordinatesChange_ShouldRecalculateRoute() {
            // Given
            shipmentRequest.setDestinationLatitude(44.0); // Changed
            shipmentRequest.setDestinationLongitude(16.0); // Changed

            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
            when(shipmentRepository.findByTrackingNumber("SHIP-001"))
                    .thenReturn(Optional.of(testShipment));
            when(routeService.calculateAndCreateRoute(
                    anyString(), anyDouble(), anyDouble(), anyString(), anyDouble(), anyDouble()))
                    .thenReturn(testRoute);
            when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);

            // When
            shipmentService.updateShipment(1L, shipmentRequest);

            // Then
            verify(routeService).calculateAndCreateRoute(
                    anyString(), anyDouble(), anyDouble(), anyString(), anyDouble(), anyDouble());
        }

        @Test
        @DisplayName("Should throw exception when tracking number duplicate")
        void updateShipment_WhenTrackingNumberDuplicate_ShouldThrowException() {
            // Given
            Shipment anotherShipment = new Shipment();
            anotherShipment.setId(2L);
            anotherShipment.setTrackingNumber("SHIP-001");

            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
            when(shipmentRepository.findByTrackingNumber("SHIP-001"))
                    .thenReturn(Optional.of(anotherShipment));

            // When & Then
            assertThatThrownBy(() -> shipmentService.updateShipment(1L, shipmentRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("SHIP-001");

            verify(shipmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Shipment")
    class DeleteShipment {

        @Test
        @DisplayName("Should delete shipment successfully")
        void deleteShipment_WhenExists_ShouldDeleteSuccessfully() {
            // Given
            when(shipmentRepository.existsById(1L)).thenReturn(true);

            // When
            shipmentService.deleteShipment(1L);

            // Then
            verify(shipmentRepository).existsById(1L);
            verify(shipmentRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when shipment not found")
        void deleteShipment_WhenNotFound_ShouldThrowException() {
            // Given
            when(shipmentRepository.existsById(999L)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> shipmentService.deleteShipment(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shipment")
                    .hasMessageContaining("999");

            verify(shipmentRepository).existsById(999L);
            verify(shipmentRepository, never()).deleteById(anyLong());
        }
    }

    // ========================================================================
    // DRIVER WORKFLOW TESTS
    // ========================================================================

    @Nested
    @DisplayName("Start Delivery")
    class StartDelivery {

        @Test
        @DisplayName("Should start delivery successfully")
        void startDelivery_WithValidData_ShouldStartSuccessfully() {
            // Given
            testShipment.setStatus(ShipmentStatus.SCHEDULED);
            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
            when(assignmentRepository.findByShipmentId(1L)).thenReturn(Optional.of(testAssignment));
            when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);
            when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);

            // When
            ShipmentResponse result = shipmentService.startDelivery(1L, 1L);

            // Then
            assertThat(result).isNotNull();
            verify(shipmentRepository).save(argThat(shipment ->
                    shipment.getStatus() == ShipmentStatus.IN_TRANSIT
            ));
        }

        @Test
        @DisplayName("Should throw exception when shipment not found")
        void startDelivery_WhenShipmentNotFound_ShouldThrowException() {
            // Given
            when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shipmentService.startDelivery(999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shipment");

            verify(shipmentRepository).findById(999L);
        }

        @Test
        @DisplayName("Should throw exception when shipment not assigned")
        void startDelivery_WhenNotAssigned_ShouldThrowException() {
            // Given
            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
            when(assignmentRepository.findByShipmentId(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shipmentService.startDelivery(1L, 1L))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("not assigned");

            verify(assignmentRepository).findByShipmentId(1L);
        }

        @Test
        @DisplayName("Should throw exception when driver mismatch")
        void startDelivery_WhenDriverMismatch_ShouldThrowException() {
            // Given
            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
            when(assignmentRepository.findByShipmentId(1L)).thenReturn(Optional.of(testAssignment));

            // When & Then
            assertThatThrownBy(() -> shipmentService.startDelivery(1L, 999L))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("not assigned to driver");

            verify(shipmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when status is not SCHEDULED")
        void startDelivery_WhenNotScheduled_ShouldThrowException() {
            // Given
            testShipment.setStatus(ShipmentStatus.DELIVERED);
            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
            when(assignmentRepository.findByShipmentId(1L)).thenReturn(Optional.of(testAssignment));

            // When & Then
            assertThatThrownBy(() -> shipmentService.startDelivery(1L, 1L))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Cannot start delivery");

            verify(shipmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Complete Delivery")
    class CompleteDelivery {

        private ProofOfDeliveryDTO podDTO;

        @BeforeEach
        void setUp() {
            podDTO = new ProofOfDeliveryDTO();
            podDTO.setRecipientName("John Smith");
            podDTO.setNotes("Package delivered successfully");
            podDTO.setLatitude(43.5081);
            podDTO.setLongitude(16.4402);
        }

        @Test
        @DisplayName("Should complete delivery successfully")
        void completeDelivery_WithValidData_ShouldCompleteSuccessfully() {
            // Given
            testShipment.setStatus(ShipmentStatus.IN_TRANSIT);
            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
            when(assignmentRepository.findByShipmentId(1L)).thenReturn(Optional.of(testAssignment));
            when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);

            // When
            ShipmentResponse result = shipmentService.completeDelivery(1L, 1L, podDTO);

            // Then
            assertThat(result).isNotNull();
            verify(shipmentRepository).save(argThat(shipment ->
                    shipment.getStatus() == ShipmentStatus.DELIVERED
            ));
        }

        @Test
        @DisplayName("Should throw exception when shipment not found")
        void completeDelivery_WhenShipmentNotFound_ShouldThrowException() {
            // Given
            when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shipmentService.completeDelivery(999L, 1L, podDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shipment");

            verify(shipmentRepository).findById(999L);
        }

        @Test
        @DisplayName("Should throw exception when status is not IN_TRANSIT")
        void completeDelivery_WhenNotInTransit_ShouldThrowException() {
            // Given
            testShipment.setStatus(ShipmentStatus.PENDING);
            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
            when(assignmentRepository.findByShipmentId(1L)).thenReturn(Optional.of(testAssignment));

            // When & Then
            assertThatThrownBy(() -> shipmentService.completeDelivery(1L, 1L, podDTO))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Cannot complete delivery");

            verify(shipmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should log proof of delivery information")
        void completeDelivery_ShouldLogPODInformation() {
            // Given
            testShipment.setStatus(ShipmentStatus.IN_TRANSIT);
            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
            when(assignmentRepository.findByShipmentId(1L)).thenReturn(Optional.of(testAssignment));
            when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);

            // When
            shipmentService.completeDelivery(1L, 1L, podDTO);

            // Then - verify that POD details were processed
            verify(shipmentRepository).save(any(Shipment.class));
            // Logger verification would require PowerMock or similar
        }
    }

    @Nested
    @DisplayName("Report Issue")
    class ReportIssue {

        private IssueReportDTO issueDTO;

        @BeforeEach
        void setUp() {
            issueDTO = new IssueReportDTO();
            issueDTO.setIssueType("VEHICLE_BREAKDOWN");
            issueDTO.setDescription("Engine overheating");
            issueDTO.setEstimatedDelay(String.valueOf(120));
            issueDTO.setLatitude(45.0);
            issueDTO.setLongitude(15.5);
        }

        @Test
        @DisplayName("Should report issue successfully")
        void reportIssue_WithValidData_ShouldReportSuccessfully() {
            // Given
            testShipment.setStatus(ShipmentStatus.IN_TRANSIT);
            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
            when(assignmentRepository.findByShipmentId(1L)).thenReturn(Optional.of(testAssignment));
            when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);

            // When
            ShipmentResponse result = shipmentService.reportIssue(1L, 1L, issueDTO);

            // Then
            assertThat(result).isNotNull();
            verify(shipmentRepository).save(argThat(shipment ->
                    shipment.getStatus() == ShipmentStatus.DELAYED
            ));
        }

        @Test
        @DisplayName("Should throw exception when shipment not found")
        void reportIssue_WhenShipmentNotFound_ShouldThrowException() {
            // Given
            when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shipmentService.reportIssue(999L, 1L, issueDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shipment");

            verify(shipmentRepository).findById(999L);
        }

        @Test
        @DisplayName("Should throw exception when status is not IN_TRANSIT")
        void reportIssue_WhenNotInTransit_ShouldThrowException() {
            // Given
            testShipment.setStatus(ShipmentStatus.DELIVERED);
            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
            when(assignmentRepository.findByShipmentId(1L)).thenReturn(Optional.of(testAssignment));

            // When & Then
            assertThatThrownBy(() -> shipmentService.reportIssue(1L, 1L, issueDTO))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Cannot report issue");

            verify(shipmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when driver mismatch")
        void reportIssue_WhenDriverMismatch_ShouldThrowException() {
            // Given
            testShipment.setStatus(ShipmentStatus.IN_TRANSIT);
            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
            when(assignmentRepository.findByShipmentId(1L)).thenReturn(Optional.of(testAssignment));

            // When & Then
            assertThatThrownBy(() -> shipmentService.reportIssue(1L, 999L, issueDTO))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("not assigned to driver");

            verify(shipmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should log issue report details")
        void reportIssue_ShouldLogIssueDetails() {
            // Given
            testShipment.setStatus(ShipmentStatus.IN_TRANSIT);
            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
            when(assignmentRepository.findByShipmentId(1L)).thenReturn(Optional.of(testAssignment));
            when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);

            // When
            shipmentService.reportIssue(1L, 1L, issueDTO);

            // Then - verify that issue was processed and saved
            verify(shipmentRepository).save(any(Shipment.class));
            // Logger verification would require PowerMock or similar
        }
    }

    @Nested
    @DisplayName("Map To Response")
    class MapToResponse {

        @Test
        @DisplayName("Should map shipment with route to response")
        void mapToResponse_WithRoute_ShouldMapCorrectly() {
            // When
            ShipmentResponse response = shipmentService.mapToResponse(testShipment);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTrackingNumber()).isEqualTo("SHIP-001");
            assertThat(response.getStatus()).isEqualTo(ShipmentStatus.PENDING);
            assertThat(response.getOriginAddress()).isEqualTo("Zagreb, Croatia");
            assertThat(response.getDestinationAddress()).isEqualTo("Split, Croatia");
            assertThat(response.getEstimatedDistanceKm()).isEqualTo(380.0);
            assertThat(response.getEstimatedDurationMinutes()).isEqualTo(240);
        }

        @Test
        @DisplayName("Should map shipment without route to response")
        void mapToResponse_WithoutRoute_ShouldMapCorrectly() {
            // Given
            testShipment.setRoute(null);

            // When
            ShipmentResponse response = shipmentService.mapToResponse(testShipment);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getOriginAddress()).isEqualTo("Zagreb, Croatia");
            assertThat(response.getDestinationAddress()).isEqualTo("Split, Croatia");
            assertThat(response.getEstimatedDistanceKm()).isNull();
        }
    }
}
