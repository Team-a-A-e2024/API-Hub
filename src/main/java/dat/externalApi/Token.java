package dat.externalApi;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@ToString

public class Token {
    @JsonAlias("access_token")
    private String accessToken;
    @JsonAlias("expires_in")
    private Long expires_in;
    @JsonAlias("token_type")
    private String tokenType;

    @Setter
    @JsonIgnore
    private LocalDateTime createdAt;
}
