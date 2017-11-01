package io.vertx.starter

import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.JsonObject

public class MainVerticle extends AbstractVerticle {

  @Override
  void start() {
    vertx.createHttpServer()
        .requestHandler({req -> req.response().end("Hello Vert.x groovy style!!")})
        .listen(config().getInteger("http.port", 8080))
  }

}
