package dat.utils;

import dat.dtos.GameDTO;
import dat.dtos.GenreDTO;
import dat.dtos.IgdbGame;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

public class GameMapper {
    public static GameDTO MapIgdbGameToGame(IgdbGame igdbGame) {
        LocalDate release = null;
        if (igdbGame.getFirstReleaseDate() != null) {
            release = Instant.ofEpochSecond(igdbGame.getFirstReleaseDate())
                    .atZone(ZoneId.systemDefault()).toLocalDate();
        }
        List<GenreDTO> genres = null;
        if (igdbGame.getGenres() != null) {
            genres = Arrays.stream(igdbGame.getGenres())
                    .filter(g -> g != null && g.getName() != null && !g.getName().isBlank())
                    .map(g -> GenreDTO.builder()
                            .id(null)
                            .name(g.getName())
                            .build())
                    .toList();
        }
        return GameDTO.builder()
                .name(igdbGame.getName())
                .firstReleaseDate(release)
                .summary(igdbGame.getSummary())
                .genres(genres)
                .build();
    }
}