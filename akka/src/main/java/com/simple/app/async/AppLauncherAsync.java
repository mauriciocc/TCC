package com.simple.app.async;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.StringUnmarshallers;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.util.Timeout;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.pgasync.ConnectionPoolBuilder;
import com.github.pgasync.Db;
import com.simple.app.Launcher;
import com.simple.app.async.raytracer.RayTracerCoordinator;
import com.simple.app.async.raytracer.RayTracerExecutor;
import com.simple.app.async.report.HttpActor;
import com.simple.app.async.todo.domain.Todo;
import com.simple.app.async.todo.service.TodoActor;
import com.simple.app.async.todo.service.TodoService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static akka.pattern.PatternsCS.ask;


public class AppLauncherAsync extends AllDirectives {

    private static final Logger LOG = LoggerFactory.getLogger(AppLauncherAsync.class);

    public static final Timeout TIMEOUT = Timeout.apply(5, TimeUnit.MINUTES);

    public static ObjectMapper om() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static Db db(Config conf) {
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
        Launcher launcher = new Launcher(args);
        Config conf = ConfigFactory.load();

        final Path path = Paths.get(launcher.configOr("application.properties"));

        boolean fileExists = Files.exists(path);
        LOG.debug("Trying to load config from '{}' - Exists: {}", path.toAbsolutePath(), fileExists);
        if (fileExists) {
            conf = ConfigFactory.parseFile(path.toFile()).withFallback(conf);
        }

        ActorSystem system = ActorSystem.create();

        // HttpApp.bindRoute expects a route being provided by HttpApp.createRoute
        final AppLauncherAsync app = new AppLauncherAsync();

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);


        Config finalConf = conf;
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = launcher.on(
                () -> app.rayTracerRoute(finalConf, system).flow(system, materializer),
                () -> app.reportRoute(finalConf, system, materializer).flow(system, materializer),
                () -> app.todoRoute(finalConf, system).flow(system, materializer)
        );
        ConnectHttp localhost = ConnectHttp.toHost(conf.getString("app.host"), conf.getInt("app.port"));
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow, localhost, materializer)
                .whenComplete((serverBinding, throwable) -> {
                    if (throwable == null) {
                        System.out.println("Server binding ok - " + serverBinding);
                    } else {
                        throwable.printStackTrace();
                    }
                });

    }

    public Route reportRoute(Config conf, ActorSystem system, Materializer materializer) {
        final Base64.Encoder encoder = Base64.getEncoder();
        BiFunction<String, byte[], String> fn = (json, image) ->
                "<pre>" + json + "</pre>" +
                        "<img src='data:image/png;" + encoder.encodeToString(image) + "'/>";
        Marshaller<String, RequestEntity> htmlMarsh = Marshaller.stringToEntity();
        ActorRef httpActor = system.actorOf(
                HttpActor.props(
                        materializer,
                        conf.getString("report.todo.url"),
                        conf.getString("report.raytracer.url")
                )
        );
        return pathPrefix("api", () ->
                path("report", () ->
                        get(() -> {
                                    CompletableFuture todos = ask(httpActor, HttpActor.Message.FIND_TODOS, TIMEOUT).toCompletableFuture();
                                    CompletableFuture image = ask(httpActor, HttpActor.Message.RENDER_IMAGE, TIMEOUT).toCompletableFuture();
                                    CompletableFuture<String> all = CompletableFuture.allOf(todos, image)
                                            .thenApply(aVoid -> fn.apply((String) todos.join(), (byte[]) image.join()));
                                    return completeOKWithFuture(all, htmlMarsh);
                                }
                        )
                )
        );
    }

    public Route rayTracerRoute(Config conf, ActorSystem system) {
        Marshaller<byte[], RequestEntity> byteMarshaller = Marshaller.wrapEntity(b -> b, Marshaller.byteArrayToEntity(), MediaTypes.IMAGE_PNG);
        ActorRef rayTracer = system.actorOf(RayTracerCoordinator.props());
        return pathPrefix("api", () ->
                path("render", () ->
                        get(() -> {
                                    CompletionStage ask = ask(rayTracer, new RayTracerExecutor.Render(), TIMEOUT);
                                    return completeOKWithFuture(ask, byteMarshaller);
                                }
                        )
                )
        );
    }

    public Route todoRoute(Config conf, ActorSystem system) {
        Db db = db(conf);
        db.queryRows("SELECT 1").toBlocking().first();
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