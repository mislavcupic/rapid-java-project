// src/main/java/hr/algebra/rapid/logisticsandfleetmanagementsystem/service/impl/UserServiceImpl.java

package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.ApplicationUser;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ⭐ Ispravljen povratni tip na List<UserInfo>
    @Override
    public List<UserInfo> findAll() {
        return userRepository.findAll();
    }

    // ✅ Implementacija za findDrivers s ispravnim tipom
    @Override
    public List<UserInfo> findDrivers() {
        return userRepository.findByRoles_Name("ROLE_DRIVER");
    }

    // Dodana implementacija za findById
    @Override
    public UserInfo findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik", "ID", id));
    }
}