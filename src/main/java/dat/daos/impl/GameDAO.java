package dat.daos.impl;

import dat.daos.IDAO;
import dat.dtos.GameDTO;
import dat.entities.Game;
import dat.entities.Genre;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class GameDAO implements IDAO<GameDTO, Integer> {

    private final EntityManagerFactory emf;

    public GameDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public GameDTO read(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            Game g = em.find(Game.class, id);
            return g == null ? null : new GameDTO(g);
        }
    }

    @Override
    public List<GameDTO> readAll() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Game> q = em.createQuery(
                    "SELECT g FROM Game g ORDER BY g.name ASC", Game.class
            );
            return q.getResultList().stream().map(GameDTO::new).toList();
        }
    }

    @Override
    public GameDTO create(GameDTO dto) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                Game g = Game.builder()
                        .name(dto.getName())
                        .firstReleaseDate(dto.getFirstReleaseDate())
                        .summary(dto.getSummary())
                        .build();
                if (dto.getGenres() != null) {
                    for (var gd : dto.getGenres()) {
                        if (gd.getName() == null || gd.getName().isBlank()) continue;
                        Genre genre = em.find(Genre.class, gd.getName());
                        if (genre == null) {
                            genre = Genre.builder().name(gd.getName()).build();
                            em.persist(genre);
                        }
                        g.getGenres().add(genre);
                    }
                }
                em.persist(g);
                em.getTransaction().commit();
                return new GameDTO(g);
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }
        }
    }

    @Override
    public GameDTO update(Integer id, GameDTO dto) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                Game existing = em.find(Game.class, id);
                if (existing == null) {
                    em.getTransaction().rollback();
                    return null;
                }
                existing.setName(dto.getName());
                existing.setFirstReleaseDate(dto.getFirstReleaseDate());
                existing.setSummary(dto.getSummary());
                em.getTransaction().commit();
                return new GameDTO(existing);
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }
        }
    }

    @Override
    public void delete(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                Game g = em.find(Game.class, id);
                if (g != null) em.remove(g);
                em.getTransaction().commit();
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }
        }
    }

    @Override
    public boolean validatePrimaryKey(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Game.class, id) != null;
        }
    }

    public List<GameDTO> searchByName(String q) {
        try (EntityManager em = emf.createEntityManager()) {
            var query = em.createQuery(
                    "SELECT g FROM Game g WHERE LOWER(g.name) LIKE :q ORDER BY g.name ASC",
                    Game.class
            );
            query.setParameter("q", "%" + q.toLowerCase() + "%");
            return query.getResultList().stream().map(GameDTO::new).toList();
        }
    }

    public GameDTO addGenre(int gameId, String genreName) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                Game g = em.find(Game.class, gameId);
                if (g == null) {
                    em.getTransaction().rollback();
                    return null;
                }
                Genre genre = em.find(Genre.class, genreName);
                if (genre == null) {
                    genre = Genre.builder().name(genreName).build();
                    em.persist(genre);
                }
                g.getGenres().add(genre);
                em.getTransaction().commit();
                return new GameDTO(g);
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }
        }
    }
}