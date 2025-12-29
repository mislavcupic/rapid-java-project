package hr.algebra.rapid.logisticsandfleetmanagementsystem.repository;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserInfo, Long> {
    // Vraća listu JPA Entiteta (UserInfo)
    List<UserInfo> findByRoles_Name(String roleName);

    // Vraća JPA Entitet
    UserInfo findByUsername(String username);

    void deleteByUsername(String username);

    boolean existsByUsername(String s);

    boolean existsByEmail(@NotBlank(message = "E-mail je obavezan.") @Email(message = "E-mail format nije ispravan.") String email);
}


