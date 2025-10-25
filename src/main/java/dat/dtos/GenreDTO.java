package dat.dtos;

import dat.entities.Genre;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenreDTO {
    private String name;

    public GenreDTO(Genre genre) {
        this.name = genre.getName();
    }
}