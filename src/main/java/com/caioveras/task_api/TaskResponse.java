package com.caioveras.task_api;

public record TaskResponse(Long id, String titulo, String descricao, boolean concluido) {

    public static TaskResponse fromEntity(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitulo(),
                task.getDescricao(),
                task.isConcluido()
        );
    }
}
