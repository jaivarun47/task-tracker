package com.project.tasktracker.repository;

import com.project.tasktracker.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByCardList_Id(Long listId);

    void deleteByCardList_Id(Long listId);
}