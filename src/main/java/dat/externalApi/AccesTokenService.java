package dat.externalApi;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class AccesTokenService {
    private final FetchTools fetchTools;
    private static Token token;

    public AccesTokenService(FetchTools fetchTools) { this.fetchTools = fetchTools; }

    public Token getToken(){
        if (token != null){
            //if the time it takes to expire is greater than the time from creation until now minus 1 hour in seconds,
            //then the token is valid and we can keep using it
            if(token.getExpires_in() > token.getCreatedAt().until(LocalDateTime.now(), ChronoUnit.SECONDS) - 3600 ){
                return token;
            }
        }
        token = fetchToken();
        return token;
    }

    private Token fetchToken() {
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
}
