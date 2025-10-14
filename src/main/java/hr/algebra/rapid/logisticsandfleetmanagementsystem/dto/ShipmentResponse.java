package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShipmentResponse {

    private Long id;
    private String trackingNumber;
    private String description;
    private BigDecimal weightKg;
    private BigDecimal volumeM3;
    private String originAddress;
    private String destinationAddress;
    private String status; // Status je obavezan u izlaznom DTO-u
    private LocalDateTime expectedDeliveryDate;
    private LocalDateTime actualDeliveryDate; // Može biti null
}