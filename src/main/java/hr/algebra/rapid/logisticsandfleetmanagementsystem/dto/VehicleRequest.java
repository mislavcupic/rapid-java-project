package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

@Data
public class VehicleRequest {
    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotBlank(message = "Make is required")
    private String make;

    @NotBlank(message = "Model is required")
    private String model;

    @Min(value = 1900, message = "Invalid year")
    private Integer modelYear;

    private String fuelType;

    @NotNull(message = "Load capacity is required")
    @Min(value = 1, message = "Capacity must be positive")
    private BigDecimal loadCapacityKg;

    // Samo ID vozača
    private Long currentDriverId;
}