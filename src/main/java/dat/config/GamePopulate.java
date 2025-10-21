package dat.config;

import dat.entities.Game;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;

public class GamePopulate {

    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Game g1 = Game.builder()
                    .name("The Witcher 3")
                    .firstReleaseDate(LocalDate.of(2015, 5, 19))
                    .summary("Open world RPG by CD Projekt Red")
                    .build();

            Game g2 = Game.builder()
                    .name("Cyberpunk 2077")
                    .firstReleaseDate(LocalDate.of(2020, 12, 10))
                    .summary("Sci-fi RPG set in Night City")
                    .build();

            Game g3 = Game.builder()
                    .name("The Legend of Zelda: Breath of the Wild")
                    .firstReleaseDate(LocalDate.of(2017, 3, 3))
                    .summary("Nintendo open world adventure game")
                    .build();

            Game g4 = Game.builder()
                    .name("Elden Ring")
                    .firstReleaseDate(LocalDate.of(2022, 2, 25))
                    .summary("Souls-like open world game by FromSoftware")
                    .build();

            em.persist(g1);
            em.persist(g2);
            em.persist(g3);
            em.persist(g4);

            em.getTransaction().commit();
            System.out.println("Testdata inserted successfully.");
        }
    }
}