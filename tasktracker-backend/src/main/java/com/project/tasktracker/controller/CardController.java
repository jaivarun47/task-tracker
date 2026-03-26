package com.project.tasktracker.controller;

import com.project.tasktracker.dto.CreateCardRequest;
import com.project.tasktracker.dto.UpdateCardRequest;
import com.project.tasktracker.model.Card;
import com.project.tasktracker.service.CardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lists/{listId}/cards")
@CrossOrigin(origins = "http://localhost:5173")
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

    @GetMapping("/{cardId}")
    public Card getCardById(
            @PathVariable Long listId,
            @PathVariable Long cardId
    ) {
        return cardService.getCardById(listId, cardId);
    }

    @PutMapping("/{cardId}")
    public Card updateCard(
            @PathVariable Long listId,
            @PathVariable Long cardId,
            @RequestBody UpdateCardRequest request
    ) {
        return cardService.updateCard(listId, cardId, request);
    }

    @DeleteMapping("/{cardId}")
    public void deleteCard(
            @PathVariable Long listId,
            @PathVariable Long cardId
    ) {
        cardService.deleteCard(listId, cardId);
    }
}