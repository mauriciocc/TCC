package com.simple.app;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.util.concurrent.Callable;

public class Launcher {

    private final OptionSet options;

    public Launcher(String... args) {
        OptionParser optionParser = new OptionParser("c::a::");
        options = optionParser.parse(args);
    }

    public <T> T on(Callable<T> rayTracerApp, Callable<T> reportApp, Callable<T> defaultApp) {
        try {
            final String desiredApp = options.has("a") ? String.valueOf(options.valueOf("a")) : "";
            switch (desiredApp) {
                case "raytracer":
                    return rayTracerApp.call();
                case "report":
                    return reportApp.call();
                default:
                    return defaultApp.call();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String config() {
        return hasConfig() ? String.valueOf(options.valueOf("c")) : "";
    }

    public boolean hasConfig() {
        return options.has("c");
    }

    public String configOr(String defVal) {
        return hasConfig() ? config() : defVal;
    }
}
