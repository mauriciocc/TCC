package com.simple.app.todo.async;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.StringUnmarshallers;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.util.Timeout;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.pgasync.ConnectionPoolBuilder;
import com.github.pgasync.Db;
import com.simple.app.todo.async.domain.Todo;
import com.simple.app.todo.async.service.TodoActor;
import com.simple.app.todo.async.service.TodoService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;

public class TodoAsyncApp extends AllDirectives {

    public static final Timeout TIMEOUT = Timeout.apply(5, TimeUnit.MINUTES);

    public static ObjectMapper om() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static Db db() {
        Config conf = ConfigFactory.load();
        return new ConnectionPoolBuilder()
                .hostname(conf.getString("db.host"))
                .port(conf.getInt("db.port"))
                .database(conf.getString("db.name"))
                .username(conf.getString("db.user"))
                .password(conf.getString("db.pass"))
                .poolSize(8)
                .build();
    }

    public static TodoService todoService(Db db) {
        return new TodoService(db);
    }

    public static ActorRef todoActor(ActorSystem system, TodoService todoService) {
        return system.actorOf(TodoActor.props(todoService));
    }

    public static void main(String[] args) throws IOException {
        // boot up server using the route as defined below
        ActorSystem system = ActorSystem.create();

        // HttpApp.bindRoute expects a route being provided by HttpApp.createRoute
        final TodoAsyncApp app = new TodoAsyncApp();

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute(system).flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", 8080), materializer);

        System.out.println("Type RETURN to exit");
        System.in.read();

        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());
    }


    public Route createRoute(ActorSystem system) {
        Db db = db();
        TodoService todoService = new TodoService(db);
        ActorRef todoActor = todoActor(system, todoService);
        ObjectMapper om = om();
        Marshaller<Object, RequestEntity> marshaller = Jackson.marshaller(om);
        Unmarshaller<HttpEntity, Todo> unmarshaller = Jackson.unmarshaller(om, Todo.class);

        return pathPrefix("api", () ->
                pathPrefix("todos", () ->
                        route(
                                path(StringUnmarshallers.LONG, id ->
                                        route(
                                                get(() ->
                                                        completeOKWithFuture(
                                                                ask(todoActor, new TodoActor.FindOne(id), TIMEOUT),
                                                                marshaller
                                                        )),
                                                put(() -> entity(unmarshaller, todo ->
                                                        completeOKWithFuture(
                                                                ask(todoActor, new TodoActor.Save(todo), TIMEOUT),
                                                                marshaller
                                                        )
                                                )),
                                                delete(() ->
                                                        completeOKWithFuture(
                                                                ask(todoActor, new TodoActor.Remove(id), TIMEOUT),
                                                                marshaller
                                                        )
                                                )
                                        )
                                ),
                                get(() ->
                                        completeOKWithFuture(
                                                ask(todoActor, new TodoActor.FindAll(), TIMEOUT),
                                                marshaller
                                        )
                                ),
                                post(() -> entity(unmarshaller, todo ->
                                        completeOKWithFuture(
                                                ask(todoActor, new TodoActor.Save(todo), TIMEOUT),
                                                marshaller
                                        )
                                ))
                        )
                )
        );
    }

}