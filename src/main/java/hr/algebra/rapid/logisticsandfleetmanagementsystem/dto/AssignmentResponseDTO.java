package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentResponseDTO {

    private Long id;


    private DriverResponseDTO driver;
    private VehicleResponse vehicle;
    private ShipmentResponse shipment;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String assignmentStatus;

    // Ovdje se mogu dodati izračunata polja, npr. isOverdue, timeRemaining
}