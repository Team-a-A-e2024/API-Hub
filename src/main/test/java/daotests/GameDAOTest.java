package daotests;

import dat.config.HibernateConfig;
import dat.daos.impl.GameDAO;
import dat.dtos.GameDTO;
import dat.entities.Game;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("IntegrationTest")
class GameDAOTest {

    private EntityManagerFactory emf;
    private GameDAO dao;

    @BeforeAll
    void setupOnce() {
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        dao = GameDAO.getInstance();
    }

    @AfterAll
    void tearDownOnce() {
        if (emf != null) emf.close();
        HibernateConfig.setTest(false);
    }

    @BeforeEach
    void cleanDatabase() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("TRUNCATE TABLE games RESTART IDENTITY CASCADE").executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    void createGame() {
        GameDTO dto = new GameDTO(null, "Half-Life", LocalDate.of(1998, 11, 19), "Classic FPS");
        GameDTO created = dao.create(dto);
        assertNotNull(created.getId());
        assertEquals("Half-Life", created.getName());
    }

    @Test
    void readGame() {
        Integer id;
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Game g = Game.builder()
                    .name("Portal")
                    .firstReleaseDate(LocalDate.of(2007, 10, 10))
                    .summary("Puzzle game")
                    .build();
            em.persist(g);
            id = g.getId();
            em.getTransaction().commit();
        }
        GameDTO found = dao.read(id);
        assertNotNull(found);
        assertEquals("Portal", found.getName());
    }

    @Test
    void readAllGames() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(new Game(null, "A", LocalDate.of(2000, 1, 1), "Test"));
            em.persist(new Game(null, "B", LocalDate.of(2001, 1, 1), "Test"));
            em.getTransaction().commit();
        }
        List<GameDTO> games = dao.readAll();
        assertEquals(2, games.size());
    }

    @Test
    void updateGame() {
        Integer id;
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Game g = Game.builder()
                    .name("Old Name")
                    .firstReleaseDate(LocalDate.of(1990, 1, 1))
                    .summary("Old Summary")
                    .build();
            em.persist(g);
            id = g.getId();
            em.getTransaction().commit();
        }
        GameDTO updatedData = new GameDTO(null, "New Name", LocalDate.of(1991, 1, 1), "Updated Summary");
        GameDTO updated = dao.update(id, updatedData);
        assertEquals("New Name", updated.getName());
    }

    @Test
    void deleteGame() {
        Integer id;
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Game g = Game.builder()
                    .name("To Delete")
                    .firstReleaseDate(LocalDate.of(1995, 5, 5))
                    .summary("Delete me")
                    .build();
            em.persist(g);
            id = g.getId();
            em.getTransaction().commit();
        }
        dao.delete(id);
        assertNull(dao.read(id));
    }

    @Test
    void validatePrimaryKey() {
        Integer id;
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Game g = Game.builder()
                    .name("Exists")
                    .firstReleaseDate(LocalDate.of(1988, 8, 8))
                    .summary("test")
                    .build();
            em.persist(g);
            id = g.getId();
            em.getTransaction().commit();
        }
        assertTrue(dao.validatePrimaryKey(id));
        assertFalse(dao.validatePrimaryKey(9999));
    }

    @Test
    void searchByName() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(new Game(null, "The Witcher 3", LocalDate.of(2015, 5, 19), "RPG"));
            em.persist(new Game(null, "The Witcher 2 Assassins of Kings Edition", LocalDate.of(2012, 4, 17), "RPG"));
            em.persist(new Game(null, "Mario", LocalDate.of(1985, 9, 13), "Minigame"));
            em.getTransaction().commit();
        }
        List<GameDTO> results = dao.searchByName("witch");
        assertEquals(2, results.size());
    }
}