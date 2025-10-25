package dat.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "first_release_date")
    private LocalDate firstReleaseDate;

    @Column(columnDefinition = "TEXT", length = 10000)
    private String summary;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "game_genres",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_name", referencedColumnName = "name")
    )
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();
}