package com.simple.app.async.service;

import com.github.pgasync.Db;
import com.github.pgasync.Row;
import com.simple.app.async.domain.Todo;
import rx.Observable;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;

public class TodoService {

    private static final Function<Row, Todo> ROW_MAPPER = row -> new Todo(
            row.getLong("id"),
            row.getString("owner"),
            row.getString("title"),
            row.getString("description"),
            row.getTimestamp("created_on").toLocalDateTime()
    );


    private static final Function<Todo, List<Object>> ROW_UNMAPPER = todo -> asList(
            todo.getOwner(),
            todo.getTitle(),
            todo.getDescription(),
            Timestamp.valueOf(todo.getCreatedOn()),
            todo.getId()
    );


    private final Db db;


    public TodoService(Db db) {
        this.db = db;
    }

    public Observable<Todo> findAll() {
        return db.queryRows("SELECT * FROM todo LIMIT 10").map(ROW_MAPPER::apply);
    }


    public Observable<Todo> findOne(Long id) {
        return db.queryRows("SELECT * FROM todo where id = $1", id).map(ROW_MAPPER::apply);
    }

    public Observable<Todo> save(Todo todo) {
        Object[] params = ROW_UNMAPPER.apply(todo).toArray();

        final String sql = todo.getId() == null
                ? "INSERT INTO todo (owner, title, description, created_on) VALUES ($1, $2, $3, $4) RETURNING *"
                : "UPDATE todo SET (owner, title, description, created_on) = ($1,$2,$3,$4) WHERE id = $5 RETURNING *";
        params = todo.getId() == null ? Arrays.copyOfRange(params, 0, 4) : params;
        return db.queryRows(sql, params).map(ROW_MAPPER::apply);
    }

    public Observable<Boolean> remove(Long id) {
        return db.querySet("DELETE FROM todo WHERE id = $1", id).map(set -> set.updatedRows() > 0);
    }

}
