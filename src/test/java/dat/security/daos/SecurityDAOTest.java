package dat.security.daos;
import dat.config.HibernateConfig;
import dat.security.dtos.UserDTO;
import dat.security.entities.User;
import dat.security.exceptions.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;

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
            em.createQuery("DELETE FROM User").executeUpdate();
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
        dao.createUser(username, password);

        // Act
        UserDTO result = dao.getVerifiedUser(username, password);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertTrue(result.getRoles().contains("user"));
    }

    @Test
    void createUser() {
        // Arrange
        String username = "newuser";
        String password = "password123";

        // Act
        User result = dao.createUser(username, password);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(1, result.getRoles().size());
    }

    @Test
    void addRole() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        User createdUser = dao.createUser(username, password);

        // Act
        User result = dao.addRole(createdUser, "admin");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getRoles().size());
    }

    @Test
    void getUserByUsername() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        dao.createUser(username, password);

        // Act
        User result = dao.getUserByUsername(username);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertFalse(result.getRoles().isEmpty());
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
        assertEquals(username, result.getUsername());
        assertFalse(result.getRoles().isEmpty());
    }

    @Test
    void editUser() {
        // Arrange
        String username = "testuser";
        String oldPassword = "oldpassword";
        String newPassword = "newpassword";
        User createdUser = dao.createUser(username, oldPassword);

        // Create a new User object with the same ID but new password
        User userToUpdate = new User(username, newPassword);
        userToUpdate.setId(createdUser.getId());

        // Act
        User result = dao.editUser(userToUpdate);

        // Assert
        assertNotNull(result);
        assertTrue(result.verifyPassword(newPassword));
        assertFalse(result.verifyPassword(oldPassword));
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