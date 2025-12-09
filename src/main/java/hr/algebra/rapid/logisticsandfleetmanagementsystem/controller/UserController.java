package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

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


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final DriverService driverService;


    @GetMapping("/drivers")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')") // Koristim 'ROLE_DISPATCHER' umjesto 'MANAGER'
    public ResponseEntity<List<DriverResponseDTO>> getDrivers() {

        List<DriverResponseDTO> driversInfo = driverService.findAllDrivers();

        return ResponseEntity.ok(driversInfo);
    }
}