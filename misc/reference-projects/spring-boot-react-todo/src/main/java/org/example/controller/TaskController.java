package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.model.entity.TaskEntity;
import org.example.model.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/tasks")
@Validated
class TaskController {
    final TaskRepository taskRepository;

    @GetMapping
    Page<TaskEntity> get(Pageable pageable) {
        try {
            Thread.sleep(2000L);
        } catch (Exception ignored) {}
        return taskRepository.findAll(pageable);
    }

    @PostMapping
    TaskEntity create(@Valid @RequestBody TaskEntity task) {
        try {
            Thread.sleep(2000L);
        } catch (Exception ignored) {}
        return taskRepository.save(task);
    }

    @GetMapping(path = "/{id}")
    TaskEntity get(@PathVariable(name = "id") UUID id) {
        try {
            Thread.sleep(2000L);
        } catch (Exception ignored) {}
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    @PutMapping(path = "/{id}")
    TaskEntity update(@PathVariable(name = "id") UUID id,
                      @RequestBody TaskEntity task) {
        try {
            Thread.sleep(2000L);
        } catch (Exception ignored) {}
        return taskRepository.save(task.setId(id));
    }

    @DeleteMapping(path = "/{id}")
    TaskEntity delete(@PathVariable(name = "id") UUID id) {
        try {
            Thread.sleep(2000L);
        } catch (Exception ignored) {}
        return taskRepository.findById(id).stream().peek(taskRepository::delete)
                .findAny()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }
}
