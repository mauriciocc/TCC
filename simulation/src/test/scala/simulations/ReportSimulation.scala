package simulations

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class ReportSimulation extends Simulation {

  val conf = ConfigFactory.load()
  val httpConf = http.baseURL(conf.getString("baseUrl"))

  setUp(
    scenario("Report Simulation")
      .exec(http("Render").get("/api/report"))
      .inject(atOnceUsers(conf.getInt("report.load")))
  ).protocols(httpConf)

}
