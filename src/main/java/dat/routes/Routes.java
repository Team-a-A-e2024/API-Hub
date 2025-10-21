package dat.routes;

import io.javalin.apibuilder.EndpointGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes {
    private static final Logger logger = LoggerFactory.getLogger(Routes.class);

    private final GameRoutes gameRoutes = new GameRoutes();

    public EndpointGroup getRoutes() {
        logger.info("Registering API routes...");
        return () -> {
            get("/", ctx -> ctx.result("API is running"));
            path("/games", gameRoutes.collection()); // GET /api/games, POST /api/games, GET /api/games/search
            path("/game", gameRoutes.item());       // GET/PUT/DELETE /api/game/{id}
            logger.info("Game routes registered under /game & /games");
        };
    }
}