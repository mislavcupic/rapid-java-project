package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate; // DODANO

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

    // NOVO: Maintenance fields (obavezni za Fleet Management)
    @NotNull(message = "Current mileage is required.")
    @Min(value = 0, message = "Mileage cannot be negative.")
    private Long currentMileageKm;

    private LocalDate lastServiceDate; // Opcionalno

    @NotNull(message = "Next service mileage is required.")
    @Min(value = 0, message = "Next service mileage must be a positive number or zero.")
    private Long nextServiceMileageKm;

    @NotNull(message = "Fuel consumption is required.")
    @DecimalMin(value = "1.0", message = "Consumption must be greater than zero.")
    private BigDecimal fuelConsumptionLitersPer100Km;

    // Samo ID vozača
    private Long currentDriverId;
}//package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;
//
//import lombok.Data;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Min;
//import java.math.BigDecimal;
//
//@Data
//public class VehicleRequest {
//    @NotBlank(message = "License plate is required")
//    private String licensePlate;
//
//    @NotBlank(message = "Make is required")
//    private String make;
//
//    @NotBlank(message = "Model is required")
//    private String model;
//
//    @Min(value = 1900, message = "Invalid year")
//    private Integer modelYear;
//
//    private String fuelType;
//
//    @NotNull(message = "Load capacity is required")
//    @Min(value = 1, message = "Capacity must be positive")
//    private BigDecimal loadCapacityKg;
//
//    // Samo ID vozača
//    private Long currentDriverId;
//}