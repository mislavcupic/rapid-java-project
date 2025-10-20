package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate; // DODANO

@Data
public class VehicleResponse {

    private Long id;
    private String licensePlate;
    private String make;
    private String model;
    private Integer modelYear;
    private String fuelType;
    private BigDecimal loadCapacityKg;

    // NOVO: Maintenance fields
    private Long currentMileageKm;
    private LocalDate lastServiceDate;
    private Long nextServiceMileageKm;
    private BigDecimal fuelConsumptionLitersPer100Km;

    // NOVO: Calculated Field (za Frontend Alert)
    private Long remainingKmToService;

    private DriverResponseDTO currentDriver;
}