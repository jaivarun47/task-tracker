package com.project.tasktracker.service;

import com.project.tasktracker.dto.CreateCardListRequest;
import com.project.tasktracker.dto.MoveCardListRequest;
import com.project.tasktracker.error.ApiException;
import com.project.tasktracker.error.ErrorCode;
import com.project.tasktracker.model.Board;
import com.project.tasktracker.model.CardList;
import com.project.tasktracker.model.User;
import com.project.tasktracker.repository.CardListRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CardListService {

    private final BoardService boardService;
    private final CardListRepository cardListRepository;

    public CardListService(BoardService boardService, CardListRepository cardListRepository) {
        this.boardService = boardService;
        this.cardListRepository = cardListRepository;
    }

    // ── Ownership helper ───────────────────────────────────────────────────

    /** Load a CardList, verify it belongs to {@code boardId}, and that the board belongs to {@code user}. */
    private CardList getVerifiedList(User user, Long boardId, Long listId) {
        boardService.getBoardById(user, boardId); // throws NOT_FOUND if inaccessible

        CardList list = cardListRepository.findById(listId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, ErrorCode.LIST_NOT_FOUND, "List not found"));

        if (list.getBoard() == null || !list.getBoard().getId().equals(boardId)) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND, ErrorCode.LIST_NOT_FOUND, "List not found");
        }
        return list;
    }

    // ── CRUD ───────────────────────────────────────────────────────────────

    public CardList createCardList(User user, Long boardId, CreateCardListRequest request) {
        Board board = boardService.getBoardById(user, boardId);
        CardList list = new CardList();
        list.setName(request.getName());
        list.setBoard(board);
        // Append at end: position = current list count in this board
        list.setPosition(cardListRepository.countByBoard_Id(boardId));
        return cardListRepository.save(list);
    }

    public List<CardList> getListsByBoard(User user, Long boardId) {
        boardService.getBoardById(user, boardId);
        // Return lists in authoritative position order
        return cardListRepository.findByBoard_IdOrderByPositionAsc(boardId);
    }

    public CardList getListById(User user, Long boardId, Long listId) {
        return getVerifiedList(user, boardId, listId);
    }

    public CardList updateCardList(User user, Long boardId, Long listId, CreateCardListRequest request) {
        CardList list = getVerifiedList(user, boardId, listId);
        if (request.getName() != null) {
            list.setName(request.getName());
        }
        return cardListRepository.save(list);
    }

    public void deleteList(User user, Long boardId, Long listId) {
        CardList list = getVerifiedList(user, boardId, listId);
        int deletedPos = list.getPosition();

        // JPA Cascade handles deletion of cards
        cardListRepository.deleteById(listId);
        // Close the gap left by the deleted list
        cardListRepository.decrementPositionsAfter(boardId, deletedPos);
    }

    // ── Move ───────────────────────────────────────────────────────────────

    /**
     * Moves a CardList to a target Board at a target position.
     *
     * <p>Supports:
     * <ul>
     *   <li>Same-board reorder (targetBoardId == sourceBoardId)</li>
     *   <li>Cross-board move (different board owned by same user)</li>
     * </ul>
     *
     * <p>The CardList ID and all its Cards (and their IDs/data) are preserved.
     * The Cards are NOT modified — they remain attached to the moved CardList.
     *
     * @param user         authenticated user
     * @param sourceBoardId the list's current board (from URL path)
     * @param listId       list to move
     * @param request      targetBoardId + desired position
     */
    public CardList moveCardList(User user, Long sourceBoardId, Long listId, MoveCardListRequest request) {
        if (request.position() < 0) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST, ErrorCode.INVALID_POSITION,
                    "Position must not be negative");
        }

        // 1. Verify source ownership and that list belongs to sourceBoard
        CardList list = getVerifiedList(user, sourceBoardId, listId);
        Long targetId = request.targetBoardId() != null ? request.targetBoardId() : sourceBoardId;
        // 2. Verify target board ownership
        Board targetBoard = boardService.getBoardById(user, targetId);

        Long targetBoardId = targetBoard.getId();
        int oldPosition    = list.getPosition();
        Long capturedListId = list.getId();

        boolean sameBoard = sourceBoardId.equals(targetBoardId);

        if (sameBoard) {
            // ── Same-board reorder ─────────────────────────────────────────
            int siblingCount = cardListRepository.countByBoard_Id(sourceBoardId);
            int newPosition  = Math.min(request.position(), siblingCount - 1);

            if (newPosition == oldPosition) {
                return list; // No-op
            }

            if (newPosition > oldPosition) {
                cardListRepository.shiftDown(sourceBoardId, oldPosition, newPosition);
            } else {
                cardListRepository.shiftUp(sourceBoardId, newPosition, oldPosition);
            }

            CardList freshList = cardListRepository.findById(capturedListId)
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.NOT_FOUND, ErrorCode.LIST_NOT_FOUND, "List not found"));
            freshList.setPosition(newPosition);
            return cardListRepository.save(freshList);

        } else {
            // ── Cross-board move ───────────────────────────────────────────
            int destCount  = cardListRepository.countByBoard_Id(targetBoardId);
            int newPosition = Math.min(request.position(), destCount);

            // Close source gap
            cardListRepository.decrementPositionsAfter(sourceBoardId, oldPosition);
            // Make room in destination
            cardListRepository.incrementPositionsFrom(targetBoardId, newPosition);

            // Re-load after context was cleared by bulk updates
            CardList freshList = cardListRepository.findById(capturedListId)
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.NOT_FOUND, ErrorCode.LIST_NOT_FOUND, "List not found"));

            // NOTE: We mutate the child's board reference — we do NOT remove the list
            // from the old board's collection, which would trigger orphanRemoval.
            freshList.setBoard(targetBoard);
            freshList.setPosition(newPosition);
            return cardListRepository.save(freshList);
        }
    }
}
