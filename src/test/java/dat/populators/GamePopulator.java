package dat.populators;

import dat.daos.impl.GameDAO;
import dat.dtos.GameDTO;
import dat.entities.Game;

import java.time.LocalDate;
import java.util.List;

public class GamePopulator {
    public static List<Game> populateGames(GameDAO gameDAO) {
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

        GameDTO dto1 = gameDAO.create(new GameDTO(g1));
        GameDTO dto2 = gameDAO.create(new GameDTO(g2));
        GameDTO dto3 = gameDAO.create(new GameDTO(g3));
        GameDTO dto4 = gameDAO.create(new GameDTO(g4));

        g1.setId(dto1.getId());
        g2.setId(dto2.getId());
        g3.setId(dto3.getId());
        g4.setId(dto4.getId());

        return List.of(g1, g2, g3, g4);
    }
}
