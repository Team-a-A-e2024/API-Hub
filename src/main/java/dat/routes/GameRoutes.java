package dat.routes;

import dat.controllers.impl.GameController;
import io.javalin.apibuilder.EndpointGroup;
import dat.security.enums.Role;

import static io.javalin.apibuilder.ApiBuilder.*;

public class GameRoutes {
    private final GameController gameController = new GameController();

    // Flertal: /api/games...
    public EndpointGroup collection() {
        return () -> {
            get("/",  gameController::readAll,Role.ANYONE);            // GET /api/games
            get("/search", gameController::searchByName,Role.ANYONE);  // GET /api/games/search?query=...
        };
    }
    // Ental: /api/game/{id}...
    public EndpointGroup item() {
        return () -> {
            get("/{id}",    gameController::read,Role.ANYONE);   // GET    /api/game/{id}
            put("/{id}",    gameController::update,Role.USER); // PUT    /api/game/{id}
            post("/", gameController::create,Role.ADMIN);       // POST   /api/game
            delete("/{id}", gameController::delete,Role.USER); // DELETE /api/game/{id}
        };
    }
}