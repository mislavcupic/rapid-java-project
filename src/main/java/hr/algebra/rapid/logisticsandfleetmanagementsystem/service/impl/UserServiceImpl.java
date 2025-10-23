package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.RegisterRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRoleRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository, 
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
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
        
        // 3. Dohvati ROLE_DRIVER kao default ulogu (možeš promijeniti u ROLE_USER ili bilo što drugo)
        UserRole driverRole = userRoleRepository.findByName("ROLE_DRIVER")
                .orElseThrow(() -> new ResourceNotFoundException("Uloga", "ime", "ROLE_DRIVER"));
        
        // 4. Kreiraj novog korisnika
        UserInfo newUser = new UserInfo();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword())); // Enkriptiraj lozinku
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setLastName(registerRequest.getLastName());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setIsEnabled(true); // Automatski aktiviraj korisnika
        newUser.setRoles(Collections.singletonList(driverRole)); // Postavi default ulogu
        
        // 5. Spremi u bazu
        return userRepository.save(newUser);
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
}
