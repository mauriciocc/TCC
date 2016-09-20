package com.simple.app.sync.web;

import com.simple.app.sync.domain.Todo;
import com.simple.app.sync.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class TodoResource {

    private final TodoService service;

    @Autowired
    public TodoResource(TodoService service) {
        this.service = service;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Todo> findAll() {
        return service.findAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Todo findOne(@PathVariable("id") Long id) {
        return service.findOne(id);
    }

    @RequestMapping(method = RequestMethod.POST)
    public Todo save(@RequestBody Todo todo) {
        return service.save(todo);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Todo update(@PathVariable("id") Long id, @RequestBody Todo todo) {
        return service.save(todo);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public boolean remove(@PathVariable("id") Long id) {
        return service.remove(id);
    }

}
