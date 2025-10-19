package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShipmentRequest {

    @NotBlank(message = "Tracking number is required")
    private String trackingNumber;

    @NotBlank(message = "Origin address is required")
    private String originAddress;

    // ✅ UKLONJENA @NotNull VALIDACIJA: Koordinate smiju biti NULL/prazne.
    // Frontend ih automatski popunjava.
    private Double originLatitude;
    private Double originLongitude;


    @NotBlank(message = "Destination address is required")
    private String destinationAddress;

    // ✅ UKLONJENA @NotNull VALIDACIJA
    private Double destinationLatitude;
    private Double destinationLongitude;


    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.1", message = "Weight must be positive")
    private BigDecimal weightKg;

    // Volumen i Vrijednost su opcionalni
    private BigDecimal volumeM3;

    private BigDecimal shipmentValue;


    @NotNull(message = "Expected delivery date is required")
    @FutureOrPresent(message = "Delivery date cannot be in the past")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime expectedDeliveryDate;

    private String description;

    private String status;
}