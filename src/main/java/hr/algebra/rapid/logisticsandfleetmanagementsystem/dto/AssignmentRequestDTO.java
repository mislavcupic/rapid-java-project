package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssignmentRequestDTO {
    @NotNull(message = "Driver ID is required")
    private Long driverId;

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    // PROMJENA: Sada primamo listu ID-ova pošiljaka
    @NotNull(message = "At least one Shipment ID is required")
    private List<Long> shipmentIds;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    private LocalDateTime endTime;
}