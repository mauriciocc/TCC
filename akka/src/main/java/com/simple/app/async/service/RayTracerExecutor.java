package com.simple.app.async.service;

import akka.actor.UntypedActor;
import com.simple.app.raytracer.domain.RayTracer;

public class RayTracerExecutor extends UntypedActor {

    private static final RayTracer rayTracer = new RayTracer();

    @Override
    public void onReceive(Object msg) throws Throwable {
        if (msg instanceof Render) {

            System.out.println(getContext().dispatcher());
            getSender().tell(
                    rayTracer.renderToPng(RayTracer.SIZE, RayTracer.SIZE, RayTracer.THREADS),
                    getContext().parent()
            );
        }
    }

    public static class Render {

    }
}
