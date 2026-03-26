package com.project.tasktracker.repository;

import com.project.tasktracker.model.CardList;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardListRepository extends JpaRepository<CardList, Long> {
    List<CardList> findByBoardId(Long boardId);
}