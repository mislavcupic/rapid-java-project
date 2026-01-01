package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("name")
    public String getName() {
        if (make != null && model != null) {
            return make + " " + model;
        }
        if (make != null) {
            return make;
        }
        if (model != null) {
            return model;
        }
        return "N/A";
    }

    @JsonProperty("driver")
    public String getDriver() {
        if (currentDriver != null && currentDriver.getFullName() != null) {
            return currentDriver.getFullName();
        }
        return "N/A";
    }

    @JsonProperty("remainingKm")
    public Long getRemainingKm() {
        return remainingKmToService != null ? remainingKmToService : 0L;
    }
}