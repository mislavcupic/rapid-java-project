package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.configuration.TestSecurityConfig;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssignmentController.class)
@Import(TestSecurityConfig.class)
class AssignmentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private AssignmentService assignmentService;
    @MockitoBean private DriverService driverService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsServiceImpl userDetailsServiceImpl;

    // POMOĆNA METODA: Kreira validan zahtjev da bi prošao @Valid provjeru
    private AssignmentRequestDTO createValidRequest() {
        AssignmentRequestDTO request = new AssignmentRequestDTO();
        request.setDriverId(1L);
        request.setVehicleId(1L);
        request.setShipmentIds(Collections.singletonList(1L));
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(2));
        return request;
    }

    private AssignmentResponseDTO getMockResponse(Long id) {
        AssignmentResponseDTO response = new AssignmentResponseDTO();
        response.setId(id);
        response.setAssignmentStatus("SCHEDULED");
        return response;
    }

    @Nested
    class RetrievalCoverage {
        @Test
        @WithMockUser(roles = "ADMIN")
        void givenData_whenFindById_thenReturns200() throws Exception {
            given(assignmentService.findById(1L)).willReturn(Optional.of(getMockResponse(1L)));
            mockMvc.perform(get("/api/assignments/1").with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void givenNoData_whenFindById_thenReturns404() throws Exception {
            given(assignmentService.findById(anyLong())).willReturn(Optional.empty());
            mockMvc.perform(get("/api/assignments/99").with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class DriverActionCoverage {
        @Test
        @WithMockUser(username = "driver1", roles = "DRIVER")
        void givenOwner_whenStart_thenReturns200() throws Exception {
            String username = "driver1";
            given(driverService.isAssignmentOwnedByDriver(anyLong(), any())).willReturn(true);
            given(driverService.getDriverIdFromUsername(username)).willReturn(10L);
            given(assignmentService.startAssignment(anyLong(), anyLong())).willReturn(Optional.of(getMockResponse(1L)));

            mockMvc.perform(put("/api/assignments/1/start")
                            .with(csrf())
                            // Ključno: koristimo user(username) da se podudara s Mockito expectationom
                            .with(user(username).roles("DRIVER")))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "driver1", roles = "DRIVER")
        void givenNotOwner_whenStart_thenReturns403() throws Exception {
            given(driverService.isAssignmentOwnedByDriver(anyLong(), any())).willReturn(false);

            mockMvc.perform(put("/api/assignments/1/start")
                            .with(csrf())
                            .with(user("driver1").roles("DRIVER")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "driver1", roles = "DRIVER")
        void givenNoAssignment_whenStart_thenReturns404() throws Exception {
            given(driverService.isAssignmentOwnedByDriver(anyLong(), any())).willReturn(true);
            given(driverService.getDriverIdFromUsername("driver1")).willReturn(10L);
            given(assignmentService.startAssignment(anyLong(), anyLong())).willReturn(Optional.empty());

            mockMvc.perform(put("/api/assignments/1/start")
                            .with(csrf())
                            .with(user("driver1").roles("DRIVER")))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class ModificationCoverage {
        @Test
        @WithMockUser(roles = "DISPATCHER")
        void givenNoData_whenUpdate_thenReturns404() throws Exception {
            // Šaljemo VALIDAN DTO (polja nisu null) da izbjegnemo Validation Error 500
            AssignmentRequestDTO validReq = createValidRequest();
            given(assignmentService.updateAssignment(anyLong(), any())).willReturn(Optional.empty());

            mockMvc.perform(put("/api/assignments/1").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validReq)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "DISPATCHER")
        void givenValidData_whenUpdate_thenReturns200() throws Exception {
            AssignmentRequestDTO validReq = createValidRequest();
            given(assignmentService.updateAssignment(anyLong(), any())).willReturn(Optional.of(getMockResponse(1L)));

            mockMvc.perform(put("/api/assignments/1").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validReq)))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void givenAdmin_whenDelete_thenReturns204() throws Exception {
            mockMvc.perform(delete("/api/assignments/1").with(csrf()))
                    .andExpect(status().isNoContent());
        }
    }
}