package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test za Driver domain
 */
class DriverTest {

    private Driver driver;
    private UserInfo userInfo;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setFirstName("John");
        userInfo.setLastName("Doe");

        vehicle = new Vehicle();
        vehicle.setId(2L);

        driver = new Driver();
    }

    @Test
    void setAndGetId_ShouldWork() {
        driver.setId(1L);
        assertThat(driver.getId()).isEqualTo(1L);
    }

    @Test
    void setAndGetUserInfo_ShouldWork() {
        driver.setUserInfo(userInfo);
        assertThat(driver.getUserInfo()).isEqualTo(userInfo);
        assertThat(driver.getUserInfo().getId()).isEqualTo(1L);
    }

    @Test
    void setAndGetLicenseNumber_ShouldWork() {
        driver.setLicenseNumber("LIC12345");
        assertThat(driver.getLicenseNumber()).isEqualTo("LIC12345");
    }

    @Test
    void setAndGetPhoneNumber_ShouldWork() {
        driver.setPhoneNumber("+385911234567");
        assertThat(driver.getPhoneNumber()).isEqualTo("+385911234567");
    }

    @Test
    void setAndGetLicenseExpirationDate_ShouldWork() {
        LocalDate expDate = LocalDate.of(2030, 12, 31);
        driver.setLicenseExpirationDate(expDate);
        assertThat(driver.getLicenseExpirationDate()).isEqualTo(expDate);
    }

    @Test
    void setAndGetCurrentVehicle_ShouldWork() {
        driver.setCurrentVehicle(vehicle);
        assertThat(driver.getCurrentVehicle()).isPresent();
        assertThat(driver.getCurrentVehicle().get()).isEqualTo(vehicle);
    }

    @Test
    void getCurrentVehicle_WhenNull_ShouldReturnEmpty() {
        driver.setCurrentVehicle(null);
        assertThat(driver.getCurrentVehicle()).isEmpty();
    }

    @Test
    void getFullName_WhenUserInfoExists_ShouldReturnFullName() {
        driver.setUserInfo(userInfo);
        assertThat(driver.getFullName()).isEqualTo("John Doe");
    }

    @Test
    void getFullName_WhenUserInfoNull_ShouldReturnNA() {
        driver.setUserInfo(null);
        assertThat(driver.getFullName()).isEqualTo("N/A");
    }

    @Test
    void equals_ShouldCompareCorrectly() {
        Driver driver1 = new Driver();
        driver1.setId(1L);

        Driver driver2 = new Driver();
        driver2.setId(1L);

        assertThat(driver1).isEqualTo(driver2);
    }
}
