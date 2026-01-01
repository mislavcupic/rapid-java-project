package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.VehicleService;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleController Tests")
class VehicleControllerTest {

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private VehicleController vehicleController;

    private MockMvc mockMvc;
    private VehicleResponse testVehicleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(vehicleController).build();

        testVehicleResponse = new VehicleResponse();
        testVehicleResponse.setId(1L);
        testVehicleResponse.setLicensePlate("ZG-1234-AB");
        testVehicleResponse.setMake("Mercedes");
        testVehicleResponse.setModel("Actros");
        testVehicleResponse.setModelYear(2020);
        testVehicleResponse.setFuelType("Diesel");
        testVehicleResponse.setLoadCapacityKg(BigDecimal.valueOf(18000));
        testVehicleResponse.setCurrentMileageKm(50000L);
        testVehicleResponse.setNextServiceMileageKm(60000L);
    }

    @Nested
    @DisplayName("GET /api/vehicles")
    class GetAllVehicles {

        @Test
        @DisplayName("Should return all vehicles")
        void getAllVehicles_ShouldReturnList() throws Exception {
            List<VehicleResponse> vehicles = Arrays.asList(testVehicleResponse);
            when(vehicleService.findAllVehicles()).thenReturn(vehicles);

            mockMvc.perform(get("/api/vehicles"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].licensePlate").value("ZG-1234-AB"));

            verify(vehicleService).findAllVehicles();
        }
    }

    @Nested
    @DisplayName("GET /api/vehicles/{id}")
    class GetVehicleById {

        @Test
        @DisplayName("Should return vehicle when exists")
        void getVehicleById_WhenExists_ShouldReturnVehicle() throws Exception {
            when(vehicleService.findVehicleById(1L)).thenReturn(Optional.of(testVehicleResponse));

            mockMvc.perform(get("/api/vehicles/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            verify(vehicleService).findVehicleById(1L);
        }

        @Test
        @DisplayName("Should return 404 when vehicle not found")
        void getVehicleById_WhenNotFound_ShouldReturn404() throws Exception {
            when(vehicleService.findVehicleById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/vehicles/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/vehicles")
    class CreateVehicle {

        @Test
        @DisplayName("Should create vehicle")
        void createVehicle_ShouldReturnCreated() throws Exception {
            when(vehicleService.createVehicle(any(VehicleRequest.class)))
                    .thenReturn(testVehicleResponse);

            String json = """
            {
                "licensePlate": "ZG-1234-AB",
                "make": "Mercedes",
                "model": "Actros",
                "year": 2020,
                "vehicleType": "TRUCK",
                "currentMileageKm": 50000,
                "loadCapacityKg": 18000.0,
                "nextServiceMileageKm": 55000,
                "fuelConsumptionLitersPer100Km": 28.5,
                "fuelType": "DIESEL",
                "vin": "WDB9630281L123456",
                "status": "ACTIVE"
            }
            """;

            mockMvc.perform(post("/api/vehicles")
                            .with(user("admin").roles("ADMIN"))  // ⭐ DODAJ Security bypass
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.licensePlate").value("ZG-1234-AB"))
                    .andExpect(jsonPath("$.make").value("Mercedes"));

            verify(vehicleService).createVehicle(any(VehicleRequest.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/vehicles/{id}")
    class UpdateVehicle {

        @Test
        @DisplayName("Should update vehicle")
        void updateVehicle_ShouldReturnUpdated() throws Exception {
            when(vehicleService.updateVehicle(eq(1L), any(VehicleRequest.class)))
                    .thenReturn(testVehicleResponse);

            String json = """
        {
            "licensePlate": "ZG-1234-AB",
            "make": "Mercedes",
            "model": "Actros",
            "year": 2023,
            "vehicleType": "TRUCK",
            "currentMileageKm": 50000,
            "loadCapacityKg": 18000.0,
            "nextServiceMileageKm": 55000,
            "fuelConsumptionLitersPer100Km": 28.5,
            "fuelType": "DIESEL",
            "vin": "WDB9630281L123456",
            "status": "ACTIVE"
        }
        """;

            mockMvc.perform(put("/api/vehicles/1")
                            .with(user("admin").roles("ADMIN"))  // ⭐ Dodaj security bypass
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.licensePlate").value("ZG-1234-AB"));

            verify(vehicleService).updateVehicle(eq(1L), any(VehicleRequest.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/vehicles/{id}")
    class DeleteVehicle {

        @Test
        @DisplayName("Should delete vehicle")
        void deleteVehicle_ShouldReturnNoContent() throws Exception {
            doNothing().when(vehicleService).deleteVehicle(1L);

            mockMvc.perform(delete("/api/vehicles/1"))
                    .andExpect(status().isNoContent());

            verify(vehicleService).deleteVehicle(1L);
        }
    }

    @Nested
    @DisplayName("Analytics Endpoints")
    class AnalyticsEndpoints {

        @Test
        @DisplayName("GET /details/overdue - should return overdue vehicles")
        void getOverdueMaintenanceVehicles_ShouldReturnList() throws Exception {
            when(vehicleService.findOverdueMaintenanceVehicles()).thenReturn(Arrays.asList(testVehicleResponse));

            mockMvc.perform(get("/api/vehicles/details/overdue"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(vehicleService).findOverdueMaintenanceVehicles();
        }

        @Test
        @DisplayName("GET /details/warning - should return warning vehicles")
        void getWarningMaintenanceVehicles_ShouldReturnList() throws Exception {
            when(vehicleService.findWarningMaintenanceVehicles(5000L)).thenReturn(Arrays.asList(testVehicleResponse));

            mockMvc.perform(get("/api/vehicles/details/warning"))
                    .andExpect(status().isOk());

            verify(vehicleService).findWarningMaintenanceVehicles(5000L);
        }

        @Test
        @DisplayName("GET /details/free - should return free vehicles")
        void getFreeVehiclesDetails_ShouldReturnList() throws Exception {
            when(vehicleService.findFreeVehiclesDetails()).thenReturn(Arrays.asList(testVehicleResponse));

            mockMvc.perform(get("/api/vehicles/details/free"))
                    .andExpect(status().isOk());

            verify(vehicleService).findFreeVehiclesDetails();
        }
    }
}
