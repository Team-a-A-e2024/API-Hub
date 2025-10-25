package dat.Util;

import dat.dtos.GameDTO;
import dat.dtos.IgdbGame;
import dat.utils.GameMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class GameMapperTest {

    @Test
    void mapIgdbGameToGame() {
        IgdbGame.Genre[] igdbGenres = new IgdbGame.Genre[]{
                IgdbGame.Genre.builder().name("RPG").build(),
                IgdbGame.Genre.builder().name("Action").build()
        };
        IgdbGame igdb = IgdbGame.builder()
                .name("Elden Ring")
                .summary("Souls-like")
                .firstReleaseDate(Instant.parse("2022-02-25T00:00:00Z").getEpochSecond())
                .genres(igdbGenres)
                .build();

        GameDTO dto = GameMapper.mapIgdbToDTO(igdb);
        assertEquals("Elden Ring", dto.getName());
        assertEquals(LocalDate.of(2022, 2, 25),
                dto.getFirstReleaseDate());
        assertNotNull(dto.getGenres());
        assertEquals(2, dto.getGenres().size());
        assertEquals("RPG", dto.getGenres().get(0).getName());
        assertEquals("Action", dto.getGenres().get(1).getName());
    }
}
