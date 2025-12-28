package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssignmentResponseDTO {
    private Long id;
    private DriverResponseDTO driver;
    private VehicleResponse vehicle;

    // PROMJENA: Odgovor sada sadrži listu pošiljaka
    private List<ShipmentResponse> shipments;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String assignmentStatus;
}