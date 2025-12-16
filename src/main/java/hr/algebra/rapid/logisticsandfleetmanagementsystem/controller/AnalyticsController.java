package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleAnalyticsResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AnalyticsService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final VehicleService vehicleService;


    @GetMapping("/shipments/average-active-weight")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<Double> getAverageActiveShipmentWeight() {
        Double averageWeight = analyticsService.getAverageActiveShipmentWeight();
        return ResponseEntity.ok(averageWeight);
    }


    @PostMapping("/shipments/mark-overdue")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> bulkMarkOverdue() {
        int updatedRows = analyticsService.bulkMarkOverdue();

        String responseMessage = String.format("Uspješno ažurirano %d pošiljaka u status 'OVERDUE'.", updatedRows);

        return ResponseEntity.ok(responseMessage);
    }
    @GetMapping("/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<VehicleAnalyticsResponse> getVehicleAlertStatus() {
        // Poziva implementaciju iz JdbcAnalyticsServiceImpl (koja zove VehicleService)
        VehicleAnalyticsResponse response = analyticsService.getVehicleAlertStatus();
        return ResponseEntity.ok(response);
    }
    @GetMapping("/vehicles/overdue")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<List<VehicleResponse>> getOverdueVehicles() {
        List<VehicleResponse> vehicles = vehicleService.findOverdueMaintenanceVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/vehicles/warning")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<List<VehicleResponse>> getWarningVehicles(
            @RequestParam(defaultValue = "5000") Long threshold
    ) {
        List<VehicleResponse> vehicles = vehicleService.findWarningMaintenanceVehicles(threshold);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("vehicles/free")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<List<VehicleResponse>> getFreeVehicles() {
        List<VehicleResponse> vehicles = vehicleService.findFreeVehiclesDetails();
        return ResponseEntity.ok(vehicles);
    }
}
