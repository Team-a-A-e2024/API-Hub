package dat.externalApi;

import dat.dtos.GameDTO;
import dat.utils.GameMapper;
import dat.utils.TimeMapper;

import java.net.http.HttpRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class GameService {
    private final FetchTools fetchTools;
    private static LocalDateTime LastUpdate;

    public GameService(FetchTools fetchTools) {
        this.fetchTools = fetchTools;
    }


    public List<GameDTO> getGames(final Long dateAdded) {
        int amount = fetchAmountOfGames(dateAdded).getCount();

        List<GameDTO> games = new ArrayList<>();

        //if theres no new games then return early
        if (amount <= 0) {
            return games;
        }

        List<Callable<IgdbGame[]>> tasks = new ArrayList<>();
        //a page has 500 games so we devide the total amount of games by 500 to get how many pages
        for (int i = 0; i < amount / 500 + 1; i++) {
            final int pagenumber = i;
            tasks.add(() -> fetchPageOfGames(pagenumber, dateAdded));
        }
        System.out.println(amount);
        System.out.println(amount / 500 + 1);
        List<IgdbGame[]> toProcess = fetchTools.getFromApiList(tasks);



        for (IgdbGame[] igdbGames : toProcess) {
            for (IgdbGame igdbGame : igdbGames) {
                games.add(GameMapper.MapIgdbGameToGame(igdbGame));
            }
        }
        return games;
    }

    //page starts from index 0, dateAdded fetches from that day going forward
    private IgdbGame[] fetchPageOfGames(int page, long dateAdded) {
        return fetchTools.postToApi("https://api.igdb.com/v4/games", IgdbGame[].class,
                HttpRequest.BodyPublishers.ofString(
                        //page number + todays date as a unix timestamp minus 31 days in seconds
                        gamesBody(page, dateAdded)),
                headerString()
        );
    }

    //count how many games there are to fetch
    private IgdbCount fetchAmountOfGames(long dateAdded) {
        AccesTokenService accesTokenService = new AccesTokenService(fetchTools);
        return fetchTools.postToApi("https://api.igdb.com/v4/games/count", IgdbCount.class,
                HttpRequest.BodyPublishers.ofString(countBody(TimeMapper.unixOf(LocalDateTime.now()) - 2678400, dateAdded)),
                headerString()
        );
    }

    private String[] headerString() {
        AccesTokenService accesTokenService = new AccesTokenService(fetchTools);
        return new String[]{"Accept", "application/json", "Client-ID", System.getenv("client_id"), "Authorization", "bearer " + accesTokenService.getToken().getAccessToken()};
    }

    private static String countBody(long releaseDate, long dateAdded) {
        return "where first_release_date > " + releaseDate + " & " +
                "created_at > " + dateAdded + ";";
    }

    private static String gamesBody(int page, long dateAdded) {
        return "fields name,first_release_date,summary,genres.name; \n" +
                "sort first_release_date asc;\n" +
                "limit 500;\n" +
                "offset " + (500 * page) + ";\n" +
                "where first_release_date > " + (TimeMapper.unixOf(LocalDateTime.now()) - 2678400) + " & \n" +
                "created_at > " + dateAdded + ";";
    }
}
