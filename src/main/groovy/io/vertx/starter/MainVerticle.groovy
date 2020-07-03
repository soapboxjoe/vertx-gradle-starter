package io.vertx.starter

import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpServer
import io.vertx.core.json.JsonObject
import io.vertx.redis.RedisOptions
import io.vertx.redis.impl.RedisConnection

// IMPORTANT! Redis key must be a hash or it will not work!
class MainVerticle extends AbstractVerticle {
  HttpServer httpServer

  @Override
  void start() {
    // TODO we should check if key exists in redis first and if not use 'master'
    String gitBranchName = getGitBranchName()
    ConfigStoreOptions redisStore = new ConfigStoreOptions()
    redisStore.type = "redis"
    redisStore.setConfig(new JsonObject([
      host: "localhost",
      port: 6379,
      key : "${gitBranchName}.joe.test"
    ]))
    ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(redisStore)
    options.scanPeriod = 2000
    ConfigRetriever retriever = ConfigRetriever.create(vertx, options)
    retriever.getConfig({ ar ->
      if (ar.failed()) {
        println "ERROR: Config not found! Key \"${gitBranchName}.joe.test\" does not exist!"
      } else {
        def cfg = ar.result()
        if (cfg) {
          startHttpServer(cfg)
        } else {
          println "ERROR: Config not found!"
        }
      }
    })

    // Listen to changes to the config
    retriever.listen({ change ->
      println change.newConfiguration

      // Changed so restart the server with new config
      startHttpServer(change.newConfiguration)
    })
  }

  /**
   * Reads the git branch name from first System properties and then from
   * MANIFEST.MF can override with -P gitBranch=name
   * @return
   */
  static String getGitBranchName() {
    if (System.getProperty("gitBranch")) {
      return System.getProperty("gitBranch")
    }

    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    Enumeration<URL> resources = loader.getResources("META-INF/MANIFEST.MF");

    Properties prop = new Properties()
    resources.each {
      prop.load(it.openStream())
    }
    return prop.get("Git-Branch")
  }

  void startHttpServer(cfg) {
    if (httpServer) {
      println "Stopping http server"
      httpServer.close()
    }

    println "Starting http server on port ${cfg.port}"
    httpServer = vertx.createHttpServer()
      .requestHandler({ req -> req.response().end(cfg.welcomeMessage + ", Git: " + getGitBranchName()) })
      .listen(cfg.port as Integer)
  }
}
