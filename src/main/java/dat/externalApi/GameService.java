package dat.externalApi;

import java.time.LocalDateTime;

public class GameService {
    private final FetchTools fetchTools;
    private static Token token;

    public GameService(FetchTools fetchTools) { this.fetchTools = fetchTools; }

    //private GameDTO fetchGame() {
    //    return fetchTools.getFromApi(genresUri(), GameDTO.class);
    //}

    private static String genresUri() {
        return "https://id.twitch.tv/oauth2/token?" +
                "client_id=" + System.getenv("client_id") +
                "&client_secret=" + System.getenv("client_secret") +
                "&grant_type=client_credentials" ;
    }
}
