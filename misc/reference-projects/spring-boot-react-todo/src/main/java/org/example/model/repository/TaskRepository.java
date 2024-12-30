package org.example.model.repository;

import org.example.model.entity.TaskEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface TaskRepository extends PagingAndSortingRepository<TaskEntity, UUID>,
        ListCrudRepository<TaskEntity, UUID> {
}
