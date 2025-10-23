package dat.Service;


import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class IgdbAccessTokenService {
    private final FetchTools fetchTools;
    private static Token token;

    public IgdbAccessTokenService(FetchTools fetchTools) { this.fetchTools = fetchTools; }

    public Token getToken(){
        if (token != null){
            //if the time it takes to expire is greater than the time from creation until now minus 1 hour in seconds,
            //then the token is valid, and we can keep using it
            if(token.getExpires_in() > token.getCreatedAt().until(LocalDateTime.now(), ChronoUnit.SECONDS) + 3600 ){
                return token;
            }
        }
        token = fetchToken();
        return token;
    }

    public Token fetchToken() {
        Token temp = fetchTools.postToApi(tokensURI(), Token.class);
        temp.setCreatedAt(LocalDateTime.now());
        return temp;
    }

    private static String tokensURI() {
        return "https://id.twitch.tv/oauth2/token?" +
                "client_id=" + System.getenv("client_id") +
                "&client_secret=" + System.getenv("client_secret") +
                "&grant_type=client_credentials" ;
    }

    @Getter
    @NoArgsConstructor
    @Builder
    @AllArgsConstructor
    @ToString
    public static class Token {
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
}
