package com.simple.app.sync.service;

import com.simple.app.sync.domain.Todo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class TodoService {

    private final NamedParameterJdbcTemplate jdbc;
    private final RowMapper<Todo> ROW_MAPPER = (rs, rowNum) -> new Todo(
            rs.getLong("id"),
            rs.getString("owner"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getTimestamp("created_on").toLocalDateTime()
    );
    private final Function<Todo, Map<String, Object>> ROW_UNMAPPER = todo -> {
        Map<String, Object> props = new HashMap<>(5);
        props.put("id", todo.getId());
        props.put("owner", todo.getOwner());
        props.put("title", todo.getTitle());
        props.put("description", todo.getDescription());
        props.put("createdOn", Timestamp.valueOf(todo.getCreatedOn()));
        return props;
    };


    @Autowired
    public TodoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public List<Todo> findAll() {
        return jdbc.query("SELECT * FROM todo LIMIT 10", ROW_MAPPER);
    }


    @Transactional(readOnly = true)
    public Todo findOne(Long id) {
        return jdbc.queryForObject("SELECT * FROM todo WHERE id = :id", Collections.singletonMap("id", id), ROW_MAPPER);
    }

    @Transactional
    public Todo save(Todo todo) {
        Map<String, Object> props = ROW_UNMAPPER.apply(todo);
        if (todo.getId() == null) {
            return jdbc.queryForObject("INSERT INTO todo (owner, title, description, created_on) VALUES (:owner, :title, :description, :createdOn) RETURNING *", props, ROW_MAPPER);
        } else {
            jdbc.update("UPDATE todo SET (owner, title, description, created_on) = (:owner, :title, :description, :createdOn) WHERE id = :id", props);
            return todo;
        }
    }

    @Transactional
    public boolean remove(Long id) {
        return jdbc.update("DELETE FROM todo WHERE id = :id", Collections.singletonMap("id", id)) > 0;
    }

}
