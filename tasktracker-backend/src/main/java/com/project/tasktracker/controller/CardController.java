package com.project.tasktracker.controller;

import com.project.tasktracker.dto.CardDto;
import com.project.tasktracker.dto.CreateCardRequest;
import com.project.tasktracker.dto.UpdateCardRequest;
import com.project.tasktracker.mapper.ModelMapper;
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
            @PathVariable Long listId,
            @RequestBody CreateCardRequest request) {

        return ModelMapper.toCardDto(cardService.createCard(listId, request));
    }

    @GetMapping
    public List<CardDto> getCardsByList(@PathVariable Long listId) {
        return cardService.getCardsByList(listId).stream()
                .map(ModelMapper::toCardDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{cardId}")
    public CardDto getCardById(
            @PathVariable Long listId,
            @PathVariable Long cardId
    ) {
        return ModelMapper.toCardDto(cardService.getCardById(listId, cardId));
    }

    @PutMapping("/{cardId}")
    public CardDto updateCard(
            @PathVariable Long listId,
            @PathVariable Long cardId,
            @RequestBody UpdateCardRequest request
    ) {
        return ModelMapper.toCardDto(cardService.updateCard(listId, cardId, request));
    }

    @DeleteMapping("/{cardId}")
    public void deleteCard(
            @PathVariable Long listId,
            @PathVariable Long cardId
    ) {
        cardService.deleteCard(listId, cardId);
    }
}