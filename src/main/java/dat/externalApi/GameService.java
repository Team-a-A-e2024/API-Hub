package dat.externalApi;

import dat.utils.TimeMapper;

import java.net.http.HttpRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class GameService {
    private final FetchTools fetchTools;
    private static Token token;

    public GameService(FetchTools fetchTools) { this.fetchTools = fetchTools; }

    public List<Game> getNewlyAddedGames(){
        List<Game> games = new ArrayList<>();
        List<Callable<igdbGame[]>> tasks = new ArrayList<>();

        int amount = fetchNewGamesCount().getCount();

        for(int i = 0; i > amount/500; i++){

        }

        return games;
    }

    //page starts from index 0
    private igdbGame[] fetchGames(int page) {
        AccesTokenService accesTokenService = new AccesTokenService(fetchTools);

        return fetchTools.postToApi(gamesUri(), igdbGame[].class,
                HttpRequest.BodyPublishers.ofString(
                        //page number + todays date as a unix timestamp minus 31 days in seconds
                        gamesBody(page, TimeMapper.unixOf(LocalDateTime.now()) - 2678400)),
                new String[] {"Accept", "application/json", "Client-ID",System.getenv("client_id"),"Authorization","bearer " + accesTokenService.getToken().getAccessToken()}
                );
    }

    //page starts from index 0
    private igdbGame[] fetchNewGames(int page) {
        AccesTokenService accesTokenService = new AccesTokenService(fetchTools);

        return fetchTools.postToApi(gamesUri(), igdbGame[].class,
                HttpRequest.BodyPublishers.ofString(
                        //page number + todays date as a unix timestamp minus 31 days in seconds
                        gamesBody(page, (TimeMapper.unixOf(LocalDateTime.now()) - 2678400)) +
                                //todays date as a unix timestamp minus 1 day in seconds
                                "where created_at > " + (TimeMapper.unixOf(LocalDateTime.now()) - 86400) + ";"),
                new String[] {"Accept", "application/json", "Client-ID",System.getenv("client_id"),"Authorization","bearer " + accesTokenService.getToken().getAccessToken()}
        );
    }

    //count how many games there are to fetch
    private igdbCount fetchgamesCount() {
        AccesTokenService accesTokenService = new AccesTokenService(fetchTools);

        return fetchTools.postToApi(gamesUri(), igdbCount.class,
                HttpRequest.BodyPublishers.ofString("where first_release_date > " + (TimeMapper.unixOf(LocalDateTime.now()) - 2678400) + "; "),
                new String[] {"Accept", "application/json", "Client-ID",System.getenv("client_id"),"Authorization","bearer " + accesTokenService.getToken().getAccessToken()}
        );
    }

    //count how many games there are to fetch
    private igdbCount fetchNewGamesCount() {
        AccesTokenService accesTokenService = new AccesTokenService(fetchTools);

        return fetchTools.postToApi(gamesUri(), igdbCount.class,
                HttpRequest.BodyPublishers.ofString("where first_release_date > " + (TimeMapper.unixOf(LocalDateTime.now()) - 2678400) + "; " +
                        "where created_at > " + (TimeMapper.unixOf(LocalDateTime.now()) - 86400) + ";"),
                new String[] {"Accept", "application/json", "Client-ID",System.getenv("client_id"),"Authorization","bearer " + accesTokenService.getToken().getAccessToken()}
        );
    }

    private static String gamesUri() {
        return "https://api.igdb.com/v4/games";
    }

    private static String gamesBody(int page, long unix) {
        return "fields name,first_release_date,summary,genres.name; \n" +
                "where first_release_date > " + unix + "; \n" +
                "sort first_release_date asc;\n" +
                "limit 500;\n" +
                "offset " + (500*page) + ";\n";
    }
}
