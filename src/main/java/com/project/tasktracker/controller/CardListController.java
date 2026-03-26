package com.project.tasktracker.controller;

import com.project.tasktracker.dto.CreateCardListRequest;
import com.project.tasktracker.model.CardList;
import com.project.tasktracker.service.CardListService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards/{boardId}/lists")
public class CardListController {

    private final CardListService cardListService;

    public CardListController(CardListService cardListService){
        this.cardListService = cardListService;
    }

    @PostMapping
    public CardList createCardList(
            @PathVariable Long boardId,
            @RequestBody CreateCardListRequest request){

        return cardListService.createCardList(boardId, request);
    }

    @GetMapping
    public List<CardList> getListsByBoard(@PathVariable Long boardId){
        return cardListService.getListsByBoard(boardId);
    }
}
