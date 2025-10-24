package dat.security.daos;
import dat.config.HibernateConfig;
import dat.security.dtos.UserDTO;
import dat.security.entities.Role;
import dat.security.entities.User;
import dat.security.exceptions.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("IntegrationTest")
class SecurityDAOTest {

    private EntityManagerFactory emf;
    private SecurityDAO dao;

    @BeforeAll
    void setupOnce() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        dao = new SecurityDAO(emf);
    }

    @BeforeEach
    void setup() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("TRUNCATE TABLE user_roles RESTART IDENTITY CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE roles RESTART IDENTITY CASCADE").executeUpdate();
            em.getTransaction().commit();
        }
    }

    @AfterAll
    void teardown() {
        if (emf != null && emf.isOpen()) emf.close();
    }

    @Test
    void getVerifiedUser() throws ValidationException {
        // Arrange
        String username = "testuser";
        String password = "password123";

        // Creating user as reference
        User createdUser = dao.createUser(username, password);

        // Act
        UserDTO result = dao.getVerifiedUser(username, password);

        // Assert
        assertNotNull(result);
        assertEquals(createdUser.getUsername(), result.getUsername());
        assertTrue(result.getRoles().contains("user"));
    }


    @Test
    void createUser() {
        // Arrange
        String username = "newuser";
        String password = "password123";

        // Expected user
        User expectedUser = new User();
        expectedUser.setUsername(username);
        expectedUser.addRole(new Role("user"));

        // Act
        User result = dao.createUser(username, password);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUser.getUsername(), result.getUsername());
        Set<String> expectedRoles = expectedUser.getRoles()
                .stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        Set<String> actualRoles = result.getRoles()
                .stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        assertEquals(expectedRoles, actualRoles);
    }

    @Test
    void addRole() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        User createdUser = dao.createUser(username, password);

        // Forventede roller efter opdatering
        Set<String> expectedRoles = Set.of("user", "admin");

        // Act
        User result = dao.addRole(createdUser, "admin");

        // Assert
        assertNotNull(result);

        // Actual roles
        Set<String> actualRoles = result.getRoles()
                .stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        assertEquals(expectedRoles, actualRoles);
    }

    @Test
    void getUserByUsername() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        dao.createUser(username, password);

        // Comparing roles
        User expectedUser = new User();
        expectedUser.setUsername(username);
        expectedUser.addRole(new Role("user"));

        // Act
        User result = dao.getUserByUsername(username);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUser.getUsername(), result.getUsername());

        // Comparing roles
        Set<String> expectedRoles = expectedUser.getRoles()
                .stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        Set<String> actualRoles = result.getRoles()
                .stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        assertEquals(expectedRoles, actualRoles);
    }

    @Test
    void getUserById() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        User createdUser = dao.createUser(username, password);

        // Act
        User result = dao.getUserById(createdUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(createdUser.getUsername(), result.getUsername());
    }

    @Test
    void editUser() {
        // Arrange
        User createdUser = dao.createUser("testuser", "oldPassword123");

        // Act
        createdUser.setPassword("newPassword123");
        User updatedUser = dao.editUser(createdUser);

        // Assert
        assertTrue(updatedUser.verifyPassword("newPassword123"));
        assertFalse(updatedUser.verifyPassword("oldPassword123"));
    }

    @Test
    void deleteUser() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        User createdUser = dao.createUser(username, password);
        Integer userId = createdUser.getId();

        // Act
        dao.deleteUser(userId);

        // Assert
        assertThrows(EntityNotFoundException.class, () ->
                dao.getUserById(userId)
        );
    }
}