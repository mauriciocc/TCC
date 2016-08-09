package com.simple.app.todo.async.service;

import akka.actor.AbstractLoggingActor;
import akka.japi.pf.ReceiveBuilder;
import com.simple.app.todo.async.akka.ActionMessage;
import com.simple.app.todo.async.domain.Todo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import rx.Observable;

import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TodoActor extends AbstractLoggingActor {

    private <T> Observable<T> handleError(Observable<T> observable, ActionMessage msg) {
        return observable.doOnError(e -> {
            e.printStackTrace();
            msg.getDeferred().setErrorResult(e);
        });
    }

    @Autowired
    public TodoActor(TodoService todoService) {
        receive(ReceiveBuilder
                .match(FindAll.class, msg ->
                        handleError(todoService.findAll(), msg)
                                .toList()
                                .subscribe(todos -> {
                                    //log().info("result collected");
                                    msg.getDeferred().setResult(todos);
                                }))
                .match(FindOne.class, msg ->
                        handleError(todoService.findOne(msg.getId()), msg)
                                .subscribe(todo -> {
                                    //log().info("result collected");
                                    msg.getDeferred().setResult(todo);
                                }))
                .match(Save.class, msg ->
                        handleError(todoService.save(msg.getTodo()), msg)
                                .subscribe(todo -> {
                                    //log().info("result collected");
                                    msg.getDeferred().setResult(todo);
                                }))
                .match(Remove.class, msg ->
                        handleError(todoService.remove(msg.getId()), msg)
                                .subscribe(removed -> {
                                    //log().info("result collected");
                                    msg.getDeferred().setResult(removed);
                                }))
                .build());
    }

    @Override
    public void preStart() throws Exception {
        log().info("starting");
    }

    @Override
    public void postStop() throws Exception {
        log().info("shutting down");
    }

    public static class FindAll extends ActionMessage<List<Todo>> {

        public FindAll(DeferredResult<List<Todo>> deferred) {
            super(deferred);
        }

    }

    public static class FindOne extends ActionMessage<Todo> {

        private final Long id;

        public FindOne(DeferredResult<Todo> deferred, Long id) {
            super(deferred);
            this.id = id;
        }

        public Long getId() {
            return id;
        }

    }

    public static class Save extends ActionMessage<Todo> {

        private final Todo todo;

        public Save(DeferredResult<Todo> deferred, Todo todo) {
            super(deferred);
            this.todo = todo;
        }

        public Todo getTodo() {
            return todo;
        }

    }

    public static class Remove extends ActionMessage<Boolean> {

        private final Long id;

        public Remove(DeferredResult<Boolean> deferred, Long id) {
            super(deferred);
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }
}
