package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver;
import lombok.Data; // Koristimo @Data za jednostavnost

@Data
public class DriverResponseDTO {

    private Long id; // ID entiteta Driver
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private String licenseNumber; // Dodao sam i LicenseNumber jer je koristan na Frontendu

    /**
     * Statička metoda za konverziju Driver entiteta u Response DTO.
     * Ova metoda rješava problem "crvenila" nakon promjene u entitetima.
     */
    public static DriverResponseDTO fromDriver(Driver driver) {

        if (driver == null || driver.getUserInfo() == null) {
            return null; // Vraćamo null ako entitet ili UserInfo ne postoje
        }

        DriverResponseDTO dto = new DriverResponseDTO();
        dto.setId(driver.getId());
        dto.setUsername(driver.getUserInfo().getUsername());
        dto.setFirstName(driver.getUserInfo().getFirstName());
        dto.setLastName(driver.getUserInfo().getLastName());

        // KRITIČNA KOREKCIJA: Kreiranje fullName bez oslanjanja na getter u entitetu
        dto.setFullName(driver.getUserInfo().getFirstName() + " " + driver.getUserInfo().getLastName());

        // Dodavanje polja LicenseNumber, ako je potrebno
        dto.setLicenseNumber(driver.getLicenseNumber());

        return dto;
    }
}