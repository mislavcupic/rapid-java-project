package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.RegisterRequestDTO;

import java.util.List;

public interface UserService {

    List<UserInfo> findAll();
    UserInfo updateUserRoles(Long userId, List<String> roleNames);
    UserInfo findById(Long id); // Važna za dodjelu vozača vozilu
    void deleteUser(Long userId);
    /**
     * Registracija novog korisnika
     * @param registerRequest DTO s podacima za registraciju
     * @return Kreirani UserInfo entitet
     */
    UserInfo registerUser(RegisterRequestDTO registerRequest);
    
    /**
     * Provjera postoji li korisnik s danim username-om
     * @param username Korisničko ime
     * @return true ako postoji, false inače
     */
    boolean existsByUsername(String username);
    
    /**
     * Provjera postoji li korisnik s danim emailom
     * @param email Email adresa
     * @return true ako postoji, false inače
     */
    boolean existsByEmail(String email);
}
