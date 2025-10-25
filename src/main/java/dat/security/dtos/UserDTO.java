package dat.security.dtos;

import dat.entities.Game;
import dat.security.entities.User;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode

public class UserDTO {
    private Integer id;
    private String username;
    private String password;
    Set<String> roles = new HashSet<>();
    Set<Game> games = new HashSet<>();

    public UserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public UserDTO(Integer id, String username, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.roles = roles;
    }

    public UserDTO(String username, Set<String> roles) {
        this.username = username;
        this.roles = roles;
    }

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();

        if (user.getRoles() != null) {
            user.getRoles().forEach(role -> roles.add(role.toString()));
        }

        if (user.getGames() != null) {
            games.addAll(user.getGames());
        }
    }
}