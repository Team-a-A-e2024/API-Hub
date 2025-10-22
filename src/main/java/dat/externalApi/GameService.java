package dat.externalApi;

import dat.entities.Game;
import dat.utils.TimeMapper;

import java.net.http.HttpRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class GameService {
    private final FetchTools fetchTools;

    public GameService(FetchTools fetchTools) { this.fetchTools = fetchTools; }

    public List<Game> getNewlyAddedGames(){
        List<Game> games = new ArrayList<>();
        List<Callable<igdbGame[]>> tasks = new ArrayList<>();

        //if theres no new games then return early
        int amount = fetchNewGamesCount().getCount();
        if (amount <=0){
            return games;
        }

        for(int i = 0; i < amount/500 + 1; i++){
            final int pagenumber = i;
            tasks.add(()-> fetchNewGames(pagenumber));
        }
        List<igdbGame[]> toProcess = fetchTools.getFromApiList(tasks);

        for(igdbGame[] igdbGames : toProcess){
            for (igdbGame igdbGame : igdbGames){
                LocalDate release = null;
                if(igdbGame.getFirstReleaseDate() != null){
                    release = Instant.ofEpochSecond(igdbGame.getFirstReleaseDate())
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                }

                games.add(Game.builder()
                        .name(igdbGame.getName())
                        .firstReleaseDate(release)
                        .summary(igdbGame.getSummary())
                        .build());
            }
        }
        return games;
    }

    public List<Game> getGames(){
        List<Game> games = new ArrayList<>();
        List<Callable<igdbGame[]>> tasks = new ArrayList<>();

        //if theres no new games then return early
        int amount = fetchGamesCount().getCount();
        if (amount <=0){
            return games;
        }

        for(int i = 0; i < amount/500 + 1; i++){
            final int pagenumber = i;
            tasks.add(()-> fetchGames(pagenumber));
        }
        List<igdbGame[]> toProcess = fetchTools.getFromApiList(tasks);

        for(igdbGame[] igdbGames : toProcess){
            for (igdbGame igdbGame : igdbGames){
                games.add(Game.builder()
                        .name(igdbGame.getName())
                        .firstReleaseDate(Instant
                                .ofEpochSecond(igdbGame.getFirstReleaseDate())
                                .atZone(ZoneId.systemDefault()).toLocalDate())
                        .summary(igdbGame.getSummary())
                        .build());
            }
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
    private igdbCount fetchGamesCount() {
        AccesTokenService accesTokenService = new AccesTokenService(fetchTools);

        return fetchTools.postToApi("https://api.igdb.com/v4/games/count", igdbCount.class,
                HttpRequest.BodyPublishers.ofString("where first_release_date > " + (TimeMapper.unixOf(LocalDateTime.now()) - 2678400) + "; "),
                new String[] {"Accept", "application/json", "Client-ID",System.getenv("client_id"),"Authorization","bearer " + accesTokenService.getToken().getAccessToken()}
        );
    }

    //count how many games there are to fetch
    private igdbCount fetchNewGamesCount() {
        AccesTokenService accesTokenService = new AccesTokenService(fetchTools);

        return fetchTools.postToApi("https://api.igdb.com/v4/games/count", igdbCount.class,
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
