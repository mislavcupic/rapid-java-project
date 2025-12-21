package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DomainMappingTest {

    // --- TESTOVI ZA USERINFO (Entity) ---

    @Test
    @DisplayName("UserInfo - Testiranje svih gettera i settera")
    void testUserInfoGettersSetters() {
        UserInfo user = new UserInfo();
        user.setId(10L);
        user.setUsername("marko");
        user.setFirstName("Marko");
        user.setLastName("Markić");
        user.setEmail("marko@algebra.hr");
        user.setPassword("tajna");
        user.setIsEnabled(true);

        assertEquals(10L, user.getId());
        assertEquals("marko", user.getUsername());
        assertEquals("Marko", user.getFirstName());
        assertEquals("marko@algebra.hr", user.getEmail());
        assertEquals("tajna", user.getPassword());
        assertTrue(user.getIsEnabled());
    }

    @Test
    @DisplayName("UserInfo - Testiranje toString metode")
    void testUserInfoToString() {
        UserInfo user = new UserInfo();
        user.setUsername("test");
        String toString = user.toString();
        assertTrue(toString.contains("username=test"));
    }

    // --- TESTOVI ZA APPLICATIONUSER (Druga verzija entiteta/tablice) ---

    @Test
    @DisplayName("ApplicationUser - Testiranje Buildera")
    void testApplicationUserBuilder() {
        UserRole role = new UserRole();
        role.setName("ROLE_USER");

        ApplicationUser user = ApplicationUser.builder()
                .id(1L)
                .roles(List.of(role))
                .build();

        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("ROLE_USER", user.getRoles().get(0).getName());
    }

    @Test
    @DisplayName("ApplicationUser - Testiranje NoArgsConstructora")
    void testApplicationUserEmptyConstructor() {
        ApplicationUser user = new ApplicationUser();
        assertNull(user.getId());
        assertNull(user.getRoles());
    }

    // --- TESTOVI ZA USERROLE ---

    @Test
    @DisplayName("UserRole - Testiranje osnovnih polja")
    void testUserRoleBasic() {
        UserRole role = new UserRole();
        role.setId(1L);
        role.setName("ROLE_ADMIN");

        assertEquals(1L, role.getId());
        assertEquals("ROLE_ADMIN", role.getName());
    }

    @Test
    @DisplayName("UserRole - Jednakost objekata (Equals/HashCode)")
    void testUserRoleEquals() {
        UserRole r1 = new UserRole();
        r1.setId(1L);

        UserRole r2 = new UserRole();
        r2.setId(1L);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }
}