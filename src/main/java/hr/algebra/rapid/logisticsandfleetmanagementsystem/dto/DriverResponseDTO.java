// src/main/java/hr/algebra.rapid.logisticsandfleetmanagementsystem.domain/DriverResponseDTO.java

package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import lombok.Value;
import lombok.Builder;

// Koristimo @Value i @Builder za DTO (ne @Entity)
@Value
@Builder
public class DriverResponseDTO {

    // Polja koja će se slati frontendu
    Long id;
    String username;
    String firstName;
    String lastName;

    /**
     * ⭐ KLJUČNO ZA POPRAVAK GREŠKE: Statička metoda za konverziju.
     * Metoda prihvaća vaš entitet (UserInfo) i vraća DTO (DriverResponseDTO).
     */
    public static DriverResponseDTO fromUserInfo(UserInfo user) {
        // Pretpostavljam da UserInfo ima getFirstName() i getLastName()
        return DriverResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName()) // Moraju postojati getteri na UserInfo
                .lastName(user.getLastName())   // Moraju postojati getteri na UserInfo
                .build();
    }
}