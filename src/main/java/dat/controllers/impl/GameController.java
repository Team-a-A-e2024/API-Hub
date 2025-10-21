package dat.controllers.impl;

import dat.config.HibernateConfig;
import dat.controllers.IController;
import dat.daos.impl.GameDAO;
import dat.dtos.GameDTO;
import dat.exceptions.Message;
import io.javalin.http.Context;

import java.util.List;

public class GameController implements IController<GameDTO, Integer> {

    private final GameDAO dao;

    public GameController() {
        this.dao = GameDAO.getInstance();
    }

    // ---- e-helpers ----
    private void e1(Context ctx) {
        ctx.status(404).json(new Message(404, "No content found for this request"));
    }

    private void e2(Context ctx, String f) {
        ctx.status(400).json(new Message(400, "Field '" + f + "' is required"));
    }

    private void e3(Context ctx, String w) {
        ctx.status(400).json(new Message(400, "Could not update '" + w + "'"));
    }

    private void e4(Context ctx, String w) {
        ctx.status(404).json(new Message(404, "Could not delete '" + w + "'"));
    }

    @Override
    public void read(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        GameDTO dto = dao.read(id);
        if (dto == null) {
            e1(ctx);
            return;
        }
        ctx.status(200).json(dto);
    }

    @Override
    public void readAll(Context ctx) {
        List<GameDTO> list = dao.readAll();
        ctx.status(200).json(list, GameDTO.class);
    }

    @Override
    public void create(Context ctx) {
        GameDTO req = validateEntity(ctx);
        if (req.getId() != null) {
            e2(ctx, "id");
            return;
        }
        if (req.getName() == null || req.getName().isBlank()) {
            e2(ctx, "name");
            return;
        }
        GameDTO saved = dao.create(req);
        ctx.header("Location", "/api/games/" + saved.getId());
        ctx.status(201).json(saved, GameDTO.class);
    }

    @Override
    public void update(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        GameDTO req = validateEntity(ctx);
        if (req.getName() == null || req.getName().isBlank()) {
            e2(ctx, "name");
            return;
        }
        GameDTO updated = dao.update(id, req);
        if (updated == null) {
            e3(ctx, "game");
            return;
        }
        ctx.status(200).json(updated, GameDTO.class);
    }

    @Override
    public void delete(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        if (!dao.validatePrimaryKey(id)) {
            e4(ctx, "game");
            return;
        }
        dao.delete(id);
        ctx.status(204);
    }

    @Override
    public boolean validatePrimaryKey(Integer id) {
        return dao.validatePrimaryKey(id);
    }

    @Override
    public GameDTO validateEntity(Context ctx) {
        return ctx.bodyValidator(GameDTO.class)
                .check(g -> g.getSummary() == null || g.getSummary().length() <= 10_000, "summary too long")
                .get();
    }

    public void searchByName(Context ctx) {
        String q = ctx.queryParam("query");
        if (q == null || q.isBlank()) {
            e2(ctx, "query");
            return;
        }
        List<GameDTO> hits = dao.searchByName(q);
        if (hits == null || hits.isEmpty()) {
            e1(ctx);
            return;
        }
        GameDTO best = hits.stream()
                .filter(g -> g.getName() != null && g.getName().equalsIgnoreCase(q))
                .findFirst()
                .orElse(hits.get(0));
        ctx.status(200).json(best, GameDTO.class);
    }
}