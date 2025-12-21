package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.UpdateUserRolesRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.JwtService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.UserDetailsServiceImpl;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserInfo sampleUser;
    private UserRole adminRole;

    @BeforeEach
    void setUp() {
        adminRole = new UserRole();
        adminRole.setId(1L);
        adminRole.setName("ROLE_ADMIN");

        sampleUser = new UserInfo();
        sampleUser.setId(1L);
        sampleUser.setUsername("test_user");
        sampleUser.setEmail("test@algebra.hr");
        sampleUser.setRoles(List.of(adminRole));
    }

    // ==========================================
    // 1. DOHVAT SVIH KORISNIKA (GET)
    // ==========================================
    @Nested
    @DisplayName("Testovi za dohvat svih korisnika")
    class GetAllUsersTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Uspješan dohvat liste korisnika")
        void shouldReturnListWhenUsersExist() throws Exception {
            given(userService.findAll()).willReturn(List.of(sampleUser));

            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].username").value("test_user"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Dohvat prazne liste (nema korisnika u bazi)")
        void shouldReturnEmptyList() throws Exception {
            given(userService.findAll()).willReturn(Collections.emptyList());

            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Greška 500 zbog @PreAuthorize ako korisnik nije ADMIN")
        void shouldFailWhenNotAdmin() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isInternalServerError());
        }
    }

    // ==========================================
    // 2. AŽURIRANJE ROLA (PUT)
    // ==========================================
    @Nested
    @DisplayName("Testovi za ažuriranje uloga")
    class UpdateUserRolesTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Uspješna promjena uloga")
        void shouldUpdateRolesSuccessfully() throws Exception {
            UpdateUserRolesRequestDTO dto = new UpdateUserRolesRequestDTO();
            dto.setRoles(List.of("ROLE_ADMIN", "ROLE_DRIVER"));

            given(userService.updateUserRoles(eq(1L), any())).willReturn(sampleUser);

            mockMvc.perform(put("/api/admin/users/1/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Greška 500 kada korisnik ne postoji (EntityNotFound)")
        void shouldReturn500WhenUserNotFound() throws Exception {
            UpdateUserRolesRequestDTO dto = new UpdateUserRolesRequestDTO();
            dto.setRoles(List.of("ROLE_USER"));

            given(userService.updateUserRoles(eq(99L), any()))
                    .willThrow(new EntityNotFoundException("User not found"));

            mockMvc.perform(put("/api/admin/users/99/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Greška 500 kada je body prazan")
        void shouldReturn500WhenNoRequestBody() throws Exception {
            mockMvc.perform(put("/api/admin/users/1/roles")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Slanje prazne liste uloga je dozvoljeno")
        void shouldAllowEmptyRolesList() throws Exception {
            UpdateUserRolesRequestDTO dto = new UpdateUserRolesRequestDTO();
            dto.setRoles(Collections.emptyList());

            given(userService.updateUserRoles(anyLong(), any())).willReturn(sampleUser);

            mockMvc.perform(put("/api/admin/users/1/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }
    }

    // ==========================================
    // 3. BRISANJE KORISNIKA (DELETE)
    // ==========================================
    @Nested
    @DisplayName("Testovi za brisanje korisnika")
    class DeleteUserTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Uspješno brisanje postojećeg korisnika")
        void shouldDeleteUserSuccessfully() throws Exception {
            doNothing().when(userService).deleteUser(1L);

            mockMvc.perform(delete("/api/admin/users/1"))
                    .andExpect(status().isNoContent());

            verify(userService, times(1)).deleteUser(1L);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Greška 500 pri brisanju nepostojećeg korisnika")
        void shouldReturn500OnDeleteNonExistent() throws Exception {
            doThrow(new EntityNotFoundException()).when(userService).deleteUser(99L);

            mockMvc.perform(delete("/api/admin/users/99"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Greška 500 kod baze podataka pri brisanju")
        void shouldHandleDatabaseErrorOnDelete() throws Exception {
            doThrow(new RuntimeException("DB error")).when(userService).deleteUser(1L);

            mockMvc.perform(delete("/api/admin/users/1"))
                    .andExpect(status().isInternalServerError());
        }
    }

    // ==========================================
    // 4. TESTOVI INTEGRITETA I SIGURNOSTI
    // ==========================================
    @Nested
    @DisplayName("Dodatni testovi integriteta")
    class SecurityAndIntegrityTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Lozinka nikada ne smije biti u JSON odgovoru")
        void passwordShouldBeIgnoredInJson() throws Exception {
            sampleUser.setPassword("STRICTLY_CONFIDENTIAL");
            given(userService.findAll()).willReturn(List.of(sampleUser));

            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].password").doesNotExist());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Provjera Media Type-a (uvijek JSON)")
        void shouldReturnJsonMediaType() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Provjera strukture uloga u JSON-u")
        void shouldHaveCorrectRoleStructureInJson() throws Exception {
            given(userService.findAll()).willReturn(List.of(sampleUser));

            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(jsonPath("$[0].roles[0].name").value("ROLE_ADMIN"));
        }
    }
    @Nested
    @DisplayName("Dodatni testovi za grananja (Branch Coverage)")
    class BranchCoverageTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Ažuriranje rola - nepostojeći korisnik (404/500)")
        void updateRoles_UserNotFound_ShouldThrowException() throws Exception {
            UpdateUserRolesRequestDTO dto = new UpdateUserRolesRequestDTO();
            dto.setRoles(List.of("ROLE_ADMIN"));

            // Simuliramo bacanje iznimke u servisu
            doThrow(new EntityNotFoundException("Korisnik nije pronađen"))
                    .when(userService).updateUserRoles(eq(999L), any());

            mockMvc.perform(put("/api/admin/users/999/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isInternalServerError()); // Ili isNotFound() ako imaš ExceptionHandler
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Dohvat korisnika - prazna baza")
        void getAllUsers_EmptyDatabase_ShouldReturnEmptyList() throws Exception {
            when(userService.findAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            // Ova grana provjerava kako se sustav ponaša kad nema podataka (Empty list branch)
        }
    }
}