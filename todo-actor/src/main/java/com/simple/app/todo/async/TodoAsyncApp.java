package com.simple.app.todo.async;

import akka.actor.ActorSystem;
import com.github.pgasync.ConnectionPoolBuilder;
import com.github.pgasync.Db;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.net.URISyntaxException;

@SpringBootApplication
public class TodoAsyncApp {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Bean
    public Config akkaConfiguration() {
        return ConfigFactory.load();
    }

    @Bean
    public ActorSystem actorSystem(Config config) {
        return ActorSystem.create("Akka-TodoAsyncApp", config);
    }

    @Bean
    public Db db() throws URISyntaxException {
        return new ConnectionPoolBuilder()
                .hostname("localhost")
                .port(5432)
                .database("todo_app")
                .username("postgres")
                .password("postgres")
                .poolSize(8)
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(TodoAsyncApp.class, args);
    }

}