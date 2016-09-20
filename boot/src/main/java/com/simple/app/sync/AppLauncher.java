package com.simple.app.sync;

import com.simple.app.Launcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppLauncher {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        Launcher launcher = new Launcher(args);
        launcher.on(
                () -> null,
                () -> null,
                () -> SpringApplication.run(AppLauncher.class, args)
        );
    }

}
