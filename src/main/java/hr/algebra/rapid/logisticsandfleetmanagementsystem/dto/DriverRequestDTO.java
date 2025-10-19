package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class DriverRequestDTO {

    // --- POLJA ZA USER INFO (Kreiranje računa) ---
    @NotBlank(message = "Korisničko ime je obavezno.")
    private String username;

    @NotBlank(message = "Lozinka je obavezna.")
    private String password;

    @NotBlank(message = "Ime je obavezno.")
    private String firstName;

    @NotBlank(message = "Prezime je obavezno.")
    private String lastName;

    // ✅ ISPRAVLJENO: Polje za Email (mora biti prisutno i validirano)
    @NotBlank(message = "E-mail je obavezan.")
    @Email(message = "E-mail format nije ispravan.")
    private String email;

    // --- POLJA ZA DRIVER PROFIL (Logistika) ---

    @NotBlank(message = "Broj licence ne smije biti prazan.")
    @Size(min = 1, message = "Broj licence ne smije biti prazan")
    private String licenseNumber;

    @NotNull(message = "Datum isteka licence je obavezan.")
    private LocalDate licenseExpirationDate;

    @NotBlank(message = "Telefonski broj je obavezan.")
    @Size(min = 9, max = 15, message = "Format telefonskog broja nije ispravan.")
    private String phoneNumber;
}