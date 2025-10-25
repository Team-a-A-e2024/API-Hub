package dat.security.daos;
import dat.config.HibernateConfig;
import dat.daos.impl.GameDAO;
import dat.dtos.GameDTO;
import dat.entities.Game;
import dat.populators.GamePopulator;
import dat.populators.SecurityPopulator;
import dat.security.dtos.UserDTO;
import dat.security.entities.Role;
import dat.security.entities.User;
import dat.security.exceptions.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("IntegrationTest")
class SecurityDAOTest {

    private EntityManagerFactory emf;
    private SecurityDAO securityDAO;
    private GameDAO gameDAO;
    private User u1, u2, u3;
    private Game g1, g2, g3, g4;

    @BeforeAll
    void setupOnce() {
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        securityDAO = new SecurityDAO(emf);
        gameDAO = new GameDAO(emf);
    }

    @BeforeEach
    void setup() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("TRUNCATE TABLE " +
                    "users, " +
                    "roles, " +
                    "user_roles, " +
                    "games, " +
                    "favorites " +
                    "RESTART IDENTITY CASCADE").executeUpdate();
            em.getTransaction().commit();
        }

        List<User> users = SecurityPopulator.populateUsers(securityDAO);
        List<Game> games = GamePopulator.populateGames(gameDAO);
        u1 = users.get(0);
        u2 = users.get(1);
        u3 = users.get(2);
        g1 = games.get(0);
        g2 = games.get(1);
        g3 = games.get(2);
        g4 = games.get(3);
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
        User createdUser = securityDAO.createUser(username, password);
        Set<String> roles = createdUser.getRoles().stream()
                .map(role -> role.getRoleName())
                .collect(Collectors.toSet());
        UserDTO expectedUser = new UserDTO(createdUser.getId(), createdUser.getUsername(), roles);


        // Act
        UserDTO result = securityDAO.getVerifiedUser(username, password);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUser, result);
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
        User result = securityDAO.createUser(username, password);

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
        User createdUser = securityDAO.createUser(username, password);

        // Forventede roller efter opdatering
        Set<String> expectedRoles = Set.of("user", "admin");

        // Act
        User result = securityDAO.addRole(createdUser, "admin");

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
        securityDAO.createUser(username, password);

        // Comparing roles
        User expectedUser = new User();
        expectedUser.setUsername(username);
        expectedUser.addRole(new Role("user"));

        // Act
        User result = securityDAO.getUserByUsername(username);

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
        User createdUser = securityDAO.createUser(username, password);

        // Act
        User result = securityDAO.getUserById(createdUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(createdUser.getUsername(), result.getUsername());
    }

    @Test
    void editUser() {
        // Arrange
        User createdUser = securityDAO.createUser("testuser", "oldPassword123");

        // Act
        createdUser.setPassword("newPassword123");
        User updatedUser = securityDAO.editUser(createdUser);

        // Assert
        assertTrue(updatedUser.verifyPassword("newPassword123"));
        assertFalse(updatedUser.verifyPassword("oldPassword123"));
    }

    @Test
    void editUser_doesNotChangeUsername() {
        // Arrange
        User createdUser = securityDAO.createUser("testuser", "password123");

        // Act
        createdUser.setUsername("newusername");
        User updatedUser = securityDAO.editUser(createdUser);

        // Assert
        assertEquals("testuser", updatedUser.getUsername());
        assertTrue(updatedUser.verifyPassword("password123"));
    }

    @Test
    void deleteUser() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        User createdUser = securityDAO.createUser(username, password);
        Integer userId = createdUser.getId();

        // Act
        securityDAO.deleteUser(userId);

        // Assert
        assertThrows(EntityNotFoundException.class, () ->
                securityDAO.getUserById(userId)
        );
    }

    @Test
    void addFavoriteGame() {
        // Arrange
        User user = u1;
        Game game = g1;
        user.addGame(game);
        UserDTO expected = new UserDTO(user);

        // Act
        UserDTO actual = securityDAO.addFavoriteGame(user.getId(), game.getId());

        // Assert
        assertThat(actual, equalTo(expected));
        assertThat(actual.getGames().size(), equalTo(1));
    }

    @Test
    void addFavoriteGameInvalidUser() {
        // Arrange
        User user = new User();
        user.setId(0);
        Game game = g1;
        user.addGame(game);

        // Act
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> securityDAO.addFavoriteGame(user.getId(), game.getId())
        );

        // Assert
        assertThat(exception.getMessage(), containsString("No user found with id: " + user.getId()));
    }

    @Test
    void addFavoriteGameInvalidGame() {
        // Arrange
        User user = u1;
        Game game = new Game();
        game.setId(0);
        user.addGame(game);

        // Act
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> securityDAO.addFavoriteGame(user.getId(), game.getId())
        );

        // Assert
        assertThat(exception.getMessage(), containsString("No game found with id: " + game.getId()));
    }

    @Test
    void removeFavoriteGame() {
        // Arrange
        User user = u1;
        Game game = g1;
        UserDTO expected = new UserDTO(user);
        user.addGame(game);
        securityDAO.addFavoriteGame(user.getId(), game.getId());

        // Act
        UserDTO actual = securityDAO.removeFavoriteGame(user.getId(), game.getId());

        // Assert
        assertThat(actual, equalTo(expected));
        assertThat(actual.getGames().size(), equalTo(0));
    }

    @Test
    void removeFavoriteGameInvalidUser() {
        // Arrange
        User user = new User();
        user.setId(0);
        Game game = g1;

        // Act
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> securityDAO.removeFavoriteGame(user.getId(), game.getId())
        );

        // Assert
        assertThat(exception.getMessage(), containsString("No user found with id: " + user.getId()));
    }

    @Test
    void removeFavoriteGameInvalidGame() {
        // Arrange
        User user = u1;
        Game game = new Game();
        game.setId(0);

        // Act
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> securityDAO.removeFavoriteGame(user.getId(), game.getId())
        );

        // Assert
        assertThat(exception.getMessage(), containsString("No game found with id: " + game.getId()));
    }

    @Test
    void validatePrimaryKey() {
        // Arrange
        User user = u1;

        // Act
        boolean actual = securityDAO.validatePrimaryKey(user.getId());

        // Assert
        assertThat(actual, is(true));
    }

    @Test
    void validatePrimaryKeyInvalidId() {
        // Arrange
        User user = u1;
        user.setId(0);

        // Act
        boolean actual = securityDAO.validatePrimaryKey(user.getId());

        // Assert
        assertThat(actual, is(false));
    }
}