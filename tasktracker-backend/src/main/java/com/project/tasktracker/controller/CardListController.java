package com.project.tasktracker.controller;

import com.project.tasktracker.dto.CreateCardListRequest;
import com.project.tasktracker.model.CardList;
import com.project.tasktracker.service.CardListService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@RestController
@RequestMapping("/api/boards/{boardId}/lists")
@CrossOrigin(origins = "http://localhost:5173")
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

    @GetMapping("/{listId}")
    public CardList getListById(
            @PathVariable Long boardId,
            @PathVariable Long listId
    ) {
        return cardListService.getListById(boardId, listId);
    }

    @PutMapping("/{listId}")
    public CardList updateCardList(
            @PathVariable Long boardId,
            @PathVariable Long listId,
            @RequestBody CreateCardListRequest request
    ) {
        return cardListService.updateCardList(boardId, listId, request);
    }

    @DeleteMapping("/{listId}")
    public void deleteCardList(
            @PathVariable Long boardId,
            @PathVariable Long listId
    ) {
        cardListService.deleteList(boardId, listId);
    }
}
