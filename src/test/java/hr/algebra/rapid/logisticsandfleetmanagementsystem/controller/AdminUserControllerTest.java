package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.UpdateUserRolesRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminUserController adminUserController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private UserInfo sampleUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminUserController).build();

        UserRole adminRole = new UserRole();
        adminRole.setId(1L);
        adminRole.setName("ROLE_ADMIN");

        sampleUser = new UserInfo();
        sampleUser.setId(1L);
        sampleUser.setUsername("test_user");
        sampleUser.setEmail("test@algebra.hr");
        sampleUser.setRoles(List.of(adminRole));
    }

    // --- GET TESTOVI ---

    @Test
    @DisplayName("Uspješan dohvat svih korisnika")
    void shouldReturnUsers() throws Exception {
        given(userService.findAll()).willReturn(List.of(sampleUser));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("test_user"));
    }

    @Test
    @DisplayName("Dohvat korisnika - Prazna lista")
    void shouldReturnEmptyListWhenNoUsers() throws Exception {
        given(userService.findAll()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- PUT TESTOVI ---

    @Test
    @DisplayName("Uspješna promjena uloga")
    void shouldUpdateRoles() throws Exception {
        UpdateUserRolesRequestDTO dto = new UpdateUserRolesRequestDTO();
        dto.setRoles(List.of("ROLE_ADMIN"));

        given(userService.updateUserRoles(eq(1L), any())).willReturn(sampleUser);

        mockMvc.perform(put("/api/admin/users/1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Greška: Korisnik nije pronađen")
    void shouldFailWhenUserNotFound() throws Exception {
        UpdateUserRolesRequestDTO dto = new UpdateUserRolesRequestDTO();
        dto.setRoles(List.of("ROLE_USER"));
        String jsonBody = objectMapper.writeValueAsString(dto);

        given(userService.updateUserRoles(eq(99L), any()))
                .willThrow(new EntityNotFoundException("User not found"));

        assertThrows(Exception.class, () ->
                mockMvc.perform(put("/api/admin/users/99/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
        );
    }

    // --- DELETE TESTOVI ---

    @Test
    @DisplayName("Uspješno brisanje")
    void shouldDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/admin/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @DisplayName("Greška baze podataka pri brisanju")
    void shouldHandleDatabaseError()  {
        doThrow(new RuntimeException("DB error")).when(userService).deleteUser(1L);

        assertThrows(Exception.class, () ->
                mockMvc.perform(delete("/api/admin/users/1"))
        );
    }

    // --- DODATNI COVERAGE TESTOVI ---

    @Test
    @DisplayName("Lozinka ne smije biti u JSON-u")
    void passwordShouldNotBeInJson() throws Exception {
        sampleUser.setPassword("tajna");
        given(userService.findAll()).willReturn(List.of(sampleUser));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    @DisplayName("Update uloga - Null uloge u DTO-u")
    void shouldHandleNullRolesInDto() throws IOException {
        UpdateUserRolesRequestDTO dto = new UpdateUserRolesRequestDTO();
        dto.setRoles(null);
        String jsonBody = objectMapper.writeValueAsString(dto);

        given(userService.updateUserRoles(eq(1L), any()))
                .willThrow(new IllegalArgumentException("Roles cannot be null"));

        assertThrows(Exception.class, () -> {
            mockMvc.perform(put("/api/admin/users/1/roles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody));
        });
    }

    @Test
    @DisplayName("Provjera Media Type-a")
    void shouldReturnJsonMediaType() throws Exception {
        given(userService.findAll()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}