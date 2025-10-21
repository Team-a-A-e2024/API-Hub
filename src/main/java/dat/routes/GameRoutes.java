package dat.routes;

import dat.controllers.impl.GameController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class GameRoutes {
    private final GameController gameController = new GameController();

    // Flertal: /api/games...
    public EndpointGroup collection() {
        return () -> {
            get("/",  gameController::readAll);            // GET /api/games
            post("/", gameController::create);             // POST /api/games
            get("/search", gameController::searchByName);  // GET /api/games/search?query=...
        };
    }
    // Ental: /api/game/{id}...
    public EndpointGroup item() {
        return () -> {
            get("/{id}",    gameController::read);   // GET    /api/game/{id}
            put("/{id}",    gameController::update); // PUT    /api/game/{id}
            delete("/{id}", gameController::delete); // DELETE /api/game/{id}
        };
    }
}