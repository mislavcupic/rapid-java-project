package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// U klasi ApplicationUser.java
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_user")
public class ApplicationUser {

    // ... id, username, password polja ...

    @Id
    private Long id;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            // OVA LINIJA JE VEĆ BILA ISPRAVNA
            joinColumns = @JoinColumn(name = "user_id"),

            // DODATNA EKSPLICITNOST DA NADJAČATE KONVENCIJE
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<UserRole> roles;
}
