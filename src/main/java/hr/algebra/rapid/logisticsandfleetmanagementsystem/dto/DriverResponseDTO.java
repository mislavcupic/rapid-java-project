// src/main/java/hr.algebra.rapid.logisticsandfleetmanagementsystem.dto/DriverResponseDTO.java

package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import lombok.Value;
import lombok.Builder;

@Value
@Builder
public class DriverResponseDTO {


    Long id;
    String username;
    String firstName;
    String lastName;
    String fullName;

    public static DriverResponseDTO fromUserInfo(UserInfo user) {

        String fullName = user.getFirstName() + " " + user.getLastName();

        return DriverResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(fullName)
                .build();
    }
}