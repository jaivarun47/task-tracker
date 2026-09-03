package com.project.tasktracker.controller;

import com.project.tasktracker.dto.CardDto;
import com.project.tasktracker.dto.CreateCardRequest;
import com.project.tasktracker.dto.MoveCardRequest;
import com.project.tasktracker.dto.UpdateCardRequest;
import com.project.tasktracker.mapper.ModelMapper;
import com.project.tasktracker.model.User;
import com.project.tasktracker.security.CurrentUser;
import com.project.tasktracker.service.CardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lists/{listId}/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public CardDto createCard(
            @CurrentUser User user,
            @PathVariable Long listId,
            @RequestBody CreateCardRequest request
    ) {
        return ModelMapper.toCardDto(cardService.createCard(user, listId, request));
    }

    @GetMapping
    public List<CardDto> getCardsByList(
            @CurrentUser User user,
            @PathVariable Long listId
    ) {
        return cardService.getCardsByList(user, listId).stream()
                .map(ModelMapper::toCardDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{cardId}")
    public CardDto getCardById(
            @CurrentUser User user,
            @PathVariable Long listId,
            @PathVariable Long cardId
    ) {
        return ModelMapper.toCardDto(cardService.getCardById(user, listId, cardId));
    }

    @PutMapping("/{cardId}")
    public CardDto updateCard(
            @CurrentUser User user,
            @PathVariable Long listId,
            @PathVariable Long cardId,
            @RequestBody UpdateCardRequest request
    ) {
        return ModelMapper.toCardDto(cardService.updateCard(user, listId, cardId, request));
    }

    @DeleteMapping("/{cardId}")
    public void deleteCard(
            @CurrentUser User user,
            @PathVariable Long listId,
            @PathVariable Long cardId
    ) {
        cardService.deleteCard(user, listId, cardId);
    }

    /**
     * Move a card to a (possibly different) list at a target position.
     *
     * <p>POST body: {@code { "targetListId": 123, "position": 0 }}
     *
     * <p>{@code listId} in the path is the card's CURRENT (source) list.
     * After a cross-list move the card will no longer belong to {@code listId}.
     */
    @PatchMapping("/{cardId}/move")
    public CardDto moveCard(
            @CurrentUser User user,
            @PathVariable Long listId,
            @PathVariable Long cardId,
            @RequestBody MoveCardRequest request
    ) {
        return ModelMapper.toCardDto(cardService.moveCard(user, listId, cardId, request));
    }
}