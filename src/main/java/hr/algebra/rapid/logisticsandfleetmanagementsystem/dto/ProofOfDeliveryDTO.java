package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO za Proof of Delivery - podaci koje Driver šalje kada završava dostavu
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProofOfDeliveryDTO {

    @NotBlank(message = "Recipient name is required")
    private String recipientName;

    private String recipientSignature; // Base64 encoded signature image

    private String photoUrl; // URL fotografije dostave (ako je uploadana)

    private String notes; // Dodatne napomene (npr. "Ostavljeno na recepciji")

    private Double latitude; // GPS koordinate
    private Double longitude;
}