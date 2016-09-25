package simulations

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration._

object Crud {
  val crud = exec(newTodo)
    .exec(newTodo.check(jsonPath("$.id").ofType[Int].saveAs("newTodoId")))
    .exec(findOne)
    .exec(http("Delete").delete("/api/todos/${foundTodoId}"))
    .exec(findAll)

  def findOne: HttpRequestBuilder = {
    http("FindOne")
      .get("/api/todos/${newTodoId}")
      .check(jsonPath("$.id").ofType[Int]
        .saveAs("foundTodoId")
      )
  }

  def updateTodo: HttpRequestBuilder = {
    http("Update")
      .put("/api/todos/${newTodoId}")
      .header("Content-Type", "application/json")
      .body(
        StringBody("""{"owner": "Teste Edit", "title": "Titulo de Testes Edit", "description": "Descrição de Testes Edit" }"""))

  }

  def findAll: HttpRequestBuilder = {
    http("FindAll").get("/api/todos")
  }

  def newTodo: HttpRequestBuilder = {
    http("Create")
      .post("/api/todos")
      .header("Content-Type", "application/json")
      .body(StringBody("""{"owner": "Teste", "title": "Titulo de Testes", "description": "Descrição de Testes" }"""))
  }
}

class TodoSimulation extends Simulation {

  val conf = ConfigFactory.load()
  val httpConf = http.baseURL(conf.getString("baseUrl"))

  setUp(
    scenario("TodoSimulation - Users/S")
      .exec(Crud.crud)
      .inject(atOnceUsers(conf.getInt("todo.load")))
      //.inject(rampUsers(1000) over (10 seconds))
  ).protocols(httpConf)

}
