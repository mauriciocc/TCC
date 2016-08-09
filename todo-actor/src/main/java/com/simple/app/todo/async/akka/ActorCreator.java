package com.simple.app.todo.async.akka;

import akka.actor.ActorRef;
import akka.actor.Extension;
import akka.actor.Props;

public interface ActorCreator extends Extension {

    Props props(Class<?> clazz, Object... args);

    ActorRef actorRef(Class<?> clazz, Object... args);
}
