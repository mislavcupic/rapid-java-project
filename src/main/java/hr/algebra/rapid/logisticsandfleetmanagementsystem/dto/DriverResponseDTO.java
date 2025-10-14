package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver; // Uvoz novog entiteta Driver
import lombok.Value;
import lombok.Builder;

// Ako koristite @Data umjesto @Value/Builder:
// import lombok.Data;
// @Data public class DriverResponseDTO { ... }

@Value
@Builder
public class DriverResponseDTO {

    Long id; // ID entiteta Driver
    String username;
    String firstName;
    String lastName;
    String fullName;

    /**
     * Statička metoda za konverziju Driver entiteta u Response DTO.
     * OVA METODA JE KRITIČNA ZA RAD AssignmentServiceImpl-a.
     */
    public static DriverResponseDTO fromDriver(Driver driver) {

        // Logika se oslanja na postojanje UserInfo unutar Driver entiteta
        if (driver == null || driver.getUserInfo() == null) {
            // Možete baciti iznimku ili vratiti null/default, ali ovo sprječava NullPointerException
            return null;
        }

        // Dohvaćanje podataka iz UserInfo (koji je ugniježđen u Driver entitetu)
        String fullName = driver.getFullName();

        return DriverResponseDTO.builder()
                .id(driver.getId())
                .username(driver.getUserInfo().getUsername())
                .firstName(driver.getUserInfo().getFirstName())
                .lastName(driver.getUserInfo().getLastName())
                .fullName(fullName)
                .build();
    }
}