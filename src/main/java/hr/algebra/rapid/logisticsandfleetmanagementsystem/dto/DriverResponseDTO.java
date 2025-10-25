package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver;
import lombok.Data;
import java.time.LocalDate;

@Data
public class DriverResponseDTO {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String licenseNumber;
    private LocalDate licenseExpirationDate;
    private String phoneNumber;

    public static DriverResponseDTO fromDriver(Driver driver) {

        if (driver == null || driver.getUserInfo() == null) {
            return null;
        }

        DriverResponseDTO dto = new DriverResponseDTO();
        dto.setId(driver.getId());
        dto.setUsername(driver.getUserInfo().getUsername());
        dto.setFirstName(driver.getUserInfo().getFirstName());
        dto.setLastName(driver.getUserInfo().getLastName());
        dto.setFullName(driver.getUserInfo().getFirstName() + " " + driver.getUserInfo().getLastName());


        dto.setEmail(driver.getUserInfo().getEmail());
        dto.setLicenseNumber(driver.getLicenseNumber());
        dto.setLicenseExpirationDate(driver.getLicenseExpirationDate());
        dto.setPhoneNumber(driver.getPhoneNumber());

        return dto;
    }
}