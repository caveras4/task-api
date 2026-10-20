package com.caioveras.task_api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskRepository repository;

    public TaskController(TaskRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> listTasks(@RequestParam(name = "concluido", required = false) Boolean completed, @RequestParam(name = "ordenarPor", required = false) String sortBy) {
        Stream<Task> taskStream = repository.findAll().stream();

        if (completed != null) {
            taskStream = taskStream.filter(t -> t.isConcluido() == completed);

        }

        if (sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "id":
                    taskStream = taskStream.sorted((taskA, taskB) -> (Long.compare(taskA.getId(), taskB.getId())));
                    break;

                default:
                    return ResponseEntity.badRequest().build();
            }
        }

        return ResponseEntity.ok(taskStream.map(TaskResponse::fromEntity).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        return repository.findById(id)
                .map(TaskResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody @Valid TaskRequest request) {
        var savedTask = repository.save(request.toEntity());

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()   // pega a URL atual: /tasks
                .path("/{id}")          // acrescenta /{id}
                .buildAndExpand(savedTask.getId())  // troca {id} pelo id real
                .toUri();

        // Converte a entidade salva para o DTO de resposta
        TaskResponse response = TaskResponse.fromEntity(savedTask);

        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, @RequestBody @Valid TaskRequest request) {
        return repository.findById(id)
                .map(existingTask -> {
                    existingTask.setTitulo(request.titulo());
                    existingTask.setDescricao(request.descricao());
                    existingTask.setConcluido(request.concluido());
                    var savedTask = repository.save(existingTask);

                    TaskResponse response = TaskResponse.fromEntity(savedTask);

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());// Status 404
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();// Status 404
        }

        repository.deleteById(id);
        return ResponseEntity.noContent().build();// Status 204 No Content (Sucesso sem corpo de resposta)
    }

}
