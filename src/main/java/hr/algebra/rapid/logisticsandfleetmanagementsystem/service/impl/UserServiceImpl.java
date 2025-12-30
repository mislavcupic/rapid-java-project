package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.RegisterRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.DriverRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRoleRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    public static final String ROLE_DRIVER = "ROLE_DRIVER";
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DriverRepository driverRepository;

    @Override
    public List<UserInfo> findAll() {
        return userRepository.findAll();
    }

    @Override
    public UserInfo findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik", "ID", id));
    }

    @Override
    @Transactional
    public UserInfo registerUser(RegisterRequestDTO registerRequest) {
        if (existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Korisničko ime već postoji!");
        }

        if (existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email već postoji!");
        }

        UserRole driverRole = userRoleRepository.findByName(ROLE_DRIVER)
                .orElseThrow(() -> new ResourceNotFoundException("Uloga", "ime", ROLE_DRIVER));

        UserInfo newUser = new UserInfo();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setLastName(registerRequest.getLastName());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setIsEnabled(true);

        // ✅ KONAČNO RJEŠENJE: Koristi List umjesto Set
        List<UserRole> rolesList = new ArrayList<>();
        rolesList.add(driverRole);
        newUser.setRoles(rolesList);

        UserInfo savedUser = userRepository.save(newUser);

        Driver driver = new Driver();
        driver.setUserInfo(savedUser);
        driver.setLicenseNumber(registerRequest.getLicenseNumber());
        driver.setLicenseExpirationDate(registerRequest.getLicenseExpirationDate() != null
                ? registerRequest.getLicenseExpirationDate()
                : LocalDate.now().plusYears(10));
        driver.setPhoneNumber(registerRequest.getPhoneNumber());

        driverRepository.save(driver);

        log.info("Registriran novi korisnik: {} sa ulogom ROLE_DRIVER", savedUser.getUsername());

        return savedUser;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username) != null;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findAll().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }
    @Override
    @Transactional
    public UserInfo updateUserRoles(Long userId, List<String> roleNames) {
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik", "ID", userId));

        if (roleNames == null || roleNames.isEmpty()) {
            throw new IllegalArgumentException("Korisnik mora imati barem jednu ulogu!");
        }

        // 1. Postavi uloge
        Set<UserRole> newRoles = roleNames.stream()
                .map(roleName -> userRoleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Uloga", "ime", roleName)))
                .collect(Collectors.toSet());

        user.getRoles().clear();
        user.getRoles().addAll(newRoles);
        UserInfo updatedUser = userRepository.saveAndFlush(user);

        // 2. LOGIKA ZA VOZAČA (Sinkronizacija)
        // ROLE_DRIVER je konstanta koju koristiš u kodu
        if (roleNames.contains(ROLE_DRIVER)) {
            if (!driverRepository.existsByUserInfo(user)) {
                Driver newDriver = new Driver();
                newDriver.setUserInfo(user);

                // Postavljamo generirani broj licence
                newDriver.setLicenseNumber("TEMP-" + user.getId());

                // Postavljamo datum (LocalDate) - osiguraj da Driver entitet koristi LocalDate
                newDriver.setLicenseExpirationDate(LocalDate.now().plusYears(10));

                // VAŽNO: Tvoj log kaže da "N/A" ne prolazi validaciju (min 9 znakova).
                // Koristimo ili korisnikov broj ili dummy broj od 10 znamenki.
                String validPhone = (newDriver.getPhoneNumber() != null && newDriver.getPhoneNumber().length() >= 9)
                        ? newDriver.getPhoneNumber()
                        : "000000000";
                newDriver.setPhoneNumber(validPhone);

                driverRepository.save(newDriver);
                log.info("Admin dashboard: Kreiran Driver zapis za korisnika {}", user.getUsername());
            }
        } else {
            // Ako uloga ROLE_DRIVER više nije u listi, obriši ga iz tablice vozača
            // Osiguraj da tvoj repository ima metodu deleteByUserInfo ili koristi find pa delete
            driverRepository.findByUserInfo(user).ifPresent(driver -> {
                driverRepository.delete(driver);
                log.info("Admin dashboard: Uklonjen Driver zapis za korisnika {}", user.getUsername());
            });
        }

        return updatedUser;
    }
//    @Override
//    @Transactional
//    public UserInfo updateUserRoles(Long userId, List<String> roleNames) {
//        UserInfo user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("Korisnik", "ID", userId));
//
//        if (roleNames == null || roleNames.isEmpty()) {
//            throw new IllegalArgumentException("Korisnik mora imati barem jednu ulogu!");
//        }
//
//        // 1. Postavi uloge (tvoj postojeći dio)
//        Set<UserRole> newRoles = roleNames.stream()
//                .map(roleName -> userRoleRepository.findByName(roleName)
//                        .orElseThrow(() -> new ResourceNotFoundException("Uloga", "ime", roleName)))
//                .collect(Collectors.toSet());
//
//        user.getRoles().clear();
//        user.getRoles().addAll(newRoles);
//        UserInfo updatedUser = userRepository.saveAndFlush(user);
//
//        // 2. LOGIKA ZA VOZAČA (Sinkronizacija)
//        // Provjeri sadrži li lista stringova ulogu "ROLE_DRIVER"
//        if (roleNames.contains(ROLE_DRIVER)) {
//            // Provjeri postoji li već zapis u driver tablici za ovog usera
//            if (!driverRepository.existsByUserInfo(user)) {
//                Driver newDriver = new Driver();
//                newDriver.setUserInfo(user);
//
//                // MORAŠ postaviti ove vrijednosti jer su NOT NULL u bazi
//                // Budući da admin nema ova polja na formi, stavljamo privremene podatke
//                newDriver.setLicenseNumber("TEMP-" + user.getUsername().toUpperCase());
//
//                // Koristimo LocalDateTime jer tvoj log kaže da baza to očekuje
//                newDriver.setLicenseExpirationDate(LocalDate.from(LocalDateTime.now().plusYears(10)));
//                newDriver.setPhoneNumber("N/A");
//
//                driverRepository.save(newDriver);
//                log.info("Admin dashboard: Kreiran Driver zapis za korisnika {}", user.getUsername());
//            }
//        } else {
//            // Ako uloga ROLE_DRIVER više nije u listi, obriši ga iz tablice vozača
//            if (driverRepository.existsByUserInfo(user)) {
//                driverRepository.deleteByUserInfo(user);
//                log.info("Admin dashboard: Uklonjen Driver zapis za korisnika {}", user.getUsername());
//            }
//        }
//
//        return updatedUser;
//    }


    @Override
    @Transactional
    public void deleteUser(Long userId) {
        UserInfo user = findById(userId);

        driverRepository.findByUserInfoId(userId)
                .ifPresent(driverRepository::delete);

        userRepository.delete(user);

        log.info("Korisnik obrisan: {}", user.getUsername());
    }
}