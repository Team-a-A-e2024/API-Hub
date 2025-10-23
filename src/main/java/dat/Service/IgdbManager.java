package dat.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dat.daos.impl.GameDAO;
import dat.utils.TimeMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IgdbManager {
    private final static Logger logger = LoggerFactory.getLogger(IgdbManager.class);
    private static IgdbManager instance;
    private IgdbGameService gameService;
    private static LocalDateTime LastUpdate;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final  String path = "logs/igdbApiPull-Log.txt";

    private IgdbManager() {}

    public static IgdbManager getInstance() {
        if (instance == null) instance = new IgdbManager();
        return instance;
    }

    public void setFetchTools(FetchTools fetchTools){
        gameService = new IgdbGameService(fetchTools);
    }

    public void start() {
        logger.info("running startup igdb api fetching");
        ObjectMapper objectMapper = new ObjectMapper();

        File file = new File(path);
        try {
            if (file.exists()) {
                UpdateLog ul = objectMapper.readValue(file, UpdateLog.class);
                LastUpdate = LocalDateTime.parse(ul.getLastUpdate(), formatter);
                logger.info("success while reading file");
            } else {
                logger.error("file not found");

            }
        } catch (IOException e) {
            logger.error("error while reading file");
        }

        //should only run if file wasn't read as it gets everything
        if (LastUpdate == null) {
            gameService.getGames(0L).forEach(GameDAO.getInstance()::create);
        } else {
            gameService.getGames(TimeMapper.unixOf(LastUpdate)).forEach(GameDAO.getInstance()::create);
        }

        //replace file with new one
        try {
            LastUpdate = LocalDateTime.now();
            objectMapper.writeValue(new File(path), new UpdateLog(LastUpdate.format(formatter)));
            logger.info("success writing to file");
        } catch (IOException e) {
            logger.error("error while writing to file");
            logger.error("please resolve as quickly as possible");
        }

        //creates a thread that runs once a day
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::update, 0, 1, TimeUnit.DAYS);
    }

    //todo: we only pull games 30 days back, meaning if theres no update in over 30 days we don't get any game :)
    public void update() {
        logger.info("running igdb fetch update");
        ObjectMapper objectMapper = new ObjectMapper();

        //if there hasn't been an update for 24 hours.
        if (LastUpdate.until(LocalDateTime.now(), ChronoUnit.SECONDS) > 28.800) {
            //gets every added game since last update
            gameService.getGames(TimeMapper.unixOf(LastUpdate)).forEach(GameDAO.getInstance()::create);

            try {
                LastUpdate = LocalDateTime.now();
                objectMapper.writeValue(new File(path), new UpdateLog(LastUpdate.format(formatter)));
                logger.info("success writing to file");
            } catch (IOException e) {
                logger.error("error while writing to file");
                logger.error("please resolve as quickly as possible");
            }
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class UpdateLog {
        private String LastUpdate;
    }
}
