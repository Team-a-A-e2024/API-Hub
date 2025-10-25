package dat.controllers.impl;

import dat.exceptions.ApiException;
import dat.exceptions.Message;
import dat.routes.Routes;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionController {
    private final Logger LOGGER = LoggerFactory.getLogger(Routes.class);

    public void apiExceptionHandler(ApiException e, Context ctx) {
        LOGGER.error(ctx.attribute("requestInfo") + " " + ctx.res().getStatus() + " " + e.getMessage());
        ctx.status(e.getStatusCode());
        ctx.json(new Message(e.getStatusCode(), e.getMessage()));
    }
    public void exceptionHandler(Exception e, Context ctx) {
        LOGGER.error(ctx.attribute("requestInfo") + " " + ctx.res().getStatus() + " " + e.getMessage());
        ctx.status(500);
        ctx.json(new Message(500, e.getMessage()));
    }
    // ---- e-helpers ----
    public static void e1(Context ctx) {
        ctx.status(404).json(new Message(404, "No content found for this request"));
    }

    public static void e2(Context ctx, String f) {
        ctx.status(400).json(new Message(400, "Field '" + f + "' is required"));
    }

    public static void e3(Context ctx, String w) {
        ctx.status(400).json(new Message(400, "Could not update '" + w + "'"));
    }

    public static void e4(Context ctx, String w) {
        ctx.status(404).json(new Message(404, "Could not delete '" + w + "'"));
    }

    public static void e5(Context ctx) {
        ctx.status(403).json(new Message(403, "Access denied, you do not have permission for this resource"));
    }
}
