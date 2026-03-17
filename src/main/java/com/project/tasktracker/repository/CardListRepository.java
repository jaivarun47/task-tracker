package com.project.tasktracker.repository;

import com.project.tasktracker.model.CardList;
import java.util.List;
import java.util.Optional;

public interface CardListRepository {

    CardList save(CardList list);

    Optional<CardList> findById(Long id);

    List<CardList> findAll();

    void deleteById(Long id);

    List<CardList> findByBoardId(Long boardId);
}