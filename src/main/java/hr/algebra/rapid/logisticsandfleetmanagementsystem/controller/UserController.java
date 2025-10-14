package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.UserService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.DriverService;
import lombok.RequiredArgsConstructor; // Preporučeno koristiti Lombok @RequiredArgsConstructor za injekciju
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Koristite @RequiredArgsConstructor umjesto ručnog konstruktora
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final DriverService driverService;

    // Ovdje treba biti samo CRUD za generičke UserInfo račune (Admin, Dispečer, kreiranje usera).
    // ... Dodajte standardni CRUD za UserInfo ovdje (npr. findAll, findById, create) ...

    // ⭐ KRITIČNO: Ova metoda se mora premjestiti u DriverController,
    // ali ako je nužno da ostane, mora KORISTITI DriverService.
    @GetMapping("/drivers")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')") // Koristim 'ROLE_DISPATCHER' umjesto 'MANAGER'
    public ResponseEntity<List<DriverResponseDTO>> getDrivers() {

        // ✅ ISPRAVNO: Koristite metodu iz DriverService koja već dohvaća i mapira Driver entitete.
        List<DriverResponseDTO> driversInfo = driverService.findAllDrivers();

        // Stare, neispravne linije su izbačene:
        // List<UserInfo> drivers = userService.findDrivers();
        // List<DriverResponseDTO> driversInfo = drivers.stream().map(DriverResponseDTO::fromDriver)...

        return ResponseEntity.ok(driversInfo);
    }
}