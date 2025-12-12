package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.configuration.TestSecurityConfig;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.ShipmentStatus;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AssignmentService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.DriverService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.JwtService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AssignmentController.class)
@Import(TestSecurityConfig.class)
class AssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssignmentService assignmentService;

    @MockitoBean
    private DriverService driverService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;



    @Nested
    class CreateAssignment {

        @Test
        @WithMockUser(username = "dispatcher1", roles = {"DISPATCHER"})
        void createAssignment_ShouldReturnCreated() throws Exception {
            // Arrange
            AssignmentRequestDTO request = new AssignmentRequestDTO();
            request.setDriverId(1L);
            request.setVehicleId(1L);
            request.setShipmentId(1L);
            request.setStartTime(LocalDateTime.now().plusHours(2));

            AssignmentResponseDTO response = createMockAssignmentResponse(1L);

            when(assignmentService.createAssignment(any(AssignmentRequestDTO.class)))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/assignments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.assignmentStatus", is("SCHEDULED")))
                    .andExpect(jsonPath("$.driver.id", is(1)))
                    .andExpect(jsonPath("$.vehicle.id", is(1)))
                    .andExpect(jsonPath("$.shipment.id", is(1)));

            verify(assignmentService, times(1)).createAssignment(any(AssignmentRequestDTO.class));
        }

        @Test
        @WithMockUser(username = "dispatcher1", roles = {"DISPATCHER"})
        void createAssignment_WithInvalidData_ShouldReturnBadRequest() throws Exception {
            // Arrange - Request bez potrebnih polja
            AssignmentRequestDTO request = new AssignmentRequestDTO();
            // Sve null

            // Act & Assert
            mockMvc.perform(post("/api/assignments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(assignmentService, never()).createAssignment(any());
        }
    }



    @Nested
    class UpdateAssignment {

        @Test
        @WithMockUser(username = "dispatcher1", roles = {"DISPATCHER"})
        void updateAssignment_ShouldReturnUpdated() throws Exception {
            // Arrange
            Long assignmentId = 1L;
            AssignmentRequestDTO request = new AssignmentRequestDTO();
            request.setDriverId(2L);
            request.setVehicleId(2L);
            request.setShipmentId(1L);
            request.setStartTime(LocalDateTime.now().plusHours(3));

            AssignmentResponseDTO response = createMockAssignmentResponse(assignmentId);
            response.getDriver().setId(2L);
            response.getVehicle().setId(2L);

            when(assignmentService.updateAssignment(anyLong(), any(AssignmentRequestDTO.class)))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(put("/api/assignments/{id}", assignmentId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.driver.id", is(2)))
                    .andExpect(jsonPath("$.vehicle.id", is(2)));

            verify(assignmentService, times(1))
                    .updateAssignment(anyLong(), any(AssignmentRequestDTO.class));
        }
    }



    @Nested
    class GetAssignment {

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        void getAssignmentById_ShouldReturnAssignment() throws Exception {
            // Arrange
            Long assignmentId = 1L;
            AssignmentResponseDTO response = createMockAssignmentResponse(assignmentId);


            when(assignmentService.findById(anyLong())).thenReturn(Optional.of(response));

            // Act & Assert
            mockMvc.perform(get("/api/assignments/{id}", assignmentId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.assignmentStatus", is("SCHEDULED")));

            verify(assignmentService, times(1)).findById(anyLong());
        }

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        void getAllAssignments_ShouldReturnList() throws Exception {
            // Arrange
            List<AssignmentResponseDTO> assignments = Arrays.asList(
                    createMockAssignmentResponse(1L),
                    createMockAssignmentResponse(2L)
            );

            when(assignmentService.findAll()).thenReturn(assignments);

            // Act & Assert
            mockMvc.perform(get("/api/assignments")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[1].id", is(2)));

            verify(assignmentService, times(1)).findAll();
        }
    }



    @Nested
    class DriverDashboard {

        @Test
        @WithMockUser(username = "driver1", roles = {"DRIVER"})
        void getDriverSchedule_ShouldReturnList() throws Exception {
            // Arrange
            Long driverId = 1L;
            List<AssignmentResponseDTO> assignments = Arrays.asList(
                    createMockAssignmentResponse(1L),
                    createMockAssignmentResponse(2L)
            );

            // Mock driverService.getDriverIdFromUsername("driver1") → 1L
            when(driverService.getDriverIdFromUsername("driver1")).thenReturn(driverId);
            when(assignmentService.findAssignmentsByDriver(anyLong()))
                    .thenReturn(assignments);

            // Act & Assert
            mockMvc.perform(get("/api/assignments/my-schedule")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));

            verify(driverService, times(1)).getDriverIdFromUsername("driver1");
            verify(assignmentService, times(1)).findAssignmentsByDriver(anyLong());
        }

        @Test
        void startAssignment_ShouldReturnUpdated() throws Exception {
            // Arrange
            Long assignmentId = 1L;
            Long driverId = 1L;

            AssignmentResponseDTO response = createMockAssignmentResponse(assignmentId);
            response.setAssignmentStatus("IN_PROGRESS");

            // Mock za @PreAuthorize provjeru
            when(driverService.isAssignmentOwnedByDriver(assignmentId, "driver1")).thenReturn(true);

            // Mock driverService.getDriverIdFromUsername("driver1") → 1L
            when(driverService.getDriverIdFromUsername("driver1")).thenReturn(driverId);
            when(assignmentService.startAssignment(anyLong(), anyLong()))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(put("/api/assignments/{id}/start", assignmentId)
                            .with(csrf())
                            .with(user("driver1").roles("DRIVER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.assignmentStatus", is("IN_PROGRESS")));

            verify(driverService, times(1)).isAssignmentOwnedByDriver(assignmentId, "driver1");
            verify(driverService, times(1)).getDriverIdFromUsername("driver1");
            verify(assignmentService, times(1))
                    .startAssignment(anyLong(), anyLong());
        }

        @Test
        void completeAssignment_ShouldReturnCompleted() throws Exception {
            // Arrange
            Long assignmentId = 1L;
            Long driverId = 1L;

            AssignmentResponseDTO response = createMockAssignmentResponse(assignmentId);
            response.setAssignmentStatus("COMPLETED");
            response.setEndTime(LocalDateTime.now());

            // Mock za @PreAuthorize provjeru
            when(driverService.isAssignmentOwnedByDriver(assignmentId, "driver1")).thenReturn(true);

            // Mock driverService.getDriverIdFromUsername("driver1") → 1L
            when(driverService.getDriverIdFromUsername("driver1")).thenReturn(driverId);
            when(assignmentService.completeAssignment(anyLong(), anyLong()))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(put("/api/assignments/{id}/complete", assignmentId)
                            .with(csrf())
                            .with(user("driver1").roles("DRIVER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.assignmentStatus", is("COMPLETED")))
                    .andExpect(jsonPath("$.endTime", notNullValue()));

            verify(driverService, times(1)).isAssignmentOwnedByDriver(assignmentId, "driver1");
            verify(driverService, times(1)).getDriverIdFromUsername("driver1");
            verify(assignmentService, times(1))
                    .completeAssignment(anyLong(), anyLong());
        }
    }


    @Nested
    class DeleteAssignment {

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        void deleteAssignment_ShouldReturnNoContent() throws Exception {
            // Arrange
            Long assignmentId = 1L;
            doNothing().when(assignmentService).deleteAssignment(anyLong());

            // Act & Assert
            mockMvc.perform(delete("/api/assignments/{id}", assignmentId)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(assignmentService, times(1)).deleteAssignment(anyLong());
        }
    }

//helper metode

    private AssignmentResponseDTO createMockAssignmentResponse(Long id) {
        AssignmentResponseDTO response = new AssignmentResponseDTO();
        response.setId(id);
        response.setAssignmentStatus("SCHEDULED");
        response.setStartTime(LocalDateTime.now().plusHours(2));

        // Driver
        DriverResponseDTO driver = new DriverResponseDTO();
        driver.setId(1L);
        driver.setUsername("testdriver");
        driver.setFirstName("Test");
        driver.setLastName("Driver");
        driver.setFullName("Test Driver");
        driver.setLicenseNumber("TEST-001");
        driver.setEmail("test@driver.com");
        driver.setPhoneNumber("+385991234567");
        response.setDriver(driver);

        // Vehicle
        VehicleResponse vehicle = new VehicleResponse();
        vehicle.setId(1L);
        vehicle.setLicensePlate("ZG-TEST-001");
        vehicle.setMake("Mercedes");
        vehicle.setModel("Sprinter");
        vehicle.setModelYear(2022);
        vehicle.setFuelType("Diesel");
        vehicle.setLoadCapacityKg(BigDecimal.valueOf(1000));
        vehicle.setCurrentMileageKm(50000L);
        vehicle.setNextServiceMileageKm(55000L);
        vehicle.setRemainingKmToService(5000L);
        response.setVehicle(vehicle);

        // Shipment
        ShipmentResponse shipment = new ShipmentResponse();
        shipment.setId(1L);
        shipment.setTrackingNumber("SHIP-001");
        shipment.setDescription("Test shipment");
        shipment.setOriginAddress("Zagreb, Croatia");
        shipment.setDestinationAddress("Split, Croatia");
        shipment.setWeightKg(BigDecimal.valueOf(100.0));
        shipment.setVolumeM3(BigDecimal.valueOf(5.0));
        shipment.setStatus(ShipmentStatus.SCHEDULED);
        shipment.setExpectedDeliveryDate(LocalDateTime.now().plusDays(2));
        shipment.setEstimatedDistanceKm(300.0);
        shipment.setEstimatedDurationMinutes(180L);
        shipment.setRouteStatus("CALCULATED");
        response.setShipment(shipment);

        return response;
    }
}