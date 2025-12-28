package hr.algebra.rapid.logisticsandfleetmanagementsystem.repository;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    // 1. Pronalazi Driver profil po ID-u povezanog UserInfo računa
    Optional<Driver> findByUserInfoId(Long userInfoId);

    // 2. Pronalazi Driver profil po broju dozvole
    Optional<Driver> findByLicenseNumber(String licenseNumber);

    // Standardne JpaRepository metode (findAll, findById, save, delete...) su implicitno uključene.
}