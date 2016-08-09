package com.simple.app.todo.async.akka;

import com.simple.app.todo.async.domain.Todo;
import org.springframework.web.context.request.async.DeferredResult;

public abstract class ActionMessage<T> {

    protected final DeferredResult<T> deferred;

    public ActionMessage(DeferredResult<T> deferred) {
        this.deferred = deferred;
    }

    public DeferredResult<T> getDeferred() {
        return deferred;
    }

}
