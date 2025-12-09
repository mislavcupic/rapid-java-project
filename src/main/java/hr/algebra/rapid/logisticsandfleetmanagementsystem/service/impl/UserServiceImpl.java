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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

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

        UserRole driverRole = userRoleRepository.findByName("ROLE_DRIVER")
                .orElseThrow(() -> new ResourceNotFoundException("Uloga", "ime", "ROLE_DRIVER"));

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

        Set<UserRole> newRoles = roleNames.stream()
                .map(roleName -> userRoleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Uloga", "ime", roleName)))
                .collect(Collectors.toSet());

        user.getRoles().clear();
        user.getRoles().addAll(newRoles);

        UserInfo updatedUser = userRepository.saveAndFlush(user);

        log.info("Uloge ažurirane za korisnika: {}. Nove uloge: {}",
                updatedUser.getUsername(),
                updatedUser.getRoles().stream().map(UserRole::getName).collect(Collectors.toSet()));

        return updatedUser;
    }

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