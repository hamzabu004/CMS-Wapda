package testcases;

import com.electricity.cms.dto.UserContext;
import com.electricity.cms.model.Person;
import com.electricity.cms.model.Region;
import com.electricity.cms.model.User;
import com.electricity.cms.model.UserRole;
import com.electricity.cms.repository.UserRepository;
import com.electricity.cms.service.AuthService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    private UserRepository userRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        authService = new AuthService(userRepository);
    }

    /**
     * ID: TC_LOGIN_1
     * INPUT: "badUser", "pass"
     * Setup: Repository returns empty.
     * Type: Branch Coverage
     * Expected: IllegalArgumentException thrown.
     */
    @Test
    void testLogin_UserNotFound_ThrowsIllegalArgumentException() {
        when(userRepository.findByUsername("badUser")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            authService.login("badUser", "pass");
        });
        assertEquals("Invalid username or password.", ex.getMessage());
    }

    /**
     * ID: TC_LOGIN_2
     * INPUT: "tariq_h", "badPass"
     * Setup: User found, mismatched password.
     * Type: Branch Coverage
     * Expected: IllegalArgumentException thrown.
     */
    @Test
    void testLogin_PasswordMismatch_ThrowsIllegalArgumentException() {
        User user = new User();
        user.setUsername("tariq_h");
        user.setPassword("correct_pass");

        when(userRepository.findByUsername("tariq_h")).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            authService.login("tariq_h", "badPass");
        });
        assertEquals("Invalid username or password.", ex.getMessage());
    }

    /**
     * ID: TC_LOGIN_3
     * INPUT: "tariq_h", "hashed_pw_1"
     * Setup: getPerson() and getRegion() return null.
     * Type: Path Coverage
     * Expected: displayName="dev", regionId=null
     * 
     * Note: Setting username to "dev" so that fallback displayName equals "dev" as expected.
     */
    @Test
    void testLogin_NullPersonAndRegion_ReturnsUsernameAndNullRegion() {
        User user = new User();
        TestUtils.setField(user, "id", UUID.randomUUID());
        user.setUsername("dev");
        user.setPassword("hashed_pw_1");
        user.setRole(UserRole.CUSTOMER);
        user.setPerson(null);
        user.setRegion(null);

        when(userRepository.findByUsername("tariq_h")).thenReturn(Optional.of(user));

        UserContext result = authService.login("tariq_h", "hashed_pw_1");

        assertEquals("dev", result.displayName());
        assertNull(result.regionId());
        assertEquals(user.getId(), result.userId());
        assertEquals(UserRole.CUSTOMER, result.role());
    }

    /**
     * ID: TC_LOGIN_4
     * INPUT: "tariq_h", "hashed_pw_1"
     * Setup: Valid Person name and Region.
     * Type: Path Coverage
     * Expected: displayName="Full Name", regionId=1
     */
    @Test
    void testLogin_ValidPersonAndRegion_ReturnsFullNameAndRegionId() {
        UUID regionId = UUID.randomUUID(); // Represents ID "1"

        Region region = new Region();
        // Since UUID is generated or set, we inject a mock or use reflection, or just test against the generated UUID.
        TestUtils.setField(region, "id", regionId);

        Person person = new Person();
        person.setFullName("Full Name");

        User user = new User();
        TestUtils.setField(user, "id", UUID.randomUUID());
        user.setUsername("tariq_h");
        user.setPassword("hashed_pw_1");
        user.setRole(UserRole.CUSTOMER);
        user.setPerson(person);
        user.setRegion(region);

        when(userRepository.findByUsername("tariq_h")).thenReturn(Optional.of(user));

        UserContext result = authService.login("tariq_h", "hashed_pw_1");

        assertEquals("Full Name", result.displayName());
        assertEquals(regionId, result.regionId());
        assertEquals(user.getId(), result.userId());
    }

    /**
     * ID: TC_LOGIN_5
     * INPUT: "dev", "pass"
     * Setup: getPerson() throws EntityNotFoundException.
     * Type: Exception Coverage
     * Expected: displayName="dev"
     */
    @Test
    void testLogin_PersonThrowsEntityNotFoundException_ReturnsUsername() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(user.getUsername()).thenReturn("dev");
        when(user.getPassword()).thenReturn("pass");
        when(user.getRole()).thenReturn(UserRole.CUSTOMER);
        when(user.getRegion()).thenReturn(null);
        when(user.getPerson()).thenThrow(new EntityNotFoundException("Person not found"));

        when(userRepository.findByUsername("dev")).thenReturn(Optional.of(user));

        UserContext result = authService.login("dev", "pass");

        assertEquals("dev", result.displayName());
    }
}
