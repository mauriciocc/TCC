package com.simple.app.sync.web;

import com.simple.app.raytracer.domain.RayTracer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/render")
public class RayTracerResource {

    private final RayTracer rayTracer;

    public RayTracerResource() {
        rayTracer = new RayTracer();
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] render() {
        return rayTracer.renderToPng(RayTracer.SIZE, RayTracer.SIZE, RayTracer.THREADS);
    }

}