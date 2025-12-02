package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverResponseDTO;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DriverController Tests")
class DriverControllerTest {

    @Mock
    private DriverService driverService;

    @InjectMocks
    private DriverController driverController;

    private MockMvc mockMvc;
    private DriverResponseDTO testDriverResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(driverController).build();

        testDriverResponse = new DriverResponseDTO();
        testDriverResponse.setId(1L);
        testDriverResponse.setUsername("driver1");
        testDriverResponse.setFirstName("John");
        testDriverResponse.setLastName("Doe");
        testDriverResponse.setEmail("john.doe@example.com");
        testDriverResponse.setLicenseNumber("DL123456");
        testDriverResponse.setPhoneNumber("+385911234567");
    }

    @Nested
    @DisplayName("GET /api/drivers")
    class GetAllDrivers {

        @Test
        @DisplayName("Should return all drivers")
        void getAllDrivers_ShouldReturnList() throws Exception {
            List<DriverResponseDTO> drivers = Arrays.asList(testDriverResponse);
            when(driverService.findAllDrivers()).thenReturn(drivers);

            mockMvc.perform(get("/api/drivers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].username").value("driver1"));

            verify(driverService).findAllDrivers();
        }
    }

    @Nested
    @DisplayName("GET /api/drivers/{id}")
    class GetDriverById {

        @Test
        @DisplayName("Should return driver when exists")
        void getDriverById_WhenExists_ShouldReturnDriver() throws Exception {
            when(driverService.findDriverById(1L)).thenReturn(Optional.of(testDriverResponse));

            mockMvc.perform(get("/api/drivers/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            verify(driverService).findDriverById(1L);
        }

        @Test
        @DisplayName("Should return 404 when driver not found")
        void getDriverById_WhenNotFound_ShouldReturn404() throws Exception {
            when(driverService.findDriverById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/drivers/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/drivers")
    class CreateDriver {

        @Test
        @DisplayName("Should create driver")
        void createDriver_ShouldReturnCreated() throws Exception {
            when(driverService.createDriver(any(DriverRequestDTO.class))).thenReturn(testDriverResponse);

            String json = """
                {
                    "username": "driver1",
                    "password": "password123",
                    "firstName": "John",
                    "lastName": "Doe",
                    "email": "john.doe@example.com",
                    "licenseNumber": "DL123456",
                    "phoneNumber": "+385911234567",
                    "licenseExpirationDate": "2030-12-31"
                }
                """;

            mockMvc.perform(post("/api/drivers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));

            verify(driverService).createDriver(any(DriverRequestDTO.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/drivers/{id}")
    class UpdateDriver {

        @Test
        @DisplayName("Should update driver")
        void updateDriver_ShouldReturnUpdated() throws Exception {
            when(driverService.updateDriver(eq(1L), any(DriverRequestDTO.class)))
                    .thenReturn(testDriverResponse);

            String json = """
                {
                    "firstName": "Jane",
                    "lastName": "Smith",
                    "email": "jane.smith@example.com"
                }
                """;

            mockMvc.perform(put("/api/drivers/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());

            verify(driverService).updateDriver(eq(1L), any(DriverRequestDTO.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/drivers/{id}")
    class DeleteDriver {

        @Test
        @DisplayName("Should delete driver")
        void deleteDriver_ShouldReturnNoContent() throws Exception {
            doNothing().when(driverService).deleteDriver(1L);

            mockMvc.perform(delete("/api/drivers/1"))
                    .andExpect(status().isNoContent());

            verify(driverService).deleteDriver(1L);
        }
    }
}
