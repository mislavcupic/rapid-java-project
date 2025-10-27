package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserInfoTest {

    private UserInfo userInfo;
    private UserRole role;

    @BeforeEach
    void setUp() {
        role = new UserRole();
        role.setId(1L);
        role.setName("ROLE_USER");
        userInfo = new UserInfo();
    }

    @Test
    void setAndGetId_ShouldWork() {
        userInfo.setId(1L);
        assertThat(userInfo.getId()).isEqualTo(1L);
    }

    @Test
    void setAndGetUsername_ShouldWork() {
        userInfo.setUsername("testuser");
        assertThat(userInfo.getUsername()).isEqualTo("testuser");
    }

    @Test
    void setAndGetPassword_ShouldWork() {
        userInfo.setPassword("password123");
        assertThat(userInfo.getPassword()).isEqualTo("password123");
    }

    @Test
    void setAndGetFirstName_ShouldWork() {
        userInfo.setFirstName("John");
        assertThat(userInfo.getFirstName()).isEqualTo("John");
    }

    @Test
    void setAndGetLastName_ShouldWork() {
        userInfo.setLastName("Doe");
        assertThat(userInfo.getLastName()).isEqualTo("Doe");
    }

    @Test
    void setAndGetEmail_ShouldWork() {
        userInfo.setEmail("john.doe@example.com");
        assertThat(userInfo.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void setAndGetIsEnabled_ShouldWork() {
        userInfo.setIsEnabled(true);
        assertThat(userInfo.getIsEnabled()).isTrue();
    }

    @Test
    void setAndGetRoles_ShouldWork() {
        List<UserRole> roles = Arrays.asList(role);
        userInfo.setRoles(roles);
        assertThat(userInfo.getRoles()).hasSize(1);
        assertThat(userInfo.getRoles().get(0).getName()).isEqualTo("ROLE_USER");
    }

    @Test
    void equals_ShouldCompareCorrectly() {
        UserInfo user1 = new UserInfo();
        user1.setId(1L);

        UserInfo user2 = new UserInfo();
        user2.setId(1L);

        assertThat(user1).isEqualTo(user2);
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyUser() {
        UserInfo emptyUser = new UserInfo();
        assertThat(emptyUser).isNotNull();
        assertThat(emptyUser.getId()).isNull();
    }
}
