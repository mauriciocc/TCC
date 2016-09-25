package com.simple.app.sync;

import com.simple.app.Launcher;
import com.simple.app.sync.raytracer.RayTracerApp;
import com.simple.app.sync.report.ReportApp;
import com.simple.app.sync.todo.TodoApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

public class AppLauncher {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        Launcher launcher = new Launcher(args);
        launcher.on(
                () -> SpringApplication.run(RayTracerApp.class, args),
                () -> SpringApplication.run(ReportApp.class, args),
                () -> SpringApplication.run(TodoApp.class, args)
        );
    }

}
