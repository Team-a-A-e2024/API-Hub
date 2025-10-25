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

    public static GameDTO mapIgdbToDTO(IgdbGame ig) {
        LocalDate release = ig.getFirstReleaseDate() == null ? null :
                Instant.ofEpochSecond(ig.getFirstReleaseDate())
                        .atZone(ZoneId.systemDefault()).toLocalDate();
        List<GenreDTO> genres = (ig.getGenres() == null) ? null
                : Arrays.stream(ig.getGenres())
                .filter(g -> g.getName() != null && !g.getName().isBlank())
                .map(g -> GenreDTO.builder().name(g.getName()).build())
                .toList();
        return GameDTO.builder()
                .name(ig.getName())
                .firstReleaseDate(release)
                .summary(ig.getSummary())
                .genres(genres)
                .build();
    }
}