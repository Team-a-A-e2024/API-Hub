package dat.routes;

import dat.controllers.impl.FavoriteController;
import dat.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class FavoriteRoutes {
    private final FavoriteController favoriteController = new FavoriteController();

    public EndpointGroup collection() {
        return () -> {
            get("/favorites", favoriteController::getAllFavoritesByUserId, Role.ANYONE);
        };
    }

    public EndpointGroup item() {
        return () -> {
            post("/favorite", favoriteController::addFavorite, Role.USER);
            delete("/favorite", favoriteController::removeFavorite, Role.USER);
        };
    }
}
