package dat.Service;

import dat.dtos.GameDTO;
import dat.dtos.IgdbGame;
import dat.utils.GameMapper;
import dat.utils.TimeMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class IgdbGameService {
    private final static Logger logger = LoggerFactory.getLogger(IgdbGameService.class);
    private final FetchTools fetchTools;
    private final static String gamesURI = "https://api.igdb.com/v4/games";
    private final static String countURI = "https://api.igdb.com/v4/games/count";

    public IgdbGameService(FetchTools fetchTools) {
        this.fetchTools = fetchTools;
    }

    //todo: fix rate limit issues
    public List<GameDTO> getGames(final Long dateAdded) {
        int amount = fetchAmountOfGames(dateAdded).getCount();
        List<GameDTO> games = new ArrayList<>();
        //if there's no new games then return early
        if (amount <= 0) {
            return games;
        }
        List<Callable<IgdbGame[]>> tasks = new ArrayList<>();
        //a page has 500 games so we divide the total amount of games by 500 to get how many pages
        for (int i = 0; i < amount / 500 + 1; i++) {
            final int pageNumber = i;
            tasks.add(() -> fetchPageOfGames(pageNumber, dateAdded));
        }
        List<IgdbGame[]> toProcess = fetchTools.getFromApiList(tasks);
        logger.info("made " + tasks.size() + "amount of api calls, requesting " + amount + "of games");
        for (IgdbGame[] igdbGames : toProcess) {
            for (IgdbGame igdbGame : igdbGames) {
                games.add(GameMapper.mapIgdbToDTO(igdbGame));
            }
        }
        return games;
    }

    //page starts from index 0, dateAdded fetches from that day going forward
    public IgdbGame[] fetchPageOfGames(int page, long dateAdded) {
        return fetchTools.postToApi(gamesURI, IgdbGame[].class,
                HttpRequest.BodyPublishers.ofString(
                        //page number + today's date as a unix timestamp minus 31 days in seconds
                        gamesBody(page, dateAdded)),
                headerString()
        );
    }

    //count how many games there are to fetch
    public IgdbCount fetchAmountOfGames(long dateAdded) {
        return fetchTools.postToApi(countURI, IgdbCount.class,
                HttpRequest.BodyPublishers.ofString(countBody(TimeMapper.unixOf(LocalDateTime.now()) - 2678400, dateAdded)),
                headerString()
        );
    }

    private String[] headerString() {
        IgdbAccessTokenService accesTokenService = new IgdbAccessTokenService(fetchTools);
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
                "where first_release_date > " + (TimeMapper.unixOf(LocalDateTime.now().minus(31, ChronoUnit.DAYS))) + " & \n" +
                "created_at > " + dateAdded + ";" +
                "where first_release_date > " + (TimeMapper.unixOf(LocalDateTime.now().minus(31, ChronoUnit.DAYS))) + " & \n" +
                "updated_at > " + dateAdded + ";";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IgdbCount {
        private int count;
    }
}