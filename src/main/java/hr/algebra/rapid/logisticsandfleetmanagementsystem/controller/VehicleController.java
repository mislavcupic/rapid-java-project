package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.VehicleService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException; // Dodao sam import ako ga nemate
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    // 1. DOHVAĆANJE SVIH (GET)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER', 'ROLE_MANAGER')") // Koristim Authority za usklađenost
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        // Koristimo novu metodu findAllVehicles
        List<VehicleResponse> vehicles = vehicleService.findAllVehicles();
        return ResponseEntity.ok(vehicles);
    }

    // 2. DOHVAĆANJE PO ID-u (GET /{id})
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER', 'ROLE_MANAGER', 'ROLE_DRIVER')")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable Long id) {
        return vehicleService.findVehicleById(id)
                .map(ResponseEntity::ok)
                // Ako vozilo nije pronađeno, bacamo iznimku (konzistentnije)
                .orElseThrow(() -> new ResourceNotFoundException("Vozilo", "ID", id));
    }

    // 3. KREIRANJE (POST)
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest request) {

        // ✅ KRITIČNO: Servis sada vraća VehicleResponse DTO, nema ručnog mapiranja.
        VehicleResponse responseDto = vehicleService.createVehicle(request);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    // 4. AŽURIRANJE (PUT /{id})
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<VehicleResponse> updateVehicle(@PathVariable Long id, @Valid @RequestBody VehicleRequest vehicleDetails) {

        // ✅ KRITIČNO: Servis sada vraća VehicleResponse DTO, nema ručnog mapiranja.
        VehicleResponse responseDto = vehicleService.updateVehicle(id, vehicleDetails);

        return ResponseEntity.ok(responseDto);
    }

    // 5. BRISANJE (DELETE)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/details/overdue")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<List<VehicleResponse>> getOverdueMaintenanceVehicles() {
        // Prag upozorenja je definiran u Service implementaciji, ovdje ga samo pozivamo
        List<VehicleResponse> list = vehicleService.findOverdueMaintenanceVehicles();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/details/warning")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<List<VehicleResponse>> getWarningMaintenanceVehicles() {
        // Prag 5000 km je definiran u Service implementaciji
        List<VehicleResponse> list = vehicleService.findWarningMaintenanceVehicles(5000L);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/details/free")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<List<VehicleResponse>> getFreeVehiclesDetails() {
        List<VehicleResponse> list = vehicleService.findFreeVehiclesDetails();
        return ResponseEntity.ok(list);
    }
}