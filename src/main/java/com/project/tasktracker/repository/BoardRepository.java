package com.project.tasktracker.repository;

import com.project.tasktracker.model.Board;
import java.util.List;
import java.util.Optional;

public interface BoardRepository {

    Board save(Board board);

    Optional<Board> findById(Long id);

    List<Board> findAll();

    void deleteById(Long id);
}