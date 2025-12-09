package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.RegisterRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.DriverRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRoleRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DriverRepository driverRepository;

    public UserServiceImpl(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder,
            DriverRepository driverRepository
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.driverRepository = driverRepository;
    }

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
        // 1. Provjera postoji li username
        if (existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Korisničko ime '" + registerRequest.getUsername() + "' je već zauzeto!");
        }

        // 2. Provjera postoji li email
        if (existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email '" + registerRequest.getEmail() + "' je već registriran!");
        }

        // 3. Dohvati ROLE_DRIVER kao default ulogu
        UserRole driverRole = userRoleRepository.findByName("ROLE_DRIVER")
                .orElseThrow(() -> new ResourceNotFoundException("Uloga", "ime", "ROLE_DRIVER"));

        // 4. Kreiraj novog korisnika
        UserInfo newUser = new UserInfo();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setLastName(registerRequest.getLastName());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setIsEnabled(true);
        newUser.setRoles(new ArrayList<>(Arrays.asList(driverRole)));

        // 5. Spremi UserInfo u bazu
        UserInfo savedUser = userRepository.save(newUser);

        log.info("Novi korisnik registriran: {} (ID: {}) sa ulogom ROLE_DRIVER",
                savedUser.getUsername(), savedUser.getId());

        // 6. Automatski kreiraj Driver profil
        Driver driver = new Driver();
        driver.setUserInfo(savedUser);
        driver.setLicenseNumber(registerRequest.getLicenseNumber()); //reqdto umj pending
        driver.setLicenseExpirationDate(LocalDate.now().plusYears(10));
        driver.setPhoneNumber(registerRequest.getPhoneNumber());

        driverRepository.save(driver);

        log.info("Driver profil automatski kreiran za korisnika: {}", savedUser.getUsername());

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

        log.info("PRIJE ažuriranja - User {} ima uloge: {}",
                user.getUsername(),
                user.getRoles().stream().map(UserRole::getName).toList());

        List<UserRole> newRoles = roleNames.stream()
                .map(roleName -> userRoleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Uloga", "ime", roleName)))
                .toList();

        user.getRoles().clear();
        userRepository.flush();

        user.getRoles().addAll(newRoles);

        UserInfo updated = userRepository.saveAndFlush(user);

        log.info("Uloge uspješno ažurirane za korisnika {} (ID: {}). NOVE uloge: {}",
                updated.getUsername(), userId, roleNames);

        log.warn("Korisnik {} MORA SE PONOVNO PRIJAVITI da bi aktivirao nove uloge!",
                updated.getUsername());

        return updated;
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        UserInfo user = findById(userId);
        userRepository.delete(user);
    }
}