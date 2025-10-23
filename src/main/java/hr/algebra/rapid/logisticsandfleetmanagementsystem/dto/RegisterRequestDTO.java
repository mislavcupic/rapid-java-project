package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequestDTO {
    
    @NotBlank(message = "Korisničko ime je obavezno")
    @Size(min = 3, max = 50, message = "Korisničko ime mora imati između 3 i 50 znakova")
    private String username;
    
    @NotBlank(message = "Lozinka je obavezna")
    @Size(min = 6, message = "Lozinka mora imati najmanje 6 znakova")
    private String password;
    
    @NotBlank(message = "Ime je obavezno")
    private String firstName;
    
    @NotBlank(message = "Prezime je obavezno")
    private String lastName;
    
    @NotBlank(message = "Email je obavezan")
    @Email(message = "Email mora biti valjan")
    private String email;
}
