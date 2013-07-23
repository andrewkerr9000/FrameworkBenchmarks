package hellovertx;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class WebServer extends Verticle
{
  @Override
  public void start()
  {
    RouteMatcher routeMatcher = new RouteMatcher()
       .get("/json", new Handler<HttpServerRequest>()
    {
      @Override
      public void handle(HttpServerRequest httpServerRequest)
      {
        handleJson(httpServerRequest);
      }
    })
       .get("/db", new Handler<HttpServerRequest>()
       {
         @Override
         public void handle(HttpServerRequest httpServerRequest)
         {
           handleDb(httpServerRequest);
         }
       });

    vertx
        .createHttpServer()
        .requestHandler(routeMatcher)
        .setAcceptBacklog(10000)
        .listen(8080);
  }

  private void handleJson(HttpServerRequest req)
  {
    JsonObject helloWorld = new JsonObject().putString("message", "Hello, world");
    req
        .response()
        .putHeader("Content-Type", "application/json; charset=UTF-8")
        .end(helloWorld.encode());
  }

  private void handleDb(final HttpServerRequest req)
  {
    final int queriesParam = getQueries(req);
    final Random random = ThreadLocalRandom.current();
    final JsonArray worlds = new JsonArray(new ArrayList<>(queriesParam));

    for (int i = 0; i < queriesParam; i++)
    {
      JsonObject mongoQuery = new JsonObject()
          .putString("action", "findone")
          .putString("collection", "world")
          .putObject("matcher", new JsonObject().putNumber("id", (random.nextInt(10000) + 1)));

      this.getVertx().eventBus().send(
          "hello.persistor",
          mongoQuery,
          new Handler<Message<JsonObject>>()
          {
            @Override
            public void handle(Message<JsonObject> reply)
            {
              JsonObject body = reply.body();
              JsonObject result = null;
              if (body != null && "ok".equals(body.getString("status")))
              {
                result = body.getObject("result");
              }

              if (result == null)
              {
                result = new JsonObject();
              }

              worlds.addObject(result);

              if (worlds.size() == queriesParam)
              {
                // All queries have completed; send the response.
                req.response()
                    .putHeader("Content-Type", "application/json; charset=UTF-8")
                    .end(worlds.encode());
              }
            }
          }
      );
    }
  }

  private int getQueries(HttpServerRequest req)
  {
    try
    {
      String queriesString = req.params().get("queries");
      if (queriesString != null)
      {
        return Integer.parseInt(queriesString);
      } else
      {
        return 1;
      }
    } catch (NumberFormatException nfe)
    {
      return 1;
    }
  }

}
