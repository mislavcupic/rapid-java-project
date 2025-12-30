package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;


import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverUpdateDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.DriverService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


import java.util.List;

/**
 * Kontroler za upravljanje logističkim profilima vozača (Driver entitet).
 * Koristi DriverService za CRUD operacije.
 */
@RestController
@RequestMapping("/api/drivers") // Novi, čisti endpoint
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;


    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<List<DriverResponseDTO>> getAllDrivers() {
        List<DriverResponseDTO> drivers = driverService.findAllDrivers();
        return ResponseEntity.ok(drivers);
    }

    /** Dohvaća Driver profil po ID-u */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER') or " +
            // Omogućuje vozaču da vidi svoj profil na temelju JWT tokena
            "@driverService.getDriverIdFromUsername(authentication.name) == #id")
    public ResponseEntity<DriverResponseDTO> getDriverById(@PathVariable Long id) {
        DriverResponseDTO driver = driverService.findDriverById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "ID", id));
        return ResponseEntity.ok(driver);
    }



    /** Kreira novi Driver profil i povezuje ga s postojećim UserInfo */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<DriverResponseDTO> createDriver(@Valid @RequestBody DriverRequestDTO request) {
        DriverResponseDTO newDriver = driverService.createDriver(request);
        return new ResponseEntity<>(newDriver, HttpStatus.CREATED);
    }

    // -----------------------------------------------------------------
    // UPDATE (Ažuriranje)
    // -----------------------------------------------------------------

    /** Ažurira postojeći Driver profil */
//    @PutMapping("/{id}")
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
//    public ResponseEntity<DriverResponseDTO> updateDriver(@PathVariable Long id,
//                                                          @Valid @RequestBody DriverRequestDTO request) {
//        DriverResponseDTO updatedDriver = driverService.updateDriver(id, request);
//        return ResponseEntity.ok(updatedDriver);
//    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<DriverResponseDTO> updateDriver(
            @PathVariable Long id,
            @Valid @RequestBody DriverUpdateDTO request) { // <-- KLJUČNA IZMJENA: Koristimo DriverUpdateDTO

        // Proslijeđujemo novi DTO u servis
        DriverResponseDTO updatedDriver = driverService.updateDriver(id, request);
        return ResponseEntity.ok(updatedDriver);
    }

    // -----------------------------------------------------------------
    // DELETE (Brisanje)
    // -----------------------------------------------------------------

    /** Briše Driver profil (ne briše UserInfo) */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Samo Admin smije brisati profile
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        driverService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }


}