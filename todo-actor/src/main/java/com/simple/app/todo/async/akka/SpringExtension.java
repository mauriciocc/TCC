package com.simple.app.todo.async.akka;

import akka.actor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
class SpringExtension extends AbstractExtensionId<SpringExtension> implements Extension, ActorCreator {

    private final ApplicationContext applicationContext;
    private final ActorSystem actorSystem;

    @Autowired
    public SpringExtension(ApplicationContext applicationContext, ActorSystem actorSystem) {
        this.applicationContext = applicationContext;
        this.actorSystem = actorSystem;
    }


    @Override
    public Props props(Class<?> clazz, Object... args) {
        Object[] params = new Object[args.length + 2];
        params[0] = applicationContext;
        params[1] = clazz;
        System.arraycopy(args, 0, params, 2, args.length);
        return Props.create(SpringActorProducer.class, params);
    }

    @Override
    public ActorRef actorRef(Class<?> clazz, Object... args) {
        return actorSystem.actorOf(props(clazz, args));
    }

    @Override
    public SpringExtension createExtension(ExtendedActorSystem system) {
        return null;
    }
}