package com.project.tasktracker.service;

import com.project.tasktracker.dto.CreateCardRequest;
import com.project.tasktracker.model.Card;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CardService {

    private final Map<Long, Card> cards = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();
    private final CardListService cardListService;

    public CardService(CardListService cardListService) {
        this.cardListService = cardListService;
    }

    public Card createCard(Long listId, CreateCardRequest request) {
        cardListService.getListById(listId);
        Card card = new Card();
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            card.setDescription("No description");
        } else {
            card.setDescription(request.getDescription());
        }
        card.setId(idGenerator.incrementAndGet());
        card.setName(request.getName());
        card.setListId(listId);
        card.setCreatedAt(LocalDateTime.now());
        cards.put(card.getId(), card);
        card.setCompleted(false);
        return card;
    }

    public List<Card> getCardsByList(Long listId) {
        cardListService.getListById(listId);

        List<Card> result = new ArrayList<>();

        for (Card card : cards.values()) {
            if (Objects.equals(card.getListId(), listId)) {
                result.add(card);
            }
        }

        return result;
    }
}