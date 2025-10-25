package dat.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "genres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "name")
public class Genre {

    @Id
    @Column(name = "name", length = 120)
    private String name;

    @ManyToMany(mappedBy = "genres")
    private Set<Game> games = new LinkedHashSet<>();
}