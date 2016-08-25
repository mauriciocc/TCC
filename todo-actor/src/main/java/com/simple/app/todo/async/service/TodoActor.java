package com.simple.app.todo.async.service;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.simple.app.todo.async.domain.Todo;

public class TodoActor extends UntypedActor {

    private final TodoService todoService;

    public TodoActor(TodoService todoService) {
        this.todoService = todoService;
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        final ActorRef sender = sender();

        if (message instanceof FindAll) {
            todoService.findAll()
                    .toList()
                    .subscribe(todos -> sender.tell(todos, self()));
        }

        if (message instanceof FindOne) {
            todoService.findOne(((FindOne) message).getId())
                    .subscribe(todos -> sender.tell(todos, self()));
        }

        if (message instanceof Save) {
            todoService.save(((Save) message).getTodo())
                    .subscribe(todo -> sender.tell(todo, self()));
        }
        if (message instanceof Remove) {
            todoService.remove(((Remove) message).getId())
                    .subscribe(todo -> sender.tell(todo, self()));
        }
    }

    public static Props props(TodoService todoService) {
        return Props.create(TodoActor.class, todoService);
    }

    public static class FindAll {

        public FindAll() {
        }

    }

    public static class FindOne {

        private final Long id;

        public FindOne(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }

    }

    public static class Save {

        private final Todo todo;

        public Save(Todo todo) {
            this.todo = todo;
        }

        public Todo getTodo() {
            return todo;
        }

    }

    public static class Remove {

        private final Long id;

        public Remove(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }
}
