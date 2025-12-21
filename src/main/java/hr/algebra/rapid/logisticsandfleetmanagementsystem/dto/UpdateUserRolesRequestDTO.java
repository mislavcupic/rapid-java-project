package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRolesRequestDTO {
    private List<String> roles; // npr. ["ROLE_ADMIN", "ROLE_DRIVER"]
}