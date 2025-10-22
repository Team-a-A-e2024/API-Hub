package dat.security.dtos;

import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Getter
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
}