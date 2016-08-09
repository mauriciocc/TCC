package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration._

object Crud {
  val crud =
    exec(findAll)
      .exec(newTodo)
      .exec(newTodo.check(jsonPath("$.id").ofType[Int].saveAs("newTodoId")))
      .exec(
        http("FindOne")
          .get("/api/todos/${newTodoId}")
          .check(jsonPath("$.id").ofType[Int]
            .saveAs("foundTodoId")
          ))
      .exec(http("Delete").delete("/api/todos/${foundTodoId}"))

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
  val httpConf = http.baseURL("http://localhost:8080")

  val scn = scenario("TodoSimulation").exec(Crud.crud)

  setUp(
    scn.inject(rampUsers(80000) over (600 seconds))
  ).protocols(httpConf)

}
