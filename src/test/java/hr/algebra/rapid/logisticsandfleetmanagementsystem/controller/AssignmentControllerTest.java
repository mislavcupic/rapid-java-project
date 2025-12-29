package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AssignmentService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.DriverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AssignmentControllerTest {

    private MockMvc mockMvc;

    @Mock private AssignmentService assignmentService;
    @Mock private DriverService driverService;
    @InjectMocks private AssignmentController assignmentController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final UserDetails mockUser = User.withUsername("driver1").password("pass").authorities("ROLE_DRIVER").build();

    @BeforeEach
    void setUp() {
        HandlerMethodArgumentResolver authResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }
            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return mockUser;
            }
        };

        // KLJUČ: Postavljamo standaloneSetup tako da statički URL-ovi imaju prednost
        mockMvc = MockMvcBuilders.standaloneSetup(assignmentController)
                .setCustomArgumentResolvers(authResolver)
                .build();
    }

    private AssignmentResponseDTO getMockResponse(Long id) {
        return AssignmentResponseDTO.builder().id(id).assignmentStatus("SCHEDULED").build();
    }

    // --- POPRAVLJENI TESTOVI 1 I 13 ---

    @Test
    void getMyAssignments_ReturnsOk() throws Exception {
        given(driverService.getDriverIdFromUsername("driver1")).willReturn(10L);
        given(assignmentService.findAssignmentsByDriver(10L)).willReturn(List.of(getMockResponse(1L)));


        mockMvc.perform(get("/api/assignments/my-schedule")
                        .principal(() -> "driver1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getMyAssignments_Empty() throws Exception {
        given(driverService.getDriverIdFromUsername("driver1")).willReturn(10L);
        given(assignmentService.findAssignmentsByDriver(10L)).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/assignments/my-schedule")
                        .principal(() -> "driver1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    @Test void startAssignment_ReturnsOk() throws Exception {
        given(driverService.getDriverIdFromUsername(any())).willReturn(10L);
        given(assignmentService.startAssignment(anyLong(), anyLong())).willReturn(Optional.of(getMockResponse(1L)));
        mockMvc.perform(put("/api/assignments/1/start")).andExpect(status().isOk());
    }

    @Test void completeAssignment_ReturnsOk() throws Exception {
        given(driverService.getDriverIdFromUsername(any())).willReturn(10L);
        given(assignmentService.completeAssignment(anyLong(), anyLong())).willReturn(Optional.of(getMockResponse(1L)));
        mockMvc.perform(put("/api/assignments/1/complete")).andExpect(status().isOk());
    }

    @Test void update_ReturnsOk() throws Exception {
        given(assignmentService.updateAssignment(anyLong(), any())).willReturn(Optional.of(getMockResponse(1L)));
        AssignmentRequestDTO req = new AssignmentRequestDTO();
        req.setDriverId(1L); req.setVehicleId(1L); req.setShipmentIds(List.of(1L)); req.setStartTime(LocalDateTime.now());
        mockMvc.perform(put("/api/assignments/1").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test void getAll_ReturnsOk() throws Exception {
        given(assignmentService.findAll()).willReturn(List.of(getMockResponse(1L)));
        mockMvc.perform(get("/api/assignments")).andExpect(status().isOk());
    }

    @Test void getById_Found() throws Exception {
        given(assignmentService.findById(1L)).willReturn(Optional.of(getMockResponse(1L)));
        mockMvc.perform(get("/api/assignments/1")).andExpect(status().isOk());
    }

    @Test void getById_NotFound() throws Exception {
        given(assignmentService.findById(anyLong())).willReturn(Optional.empty());
        mockMvc.perform(get("/api/assignments/99")).andExpect(status().isNotFound());
    }

    @Test void create_Returns201() throws Exception {
        given(assignmentService.createAssignment(any())).willReturn(getMockResponse(1L));
        AssignmentRequestDTO req = new AssignmentRequestDTO();
        req.setDriverId(1L); req.setVehicleId(1L); req.setShipmentIds(List.of(1L)); req.setStartTime(LocalDateTime.now());
        mockMvc.perform(post("/api/assignments").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test void delete_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/assignments/1")).andExpect(status().isNoContent());
    }

    @Test void patchStatus_ReturnsOk() throws Exception {
        given(assignmentService.updateStatus(anyLong(), anyString())).willReturn(Optional.of(getMockResponse(1L)));
        mockMvc.perform(patch("/api/assignments/1/status").param("status", "IN_PROGRESS")).andExpect(status().isOk());
    }

    @Test void startAssignment_NotFound() throws Exception {
        given(driverService.getDriverIdFromUsername(any())).willReturn(10L);
        given(assignmentService.startAssignment(anyLong(), anyLong())).willReturn(Optional.empty());
        mockMvc.perform(put("/api/assignments/99/start")).andExpect(status().isNotFound());
    }

    @Test void completeAssignment_NotFound() throws Exception {
        given(driverService.getDriverIdFromUsername(any())).willReturn(10L);
        given(assignmentService.completeAssignment(anyLong(), anyLong())).willReturn(Optional.empty());
        mockMvc.perform(put("/api/assignments/99/complete")).andExpect(status().isNotFound());
    }

    @Test void patchStatus_NotFound() throws Exception {
        given(assignmentService.updateStatus(anyLong(), anyString())).willReturn(Optional.empty());
        mockMvc.perform(patch("/api/assignments/99/status").param("status", "DONE")).andExpect(status().isNotFound());
    }

    @Test void create_Returns400() throws Exception {
        mockMvc.perform(post("/api/assignments").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}