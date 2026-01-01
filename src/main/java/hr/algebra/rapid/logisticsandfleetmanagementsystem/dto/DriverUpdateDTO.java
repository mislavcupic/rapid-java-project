package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverUpdateDTO {
    private String firstName;
    private String lastName;
    private String email;

    // Samo polja specifična za vozača
    @NotBlank(message = "Broj licence ne smije biti prazan.")
    private String licenseNumber;

    @NotNull(message = "Datum isteka licence je obavezan.")
    private LocalDate licenseExpirationDate;

    @NotBlank(message = "Telefonski broj je obavezan.")
    @Size(min = 9, max = 15, message = "Format telefonskog broja nije ispravan.")
    private String phoneNumber;
}