// src/main/java/hr/algebra/rapid/logisticsandfleetmanagementsystem/controller/UserController.java

package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

// Uklonite import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.ApplicationUser;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo; // Entitet
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverResponseDTO; // DTO
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ⭐ KLJUČNA PROMJENA: Povratni tip je sada List<DriverResponseDTO>
    @GetMapping("/drivers")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<DriverResponseDTO>> getDrivers() {

        // userService.findDrivers() SADA vraća List<UserInfo>
        List<UserInfo> drivers = userService.findDrivers();

        // DriverResponseDTO::fromUserInfo prima UserInfo -> Mapiranje je ispravno
        List<DriverResponseDTO> driversInfo = drivers.stream()
                .map(DriverResponseDTO::fromUserInfo)
                .collect(Collectors.toList());

        return ResponseEntity.ok(driversInfo);
    }
}