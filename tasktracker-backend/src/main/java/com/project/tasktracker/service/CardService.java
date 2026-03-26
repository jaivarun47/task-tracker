package com.project.tasktracker.service;

import com.project.tasktracker.dto.CreateCardRequest;
import com.project.tasktracker.dto.UpdateCardRequest;
import com.project.tasktracker.error.ApiException;
import com.project.tasktracker.error.ErrorCode;
import com.project.tasktracker.model.Card;
import com.project.tasktracker.repository.CardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CardService {

    private final CardListService cardListService;
    private final CardRepository cardRepository;

    public CardService(CardListService cardListService, CardRepository cardRepository) {
        this.cardListService = cardListService;
        this.cardRepository = cardRepository;
    }

    public Card createCard(Long listId, CreateCardRequest request) {
        cardListService.getListById(listId);
        Card card = new Card();
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            card.setDescription("No description");
        } else {
            card.setDescription(request.getDescription());
        }
        card.setName(request.getName());
        card.setListId(listId);
        card.setCreatedAt(LocalDateTime.now());
        card.setCompleted(false);
        return cardRepository.save(card);
    }

    public List<Card> getCardsByList(Long listId) {
        cardListService.getListById(listId);
        return cardRepository.findByListId(listId);
    }

    public Card getCardById(Long listId, Long cardId) {
        cardListService.getListById(listId);
        Card card = cardRepository
                .findById(cardId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.CARD_NOT_FOUND, "Card not found"));

        if (card.getListId() == null || !card.getListId().equals(listId)) {
            // Avoid leaking resource relationships; treat as "not found".
            throw new ApiException(HttpStatus.NOT_FOUND, ErrorCode.CARD_NOT_FOUND, "Card not found");
        }

        return card;
    }

    public Card updateCard(Long listId, Long cardId, UpdateCardRequest request) {
        Card card = getCardById(listId, cardId);

        if (request.getName() != null) {
            card.setName(request.getName());
        }

        if (request.getDescription() != null) {
            if (request.getDescription().isBlank()) {
                card.setDescription("No description");
            } else {
                card.setDescription(request.getDescription());
            }
        }

        if (request.getCompleted() != null) {
            card.setCompleted(request.getCompleted());
        }

        return cardRepository.save(card);
    }

    public void deleteCard(Long listId, Long cardId) {
        // Ensure consistent 404 behavior across all card operations.
        getCardById(listId, cardId);
        cardRepository.deleteById(cardId);
    }
}