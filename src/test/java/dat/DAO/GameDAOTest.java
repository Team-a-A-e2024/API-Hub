package dat.DAO;

import dat.config.HibernateConfig;
import dat.daos.impl.GameDAO;
import dat.dtos.GameDTO;
import dat.dtos.GenreDTO;
import dat.entities.Game;
import dat.entities.Genre;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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
            em.createNativeQuery("TRUNCATE TABLE game_genres, games, genres RESTART IDENTITY CASCADE;").executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    void createGame() {
        GameDTO dto = GameDTO.builder()
                .name("Half-Life")
                .firstReleaseDate(LocalDate.of(1998, 11, 19))
                .summary("Classic FPS")
                .build();
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
            em.persist(Game.builder()
                    .name("A")
                    .firstReleaseDate(LocalDate.of(2000, 1, 1))
                    .summary("Test")
                    .build());
            em.persist(Game.builder()
                    .name("B")
                    .firstReleaseDate(LocalDate.of(2001, 1, 1))
                    .summary("Test")
                    .build());
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
        GameDTO updatedData = GameDTO.builder()
                .name("New Name")
                .firstReleaseDate(LocalDate.of(1991, 1, 1))
                .summary("Updated Summary")
                .build();
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
            em.persist(Game.builder()
                    .name("The Witcher 3")
                    .firstReleaseDate(LocalDate.of(2015, 5, 19))
                    .summary("RPG")
                    .build());
            em.persist(Game.builder()
                    .name("The Witcher 2 Assassins of Kings Edition")
                    .firstReleaseDate(LocalDate.of(2012, 4, 17))
                    .summary("RPG")
                    .build());
            em.persist(Game.builder()
                    .name("Mario")
                    .firstReleaseDate(LocalDate.of(1985, 9, 13))
                    .summary("Minigame")
                    .build());
            em.getTransaction().commit();
        }
        List<GameDTO> results = dao.searchByName("witch");
        assertEquals(2, results.size());
    }

    @Test
    void addGenre() {
        int gameId;
        String genreName;
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Genre rpg = Genre.builder().name("RPG").build();
            em.persist(rpg);
            Game game = Game.builder()
                    .name("Elden Ring")
                    .firstReleaseDate(LocalDate.of(2022, 2, 25))
                    .summary("Souls-like")
                    .build();
            em.persist(game);
            em.getTransaction().commit();
            genreName = rpg.getName();
            gameId = game.getId();
        }
        GameDTO after = dao.addGenre(gameId, genreName);
        assertNotNull(after);
        assertEquals(gameId, after.getId());
        try (EntityManager em = emf.createEntityManager()) {
            Number count = (Number) em.createNativeQuery(
                            "SELECT COUNT(*) FROM game_genres WHERE game_id = ?1 AND genre_name = ?2")
                    .setParameter(1, gameId)
                    .setParameter(2, genreName)
                    .getSingleResult();
            assertEquals(1, count.intValue());
            @SuppressWarnings("unchecked")
            List<String> names = em.createNativeQuery(
                            "SELECT genre_name FROM game_genres WHERE game_id = ?1")
                    .setParameter(1, gameId)
                    .getResultList();
            assertEquals(Set.of(genreName), Set.copyOf(names));
        }
    }

    @Test
    void create_savesGenres() {
        GameDTO dto = GameDTO.builder()
                .name("Baldur's Gate 3")
                .firstReleaseDate(LocalDate.of(2023, 8, 3))
                .summary("CRPG")
                .genres(List.of(new GenreDTO("RPG"), new GenreDTO("Fantasy")))
                .build();
        GameDTO saved = dao.create(dto);
        assertNotNull(saved.getId());
        try (EntityManager em = emf.createEntityManager()) {
            @SuppressWarnings("unchecked")
            List<String> actual = em.createNativeQuery(
                            "SELECT genre_name FROM game_genres WHERE game_id = ?1")
                    .setParameter(1, saved.getId())
                    .getResultList();
            assertEquals(Set.of("RPG", "Fantasy"), Set.copyOf(actual));
        }
    }


}