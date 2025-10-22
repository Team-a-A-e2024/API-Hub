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

    @BeforeEach
    void cleanDatabase() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("TRUNCATE TABLE games RESTART IDENTITY CASCADE").executeUpdate();
        em.getTransaction().commit();
        em.close();
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
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Game g = Game.builder()
                .name("Portal")
                .firstReleaseDate(LocalDate.of(2007, 10, 10))
                .summary("Puzzle game")
                .build();
        em.persist(g);
        em.getTransaction().commit();
        em.close();
        GameDTO found = dao.read(g.getId());
        assertNotNull(found);
        assertEquals("Portal", found.getName());
    }

    @Test
    void readAllGames() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(new Game(null, "A", LocalDate.now(), "Test"));
        em.persist(new Game(null, "B", LocalDate.now(), "Test"));
        em.getTransaction().commit();
        em.close();
        List<GameDTO> games = dao.readAll();
        assertEquals(2, games.size());
    }

    @Test
    void updateGame() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Game g = Game.builder()
                .name("Old Name")
                .firstReleaseDate(LocalDate.now())
                .summary("Old Summary")
                .build();
        em.persist(g);
        em.getTransaction().commit();
        em.close();
        GameDTO updatedData = new GameDTO(null, "New Name", LocalDate.now(), "Updated Summary");
        GameDTO updated = dao.update(g.getId(), updatedData);
        assertEquals("New Name", updated.getName());
    }

    @Test
    void deleteGame() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Game g = Game.builder()
                .name("To Delete")
                .firstReleaseDate(LocalDate.now())
                .summary("Delete me")
                .build();
        em.persist(g);
        em.getTransaction().commit();
        em.close();
        dao.delete(g.getId());
        assertNull(dao.read(g.getId()));
    }

    @Test
    void validatePrimaryKey() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Game g = Game.builder()
                .name("Exists")
                .firstReleaseDate(LocalDate.now())
                .summary("test")
                .build();
        em.persist(g);
        em.getTransaction().commit();
        em.close();
        assertTrue(dao.validatePrimaryKey(g.getId()));
        assertFalse(dao.validatePrimaryKey(9999));
    }

    @Test
    void searchByName() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(new Game(null, "The Witcher 3", LocalDate.of(2015, 5, 19), "RPG"));
        em.persist(new Game(null, "The Witcher 2 Assassins of Kings Edition", LocalDate.of(2012, 4, 17), "RPG"));
        em.persist(new Game(null, "Mario", LocalDate.of(1985, 9, 13), "Minigame"));
        em.getTransaction().commit();
        em.close();
        List<GameDTO> results = dao.searchByName("witch");
        assertEquals(2, results.size());
    }
}