package com.simple.app.todo.async.akka;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;
import org.springframework.context.ApplicationContext;

class SpringActorProducer implements IndirectActorProducer {

    private final ApplicationContext applicationContext;
    private final Object bean;

    public SpringActorProducer(ApplicationContext applicationContext,
                               Object bean) {
        this.applicationContext = applicationContext;
        this.bean = bean;
    }

    @Override
    public Actor produce() {
        if (bean instanceof String) {
            return (Actor) applicationContext.getBean((String) bean);
        } else {
            return (Actor) applicationContext.getBean((Class) bean);
        }
    }

    @Override
    public Class<? extends Actor> actorClass() {
        if (bean instanceof Class) {
            return (Class<? extends Actor>) bean;
        } else {
            return (Class<? extends Actor>) applicationContext.getType((String) bean);
        }
    }
}
