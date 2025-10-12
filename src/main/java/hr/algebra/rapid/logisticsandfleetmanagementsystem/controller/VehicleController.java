package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Vehicle; // Potreban jer servis vraća Vehicle
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.VehicleService;
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

    // 1. DOHVAĆANJE SVIH (GET) - ISPRAVNO: Vraća VehicleResponse DTO
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        List<VehicleResponse> vehicles = vehicleService.findAll();
        return ResponseEntity.ok(vehicles);
    }

    // 2. KREIRANJE (POST) - POPRAVLJENO: Vraća VehicleResponse, koristi mapToResponse()
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    // Promijenjeno da VRAĆA VehicleResponse
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest request) {

        // 1. Servis stvara i vraća JPA entitet Vehicle.
        Vehicle newVehicleEntity = vehicleService.createVehicle(request);

        // 2. KRITIČNO: Mapiranje Vehicle entiteta u VehicleResponse DTO.
        VehicleResponse responseDto = vehicleService.mapToResponse(newVehicleEntity);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    // 3. DOHVAĆANJE PO ID-u (GET /{id}) - ISPRAVNO: Vraća VehicleResponse DTO
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable Long id) {
        return vehicleService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 4. AŽURIRANJE (PUT /{id}) - POPRAVLJENO: Vraća VehicleResponse, koristi mapToResponse()
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponse> updateVehicle(@PathVariable Long id, @Valid @RequestBody VehicleRequest vehicleDetails) {

        // 1. Servis ažurira i vraća JPA entitet Vehicle.
        Vehicle updatedVehicleEntity = vehicleService.updateVehicle(id, vehicleDetails);

        // 2. KRITIČNO: Mapiranje Vehicle entiteta u VehicleResponse DTO.
        VehicleResponse responseDto = vehicleService.mapToResponse(updatedVehicleEntity);

        return ResponseEntity.ok(responseDto);
    }

    // 5. BRISANJE (DELETE) - ISPRAVNO
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}