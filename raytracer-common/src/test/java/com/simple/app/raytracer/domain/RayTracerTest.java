package com.simple.app.raytracer.domain;

import org.junit.Before;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.junit.Assert.*;

public class RayTracerTest {

    private RayTracer rayTracer;

    @Before
    public void setUp() throws Exception {
        rayTracer = new RayTracer();
    }
    @org.junit.Test
    public void renderToPng() throws Exception {
        Path path = Paths.get("out.png");
        Files.write(path, rayTracer.renderToPng(256, 256, 1));
    }

}