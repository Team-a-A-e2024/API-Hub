package dat.dtos;

import dat.entities.Genre;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenreDTO {
    private Integer id;
    private String name;

    public GenreDTO(Genre genre) {
        this.id = genre.getId();
        this.name = genre.getName();
    }
}
