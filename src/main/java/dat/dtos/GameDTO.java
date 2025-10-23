package dat.dtos;

import dat.entities.Game;
import lombok.*;

import java.time.LocalDate;

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

    public GameDTO(Game g) {
        this.id = g.getId();
        this.name = g.getName();
        this.firstReleaseDate = g.getFirstReleaseDate();
        this.summary = g.getSummary();
    }
}
