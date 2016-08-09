package com.simple.app.todo.async.web;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import com.simple.app.todo.async.akka.ActorCreator;
import com.simple.app.todo.async.domain.Todo;
import com.simple.app.todo.async.service.TodoActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.function.Function;

@RestController
@RequestMapping("/api/todos")
public class TodoResource {

    private final ActorRef actor;

    @Autowired
    public TodoResource(ActorCreator actorCreator) {
        this.actor = actorCreator.actorRef(TodoActor.class);
    }

    private <T> DeferredResult<T> process(Function<DeferredResult<T>, Object> fn) {
        DeferredResult<T> result = new DeferredResult<>(5000L);
        actor.tell(fn.apply(result), null);
        return result;
    }

    @RequestMapping(method = RequestMethod.GET)
    public DeferredResult<List<Todo>> findAll() {
        return process(TodoActor.FindAll::new);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public DeferredResult<Todo> findOne(@PathVariable("id") Long id) {
        return process(def -> new TodoActor.FindOne(def, id));
    }

    @RequestMapping(method = RequestMethod.POST)
    public DeferredResult<Todo> save(@RequestBody Todo todo) {
        return process(def -> new TodoActor.Save(def, todo));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public DeferredResult<Todo> update(@PathVariable("id") Long id, @RequestBody Todo todo) {
        return process(def -> new TodoActor.Save(def, todo));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public DeferredResult<Boolean> remove(@PathVariable("id") Long id) {
        return process(def -> new TodoActor.Remove(def, id));
    }

}
