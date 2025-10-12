package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data // Anotacija od Lomboka za gettere, settere, toString, itd.
public class VehicleResponse {

    private Long id;
    private String licensePlate;
    private String make;
    private String model;
    private Integer modelYear;
    private String fuelType;
    private BigDecimal loadCapacityKg;

    // Ovo polje vraća samo ID vozača, čime izbjegavamo slanje cijelog UserInfo entiteta
    private Long currentDriverId;

    // Opcija: Možete dodati i ime za lakši prikaz
    private String currentDriverFullName;
}