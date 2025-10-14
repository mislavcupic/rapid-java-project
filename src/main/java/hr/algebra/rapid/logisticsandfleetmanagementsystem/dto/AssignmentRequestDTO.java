package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AssignmentRequestDTO {

    // ID entiteta Driver (klasa Driver, ne app_user)
    @NotNull(message = "Driver ID is required")
    private Long driverId;

    // ID entiteta Vehicle
    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    // ID entiteta Shipment
    @NotNull(message = "Shipment ID is required")
    private Long shipmentId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    // Opcijski, jer endTime može biti nepoznat pri kreiranju
    private LocalDateTime endTime;
}