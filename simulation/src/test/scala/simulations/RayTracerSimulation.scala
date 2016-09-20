package simulations

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class RayTracerSimulation extends Simulation {

  val conf = ConfigFactory.load()
  val httpConf = http.baseURL(conf.getString("baseUrl"))

  setUp(
    scenario("RayTracer")
      .exec(http("Render").get("/api/render"))
      .inject(atOnceUsers(100))
  ).protocols(httpConf)

}
