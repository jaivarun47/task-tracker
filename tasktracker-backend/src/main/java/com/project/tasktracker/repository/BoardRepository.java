package com.project.tasktracker.repository;

import com.project.tasktracker.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByUser_Id(UUID userId);
}