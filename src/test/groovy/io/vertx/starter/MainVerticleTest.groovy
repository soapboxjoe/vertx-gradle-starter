package io.vertx.starter

import io.vertx.core.Vertx
import io.vertx.ext.unit.Async
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.JsonObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.ServerSocket
import io.vertx.core.logging.LoggerFactory

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {

  private Vertx vertx
  private int port
  private logger = LoggerFactory.getLogger(MainVerticleTest)

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx()
    port = new ServerSocket(0).getLocalPort()
    logger.info("Running on port $port")
    def opts = new DeploymentOptions() .setConfig(new JsonObject().put("http.port", port))
    vertx.deployVerticle(MainVerticle.class.getName(), opts, tc.asyncAssertSuccess())
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess())
  }

  @Test
  public void testThatTheServerIsStarted(TestContext tc) {
    Async async = tc.async()
    vertx.createHttpClient().getNow(port, "localhost", "/", { response -> 
      tc.assertEquals(response.statusCode(), 200)
      response.bodyHandler({body -> 
        tc.assertTrue(body.length() > 0)
        async.complete()
      })
    })
  }

}