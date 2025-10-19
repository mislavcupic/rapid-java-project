package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AssignmentResponseDTO {

    private Long id;

    // Ugniježđeni DTO-i za prikaz punih informacija
    private DriverResponseDTO driver;
    private VehicleResponse vehicle;
    private ShipmentResponse shipment;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String assignmentStatus;

    // Ovdje se mogu dodati izračunata polja, npr. isOverdue, timeRemaining
}