package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class DriverRequestDTO {

    // ID povezanog korisničkog računa (UserInfo ID)
    @NotNull(message = "User ID for driver is required")
    private Long userInfoId;

    // Broj vozačke dozvole, mora biti jedinstven
    @Size(min = 1, message = "License number must not be empty")
    private String licenseNumber;

    // Datum isteka dozvole (ključan za logistiku)
    @NotNull(message = "License expiration date is required")
    private LocalDate licenseExpirationDate;

    // Kontaktni broj vozača
    @Size(min = 9, max = 15, message = "Phone number format invalid")
    private String phoneNumber;

    // Opcionalno, ako je potrebno unijeti ID vozila prilikom kreiranja vozača
    // private Long currentVehicleId;
}