package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleTest {

    private UserRole userRole;

    @BeforeEach
    void setUp() {
        userRole = new UserRole();
    }

    @Test
    void setAndGetId_ShouldWork() {
        userRole.setId(1L);
        assertThat(userRole.getId()).isEqualTo(1L);
    }

    @Test
    void setAndGetName_ShouldWork() {
        userRole.setName("ROLE_ADMIN");
        assertThat(userRole.getName()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void equals_ShouldCompareCorrectly() {
        UserRole role1 = new UserRole();
        role1.setId(1L);

        UserRole role2 = new UserRole();
        role2.setId(1L);

        assertThat(role1).isEqualTo(role2);
    }
}
