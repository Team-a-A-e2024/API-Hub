package dat.security.dtos;

import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UserDTO {
    private Integer id;
    private String username;
    private String password;
    Set<String> roles = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO dto = (UserDTO) o;
        return Objects.equals(username, dto.username) && Objects.equals(roles, dto.roles);
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(username, roles);
    }
}