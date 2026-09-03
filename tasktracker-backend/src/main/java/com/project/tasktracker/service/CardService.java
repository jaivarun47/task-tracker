package com.project.tasktracker.service;

import com.project.tasktracker.dto.CreateCardRequest;
import com.project.tasktracker.dto.MoveCardRequest;
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

    // ── Ownership helpers ──────────────────────────────────────────────────

    /** Load a CardList and verify it belongs to {@code user}. */
    private CardList getVerifiedCardList(User user, Long listId) {
        CardList cardList = cardListRepository.findById(listId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, ErrorCode.LIST_NOT_FOUND, "List not found"));

        Board board = cardList.getBoard();
        if (board == null || board.getUser() == null
                || !board.getUser().getId().equals(user.getId())) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND, ErrorCode.LIST_NOT_FOUND, "List not found");
        }
        return cardList;
    }

    /** Load a Card by ID and verify it currently belongs to {@code listId}. */
    private Card getVerifiedCard(User user, Long listId, Long cardId) {
        // Verifying the list also confirms user ownership of the list.
        getVerifiedCardList(user, listId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, ErrorCode.CARD_NOT_FOUND, "Card not found"));

        if (card.getCardList() == null || !card.getCardList().getId().equals(listId)) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND, ErrorCode.CARD_NOT_FOUND, "Card not found");
        }
        return card;
    }

    // ── CRUD ───────────────────────────────────────────────────────────────

    public Card createCard(User user, Long listId, CreateCardRequest request) {
        CardList cardList = getVerifiedCardList(user, listId);

        Card card = new Card();
        card.setName(request.getName());
        card.setDescription(
                (request.getDescription() == null || request.getDescription().isBlank())
                        ? "No description"
                        : request.getDescription()
        );
        card.setCardList(cardList);
        card.setCreatedAt(LocalDateTime.now());
        card.setCompleted(false);
        // Append at end: position = current card count
        card.setPosition(cardRepository.countByCardList_Id(listId));
        return cardRepository.save(card);
    }

    public List<Card> getCardsByList(User user, Long listId) {
        getVerifiedCardList(user, listId);
        // Return cards in authoritative position order
        return cardRepository.findByCardList_IdOrderByPositionAsc(listId);
    }

    public Card getCardById(User user, Long listId, Long cardId) {
        return getVerifiedCard(user, listId, cardId);
    }

    public Card updateCard(User user, Long listId, Long cardId, UpdateCardRequest request) {
        Card card = getVerifiedCard(user, listId, cardId);

        if (request.getName() != null) {
            card.setName(request.getName());
        }
        if (request.getDescription() != null) {
            card.setDescription(
                    request.getDescription().isBlank() ? "No description" : request.getDescription()
            );
        }
        if (request.getCompleted() != null) {
            card.setCompleted(request.getCompleted());
        }
        return cardRepository.save(card);
    }

    public void deleteCard(User user, Long listId, Long cardId) {
        Card card = getVerifiedCard(user, listId, cardId);
        int deletedPos = card.getPosition();

        cardRepository.deleteById(cardId);
        // Close the gap left by the deleted card
        cardRepository.decrementPositionsAfter(listId, deletedPos);
    }

    // ── Move ───────────────────────────────────────────────────────────────

    /**
     * Moves a Card to a target CardList at a target position.
     *
     * <p>Supports:
     * <ul>
     *   <li>Same-list reorder (targetListId == sourceListId)</li>
     *   <li>Cross-list move (different list, possibly different board)</li>
     * </ul>
     *
     * <p>The Card ID and all non-positional fields are preserved.
     * The entire operation is atomic within a single transaction.
     *
     * @param user         authenticated user
     * @param sourceListId the card's current list (from URL path)
     * @param cardId       card to move
     * @param request      targetListId + desired position
     */
    public Card moveCard(User user, Long sourceListId, Long cardId, MoveCardRequest request) {
        if (request.position() < 0) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST, ErrorCode.INVALID_POSITION,
                    "Position must not be negative");
        }

        // 1. Verify source ownership and that card belongs to sourceList
        Card card = getVerifiedCard(user, sourceListId, cardId);
        Long targetId = request.targetListId() != null ? request.targetListId() : sourceListId;
        // 2. Verify target list ownership
        CardList targetList = getVerifiedCardList(user, targetId);

        Long targetListId = targetList.getId();
        int oldPosition   = card.getPosition();
        // Capture card ID before context is cleared by bulk updates
        Long capturedCardId = card.getId();

        boolean sameList = sourceListId.equals(targetListId);

        if (sameList) {
            // ── Same-list reorder ──────────────────────────────────────────
            int siblingCount = cardRepository.countByCardList_Id(sourceListId);
            // Clamp: maximum valid target when the item is still in the list is siblingCount-1
            int newPosition = Math.min(request.position(), siblingCount - 1);

            if (newPosition == oldPosition) {
                return card; // No-op
            }

            if (newPosition > oldPosition) {
                // Moving forward: shift items in (oldPosition, newPosition] down
                cardRepository.shiftDown(sourceListId, oldPosition, newPosition);
            } else {
                // Moving backward: shift items in [newPosition, oldPosition) up
                cardRepository.shiftUp(sourceListId, newPosition, oldPosition);
            }

            // Re-load after context was cleared by bulk update
            Card freshCard = cardRepository.findById(capturedCardId)
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.NOT_FOUND, ErrorCode.CARD_NOT_FOUND, "Card not found"));
            freshCard.setPosition(newPosition);
            return cardRepository.save(freshCard);

        } else {
            // ── Cross-list move ────────────────────────────────────────────
            int destCount  = cardRepository.countByCardList_Id(targetListId);
            // Clamp: maximum valid insertion position is destCount (append at end)
            int newPosition = Math.min(request.position(), destCount);

            // 5. Close source gap: decrement all source siblings after oldPosition
            cardRepository.decrementPositionsAfter(sourceListId, oldPosition);
            // 6. Make room in destination: increment all dest siblings >= newPosition
            cardRepository.incrementPositionsFrom(targetListId, newPosition);

            // 7. Re-load after context was cleared by bulk updates
            Card freshCard = cardRepository.findById(capturedCardId)
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.NOT_FOUND, ErrorCode.CARD_NOT_FOUND, "Card not found"));

            // 8. Update parent relationship and position
            //    NOTE: We call setCardList() on the child entity — we do NOT remove the
            //    card from the old list's collection, which would trigger orphanRemoval.
            freshCard.setCardList(targetList);
            freshCard.setPosition(newPosition);
            return cardRepository.save(freshCard);
        }
    }
}