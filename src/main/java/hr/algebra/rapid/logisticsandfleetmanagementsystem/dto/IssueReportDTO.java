package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO za prijavljivanje problema s dostavom
 */
@Data
public class IssueReportDTO {

    @NotBlank(message = "Issue type is required")
    private String issueType; // "ADDRESS_INCORRECT", "RECIPIENT_UNAVAILABLE", "VEHICLE_ISSUE", "OTHER"

    @NotBlank(message = "Description is required")
    private String description;

    private String estimatedDelay; // Procijenjeno kašnjenje (npr. "30 minutes")

    private Double latitude; // GPS lokacija gdje je problem prijavljen
    private Double longitude;
}