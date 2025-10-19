package hr.algebra.rapid.logisticsandfleetmanagementsystem.repository;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    // Ključna metoda: Dohvaćanje uloge po imenu (npr. "ROLE_DRIVER")
    Optional<UserRole> findByName(String name);
}