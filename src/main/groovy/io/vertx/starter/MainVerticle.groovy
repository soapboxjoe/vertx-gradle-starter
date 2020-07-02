package io.vertx.starter

import io.vertx.config.ConfigRetriever
import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpServer

import java.util.jar.Attributes
import java.util.jar.Manifest

class MainVerticle extends AbstractVerticle {

  static final String GIT_BRANCH_NAME = "gitBranchName"
  static final String DEFAULT_BRANCH_NAME = "master"

  // IMPORTANT! Redis key must be a hash or it will not work!
  Map store = [
    type:"redis",
    config:[
      host:"localhost",
      port:6379,
      key:"${getGitBranchName()}joe.test"
    ]
  ]

  Map options = [
    scanPeriod:2000,
    stores:[
      store
    ]
  ]

  HttpServer httpServer

  @Override
  void start() {
    ConfigRetriever retriever = ConfigRetriever.create(vertx, options)
    retriever.getConfig({ ar ->
      if (ar.failed()) {
        println "ERROR: Config not found!"
      } else {
        def cfg = ar.result()
        if(cfg){
          startHttpServer(cfg)
        }else{
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

  void startHttpServer(cfg){
    println "VALUE IS: " + System.getProperty("gitBranch")

    if(httpServer){
      println "Stopping http server"
      httpServer.close()
    }

    println "Starting http server on port ${cfg.port}"
    httpServer = vertx.createHttpServer()
        .requestHandler({req -> req.response().end(cfg.welcomeMessage + ", Git: " + System.getProperty("gitBranch").toString())})
        .listen(cfg.port as Integer)
  }

  static String getGitBranchName(){
    if(System.hasProperty(GIT_BRANCH_NAME)){
      return System.getProperty(GIT_BRANCH_NAME)
    }

    String className = getClass().getSimpleName() + ".class"
    String classPath = getClass().getResource(className).toString()
    if (!classPath.startsWith("jar")) {
      return DEFAULT_BRANCH_NAME
    }

    URL url = new URL(classPath)
    JarURLConnection jarConnection = (JarURLConnection) url.openConnection()
    Manifest manifest = jarConnection.getManifest()
    Attributes attributes = manifest.getMainAttributes()
    return attributes.getValue(GIT_BRANCH_NAME)
  }
}
