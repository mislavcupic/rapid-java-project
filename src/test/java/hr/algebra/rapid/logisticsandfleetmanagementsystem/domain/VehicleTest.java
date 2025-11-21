package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleTest {

    private Vehicle vehicle;
    private Driver driver;

    @BeforeEach
    void setUp() {
        driver = new Driver();
        driver.setId(1L);
        vehicle = new Vehicle();
    }

    @Test
    void setAndGetId_ShouldWork() {
        vehicle.setId(1L);
        assertThat(vehicle.getId()).isEqualTo(1L);
    }

    @Test
    void setAndGetLicensePlate_ShouldWork() {
        vehicle.setLicensePlate("ZG-1234-AB");
        assertThat(vehicle.getLicensePlate()).isEqualTo("ZG-1234-AB");
    }

    @Test
    void setAndGetMake_ShouldWork() {
        vehicle.setMake("Mercedes");
        assertThat(vehicle.getMake()).isEqualTo("Mercedes");
    }

    @Test
    void setAndGetModel_ShouldWork() {
        vehicle.setModel("Sprinter");
        assertThat(vehicle.getModel()).isEqualTo("Sprinter");
    }

    @Test
    void setAndGetYear_ShouldWork() {
        vehicle.setYear(2020);
        assertThat(vehicle.getYear()).isEqualTo(2020);
    }

    @Test
    void setAndGetFuelType_ShouldWork() {
        vehicle.setFuelType("Diesel");
        assertThat(vehicle.getFuelType()).isEqualTo("Diesel");
    }

    @Test
    void setAndGetLoadCapacityKg_ShouldWork() {
        vehicle.setLoadCapacityKg(BigDecimal.valueOf(3500));
        assertThat(vehicle.getLoadCapacityKg()).isEqualByComparingTo(BigDecimal.valueOf(3500));
    }

    @Test
    void setAndGetCurrentMileageKm_ShouldWork() {
        vehicle.setCurrentMileageKm(50000L);
        assertThat(vehicle.getCurrentMileageKm()).isEqualTo(50000L);
    }

    @Test
    void setAndGetLastServiceDate_ShouldWork() {
        LocalDate date = LocalDate.of(2024, 6, 1);
        vehicle.setLastServiceDate(date);
        assertThat(vehicle.getLastServiceDate()).isEqualTo(date);
    }

    @Test
    void setAndGetNextServiceMileageKm_ShouldWork() {
        vehicle.setNextServiceMileageKm(60000L);
        assertThat(vehicle.getNextServiceMileageKm()).isEqualTo(60000L);
    }

    @Test
    void setAndGetFuelConsumptionLitersPer100Km_ShouldWork() {
        vehicle.setFuelConsumptionLitersPer100Km(BigDecimal.valueOf(8.5));
        assertThat(vehicle.getFuelConsumptionLitersPer100Km()).isEqualByComparingTo(BigDecimal.valueOf(8.5));
    }

    @Test
    void setAndGetCurrentDriver_ShouldWork() {
        vehicle.setCurrentDriver(driver);
        assertThat(vehicle.getCurrentDriver()).isEqualTo(driver);
    }

    @Test
    void getCurrentDriverOptional_WhenDriverExists_ShouldReturnPresent() {
        vehicle.setCurrentDriver(driver);
        assertThat(vehicle.getCurrentDriverOptional()).isPresent();
        assertThat(vehicle.getCurrentDriverOptional()).contains(driver);

    }

    @Test
    void getCurrentDriverOptional_WhenDriverNull_ShouldReturnEmpty() {
        vehicle.setCurrentDriver(null);
        assertThat(vehicle.getCurrentDriverOptional()).isEmpty();
    }
}
