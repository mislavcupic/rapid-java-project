package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@DependsOn("entityManagerFactory")
@RequiredArgsConstructor // ✅ Lombok generiše konstruktor
public class UserDetailsServiceImpl implements UserDetailsService {

    // ✅ Constructor Injection umjesto Field Injection
    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = repository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("Unknown user: " + username);
        }

        List<UserRole> userRoleList = user.getRoles();

        // ✅ Moderan pristup - koristi Stream API
        String[] roles = userRoleList.stream()
                .map(UserRole::getName)
                .map(this::removeRolePrefix)
                .toArray(String[]::new);

        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(roles) // Prima samo "ADMIN", "DISPATCHER", "DRIVER"
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    /**
     * Uklanja "ROLE_" prefiks iz role name-a ako postoji
     * Npr: "ROLE_ADMIN" -> "ADMIN"
     */
    private String removeRolePrefix(String roleName) {
        return roleName.startsWith("ROLE_")
                ? roleName.substring(5)
                : roleName;
    }
}