package com.project.tasktracker.service;

import com.project.tasktracker.dto.CreateCardRequest;
import com.project.tasktracker.dto.UpdateCardRequest;
import com.project.tasktracker.error.ApiException;
import com.project.tasktracker.error.ErrorCode;
import com.project.tasktracker.model.Board;
import com.project.tasktracker.model.Card;
import com.project.tasktracker.model.CardList;
import com.project.tasktracker.model.User;
import com.project.tasktracker.repository.CardListRepository;
import com.project.tasktracker.repository.CardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CardService {

    private final CardListRepository cardListRepository;
    private final CardRepository cardRepository;

    public CardService(CardListRepository cardListRepository, CardRepository cardRepository) {
        this.cardListRepository = cardListRepository;
        this.cardRepository = cardRepository;
    }

    private CardList getVerifiedCardList(User user, Long listId) {
        CardList cardList = cardListRepository.findById(listId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.LIST_NOT_FOUND, "List not found"));

        Board board = cardList.getBoard();
        if (board == null || board.getUser() == null || !board.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, ErrorCode.LIST_NOT_FOUND, "List not found");
        }

        return cardList;
    }

    public Card createCard(User user, Long listId, CreateCardRequest request) {
        CardList cardList = getVerifiedCardList(user, listId);
        Card card = new Card();
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            card.setDescription("No description");
        } else {
            card.setDescription(request.getDescription());
        }
        card.setName(request.getName());
        card.setCardList(cardList);
        card.setCreatedAt(LocalDateTime.now());
        card.setCompleted(false);
        return cardRepository.save(card);
    }

    public List<Card> getCardsByList(User user, Long listId) {
        getVerifiedCardList(user, listId);
        return cardRepository.findByCardList_Id(listId);
    }

    public Card getCardById(User user, Long listId, Long cardId) {
        getVerifiedCardList(user, listId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.CARD_NOT_FOUND, "Card not found"));

        if (card.getCardList() == null || !card.getCardList().getId().equals(listId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, ErrorCode.CARD_NOT_FOUND, "Card not found");
        }

        return card;
    }

    public Card updateCard(User user, Long listId, Long cardId, UpdateCardRequest request) {
        Card card = getCardById(user, listId, cardId);

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

    public void deleteCard(User user, Long listId, Long cardId) {
        getCardById(user, listId, cardId);
        cardRepository.deleteById(cardId);
    }
}