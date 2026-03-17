package com.project.tasktracker.controller;

import com.project.tasktracker.dto.CreateCardRequest;
import com.project.tasktracker.model.Card;
import com.project.tasktracker.model.CardList;
import com.project.tasktracker.service.CardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lists/{listId}/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public Card createCard(
            @PathVariable Long listId,
            @RequestBody CreateCardRequest request) {

        return cardService.createCard(listId, request);
    }

    @GetMapping
    public List<Card> getCardsByList(@PathVariable Long listId) {
        return cardService.getCardsByList(listId);
    }

}