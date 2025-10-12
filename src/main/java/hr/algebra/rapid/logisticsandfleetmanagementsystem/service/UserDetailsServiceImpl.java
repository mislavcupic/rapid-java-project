package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@DependsOn("entityManagerFactory")
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = this.repository.findByUsername(username);

        if(user == null) {
            throw new UsernameNotFoundException("Unknown user " + username);
        }

        List<UserRole> userRoleList = user.getRoles();

        String[] roles = new String[userRoleList.size()];

        for(int i = 0; i < userRoleList.size(); i++) {
            String roleName = userRoleList.get(i).getName();

            // 💡 KLJUČNA IZMJENA: Uklanjamo prefiks "ROLE_" prije prosljeđivanja metodi .roles()
            if (roleName.startsWith("ROLE_")) {
                roles[i] = roleName.substring(5); // Ukloni prvih 5 znakova ("ROLE_")
            } else {
                roles[i] = roleName;
            }
        }

        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(roles) // Sada prima samo "ADMIN", "MANAGER", "DRIVER"
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}