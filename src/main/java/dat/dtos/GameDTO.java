package dat.dtos;

import dat.entities.Game;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class GameDTO {
    private Integer id;
    private String name;
    private LocalDate firstReleaseDate;
    private String summary;
    private List<GenreDTO> genres;

    public GameDTO(Game g) {
        this.id = g.getId();
        this.name = g.getName();
        this.firstReleaseDate = g.getFirstReleaseDate();
        this.summary = g.getSummary();
        this.genres = (g.getGenres() == null)
                ? List.of()
                : g.getGenres().stream().map(GenreDTO::new).collect(Collectors.toList());
    }
}