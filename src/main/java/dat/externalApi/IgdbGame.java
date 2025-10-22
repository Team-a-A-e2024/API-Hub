package dat.externalApi;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IgdbGame {
    private Long id;
    @JsonAlias("first_release_date")
    private Long firstReleaseDate;
    private Genre[] genres;
    private String name;
    private String summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Genre {
        private int id;
        private String name;
    }
}
