package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AssignmentService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.DriverService;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssignmentController Tests")
class AssignmentControllerTest {

    @Mock
    private AssignmentService assignmentService;

    @Mock
    private DriverService driverService;

    @InjectMocks
    private AssignmentController assignmentController;

    private MockMvc mockMvc;
    private AssignmentResponseDTO testAssignmentResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(assignmentController).build();

        testAssignmentResponse = new AssignmentResponseDTO();
        testAssignmentResponse.setId(1L);
    }

    @Nested
    @DisplayName("GET /api/assignments")
    class GetAllAssignments {

        @Test
        @DisplayName("Should return all assignments")
        void getAllAssignments_ShouldReturnList() throws Exception {
            List<AssignmentResponseDTO> assignments = Arrays.asList(testAssignmentResponse);
            when(assignmentService.findAll()).thenReturn(assignments);

            mockMvc.perform(get("/api/assignments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(assignmentService).findAll();
        }
    }

    @Nested
    @DisplayName("GET /api/assignments/{id}")
    class GetAssignmentById {

        @Test
        @DisplayName("Should return assignment when exists")
        void getAssignmentById_WhenExists_ShouldReturnAssignment() throws Exception {
            when(assignmentService.findById(1L)).thenReturn(Optional.of(testAssignmentResponse));

            mockMvc.perform(get("/api/assignments/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            verify(assignmentService).findById(1L);
        }

        @Test
        @DisplayName("Should return 404 when assignment not found")
        void getAssignmentById_WhenNotFound_ShouldReturn404() throws Exception {
            when(assignmentService.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/assignments/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/assignments")
    class CreateAssignment {

        @Test
        @DisplayName("Should create assignment")
        void createAssignment_ShouldReturnCreated() throws Exception {
            when(assignmentService.createAssignment(any(AssignmentRequestDTO.class)))
                    .thenReturn(testAssignmentResponse);

            String json = """
                {
                    "driverId": 1,
                    "vehicleId": 1,
                    "shipmentId": 1
                }
                """;

            mockMvc.perform(post("/api/assignments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));

            verify(assignmentService).createAssignment(any(AssignmentRequestDTO.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/assignments/{id}")
    class UpdateAssignment {

        @Test
        @DisplayName("Should update assignment")
        void updateAssignment_ShouldReturnUpdated() throws Exception {
            when(assignmentService.updateAssignment(eq(1L), any(AssignmentRequestDTO.class)))
                    .thenReturn(testAssignmentResponse);

            String json = """
                {
                    "driverId": 1,
                    "vehicleId": 2
                }
                """;

            mockMvc.perform(put("/api/assignments/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());

            verify(assignmentService).updateAssignment(eq(1L), any(AssignmentRequestDTO.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/assignments/{id}")
    class DeleteAssignment {

        @Test
        @DisplayName("Should delete assignment")
        void deleteAssignment_ShouldReturnNoContent() throws Exception {
            doNothing().when(assignmentService).deleteAssignment(1L);

            mockMvc.perform(delete("/api/assignments/1"))
                    .andExpect(status().isNoContent());

            verify(assignmentService).deleteAssignment(1L);
        }
    }

    @Nested
    @DisplayName("Driver Dashboard Endpoints")
    class DriverDashboard {

        @Test
        @DisplayName("GET /api/assignments/my-schedule - should return driver schedule")
        void getDriverSchedule_ShouldReturnList() throws Exception {
            when(driverService.getDriverIdFromUsername(anyString())).thenReturn(1L);
            when(assignmentService.findAssignmentsByDriver(1L))
                    .thenReturn(Arrays.asList(testAssignmentResponse));

            mockMvc.perform(get("/api/assignments/my-schedule"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(assignmentService).findAssignmentsByDriver(1L);
        }

        @Test
        @DisplayName("PUT /api/assignments/{id}/start - should start assignment")
        void startAssignment_ShouldReturnUpdated() throws Exception {
            when(driverService.getDriverIdFromUsername(anyString())).thenReturn(1L);
            when(assignmentService.startAssignment(eq(1L), eq(1L)))
                    .thenReturn(testAssignmentResponse);

            mockMvc.perform(put("/api/assignments/1/start"))
                    .andExpect(status().isOk());

            verify(assignmentService).startAssignment(eq(1L), eq(1L));
        }

        @Test
        @DisplayName("PUT /api/assignments/{id}/complete - should complete assignment")
        void completeAssignment_ShouldReturnCompleted() throws Exception {
            when(driverService.getDriverIdFromUsername(anyString())).thenReturn(1L);
            when(assignmentService.completeAssignment(eq(1L), eq(1L)))
                    .thenReturn(testAssignmentResponse);

            mockMvc.perform(put("/api/assignments/1/complete"))
                    .andExpect(status().isOk());

            verify(assignmentService).completeAssignment(eq(1L), eq(1L));
        }
    }
}
