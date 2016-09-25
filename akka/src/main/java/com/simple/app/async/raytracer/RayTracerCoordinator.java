package com.simple.app.async.raytracer;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Router;

import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class RayTracerCoordinator extends UntypedActor {

    private final Router router = new Router(
            new RoundRobinRoutingLogic(),
            IntStream.rangeClosed(1, Runtime.getRuntime().availableProcessors())
                    .mapToObj(value -> {
                        ActorRef r = getContext().actorOf(Props.create(RayTracerExecutor.class).withDispatcher("raytracer-dispatcher"));
                        getContext().watch(r);
                        return new ActorRefRoutee(r);
                    })
                    .collect(toList()));

    public void onReceive(Object msg) {
        if (msg instanceof RayTracerExecutor.Render) {
            router.route(msg, getSender());
        }
    }

    public static Props props() {
        return Props.create(RayTracerCoordinator.class);
    }
}
