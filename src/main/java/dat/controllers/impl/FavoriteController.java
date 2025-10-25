package dat.controllers.impl;

import dat.config.HibernateConfig;
import dat.daos.impl.GameDAO;
import dat.dtos.GameDTO;
import dat.security.controllers.SecurityController;
import dat.security.daos.SecurityDAO;
import dat.security.dtos.UserDTO;
import io.javalin.http.Context;

import java.util.List;
import java.util.stream.Collectors;

public class FavoriteController {

    private final SecurityDAO securityDAO;
    private final GameDAO gameDAO;
    private final SecurityController securityController;

    public FavoriteController() {
        this.securityDAO = new SecurityDAO(HibernateConfig.getEntityManagerFactory());
        this.gameDAO = GameDAO.getInstance();
        this.securityController = SecurityController.getInstance();
    }

    public void getAllFavoritesByUserId(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).check(this::validateUserPrimaryKey, "Not a valid id").get();

        UserDTO user = new UserDTO(securityDAO.getUserById(id));
        List<GameDTO> favorites = user.getGames().stream().map(GameDTO::new).collect(Collectors.toList());

        ctx.res().setStatus(200);
        ctx.json(favorites, GameDTO.class);
    }

    public void addFavorite(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).check(this::validateGamePrimaryKey, "Not a valid id").get();
        String header = ctx.header("Authorization");

        if (header == null) {
            ExceptionController.e5(ctx);
            return;
        }

        securityDAO.addFavoriteGame(getUserFromHeader(header).getId(), id);

        ctx.status(201);
    }

    public void removeFavorite(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).check(this::validateGamePrimaryKey, "Not a valid id").get();
        String header = ctx.header("Authorization");

        if (header == null) {
            ExceptionController.e5(ctx);
            return;
        }

        UserDTO userDTO = getUserFromHeader(header);
        GameDTO gameDTO = gameDAO.read(id);

        if (userDTO.getGames().stream().noneMatch(game -> game.getId().equals(id))) {
            ExceptionController.e4(ctx, gameDTO.getName());
            return;
        }

        securityDAO.removeFavoriteGame(userDTO.getId(), id);

        ctx.status(204);
    }

    public boolean validateUserPrimaryKey(Integer id) { return securityDAO.validatePrimaryKey(id); }
    public boolean validateGamePrimaryKey(Integer id) { return gameDAO.validatePrimaryKey(id); }

    private UserDTO getUserFromHeader(String header) {
        String[] headerParts = header.split(" ");
        String token = headerParts[1];
        UserDTO userDTO = securityController.verifyToken(token);
        return new UserDTO(securityDAO.getUserByUsername(userDTO.getUsername()));
    }
}
