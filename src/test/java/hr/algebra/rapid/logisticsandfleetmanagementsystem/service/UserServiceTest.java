package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.RegisterRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRoleRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;
    private UserInfo userInfo;
    private RegisterRequestDTO registerDTO;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, userRoleRepository, passwordEncoder);

        userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("testuser");
        userInfo.setEmail("test@example.com");

        registerDTO = new RegisterRequestDTO();
        registerDTO.setUsername("testuser");
        registerDTO.setEmail("test@example.com");
        registerDTO.setPassword("password123");
    }

    @Test
    void findAll_ShouldReturnList() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(userInfo));
        List<UserInfo> result = userService.findAll();
        assertThat(result).isNotEmpty();
    }

    @Test
    void findById_WhenExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userInfo));
        UserInfo result = userService.findById(1L);
        assertThat(result).isNotNull();
    }

    @Test
    void registerUser_ShouldRegisterUser() {
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(userInfo);
        UserInfo result = userService.registerUser(registerDTO);
        assertThat(result).isNotNull();
    }

}
