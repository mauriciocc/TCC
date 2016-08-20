package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration._

object Crud {
  val crud = exec(newTodo)
    //.pause(1)
    .exec(newTodo.check(jsonPath("$.id").ofType[Int].saveAs("newTodoId")))
    //.pause(200 milliseconds, 1000 milliseconds)
    .exec(findOne)
    //.pause(200 milliseconds, 1000 milliseconds)
    .exec(http("Delete").delete("/api/todos/${foundTodoId}"))
    //.pause(200 milliseconds, 1000 milliseconds)
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
  val httpConf = http.baseURL("http://10.1.1.101:8080")

  setUp(
    scenario("TodoSimulation - Users/S")
      .exec(Crud.crud)
      .inject(atOnceUsers(1000))
      //.inject(rampUsers(1000) over (10 seconds))
  ).protocols(httpConf)

}
