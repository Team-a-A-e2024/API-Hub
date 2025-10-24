package dat.routes;

import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class UserRoutes {

    private final FavoriteRoutes favoriteRoutes = new FavoriteRoutes();

    public EndpointGroup item() {
        return () -> {
            path("/{id}", favoriteRoutes.collection());
        };
    }
}
