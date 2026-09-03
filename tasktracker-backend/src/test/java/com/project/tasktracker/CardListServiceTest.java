package com.project.tasktracker;

import com.project.tasktracker.dto.CreateCardListRequest;
import com.project.tasktracker.dto.MoveCardListRequest;
import com.project.tasktracker.error.ApiException;
import com.project.tasktracker.error.ErrorCode;
import com.project.tasktracker.model.Board;
import com.project.tasktracker.model.CardList;
import com.project.tasktracker.model.User;
import com.project.tasktracker.repository.BoardRepository;
import com.project.tasktracker.repository.CardListRepository;
import com.project.tasktracker.service.BoardService;
import com.project.tasktracker.service.CardListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Mockito-based unit tests for {@link CardListService} business logic.
 */
@ExtendWith(MockitoExtension.class)
class CardListServiceTest {

    @Mock CardListRepository cardListRepository;
    @Mock BoardRepository boardRepository;
    @Mock BoardService boardService;

    CardListService cardListService;

    User user;
    Board board;
    Board board2;

    @BeforeEach
    void setUp() {
        cardListService = new CardListService(boardService, cardListRepository);

        user = new User();
        user.setId(UUID.randomUUID());

        board = new Board();
        board.setId(1L);
        board.setUser(user);

        board2 = new Board();
        board2.setId(2L);
        board2.setUser(user);
    }

    // ── Create ─────────────────────────────────────────────────────────────

    @Test
    void createCardList_appendsAtCount() {
        when(boardService.getBoardById(user, 1L)).thenReturn(board);
        when(cardListRepository.countByBoard_Id(1L)).thenReturn(2);
        when(cardListRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateCardListRequest req = new CreateCardListRequest();
        req.setName("New List");

        CardList result = cardListService.createCardList(user, 1L, req);

        assertThat(result.getPosition()).isEqualTo(2);
    }

    // ── Delete ─────────────────────────────────────────────────────────────

    @Test
    void deleteList_closesGap() {
        CardList list = makeList(10L, "L", board, 1);
        when(boardService.getBoardById(user, 1L)).thenReturn(board);
        when(cardListRepository.findById(10L)).thenReturn(Optional.of(list));

        cardListService.deleteList(user, 1L, 10L);

        verify(cardListRepository).deleteById(10L);
        verify(cardListRepository).decrementPositionsAfter(1L, 1);
    }

    // ── Same-board reorder ─────────────────────────────────────────────────

    @Test
    void sameBoardReorder_forward_callsShiftDown() {
        CardList list = makeList(10L, "L", board, 0);
        when(boardService.getBoardById(user, 1L)).thenReturn(board);
        when(cardListRepository.findById(10L)).thenReturn(Optional.of(list));
        when(cardListRepository.countByBoard_Id(1L)).thenReturn(3);
        when(cardListRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cardListService.moveCardList(user, 1L, 10L, new MoveCardListRequest(1L, 2));

        verify(cardListRepository).shiftDown(1L, 0, 2);
    }

    @Test
    void sameBoardReorder_samePosition_isNoOp() {
        CardList list = makeList(10L, "L", board, 1);
        when(boardService.getBoardById(user, 1L)).thenReturn(board);
        when(cardListRepository.findById(10L)).thenReturn(Optional.of(list));
        when(cardListRepository.countByBoard_Id(1L)).thenReturn(3);

        cardListService.moveCardList(user, 1L, 10L, new MoveCardListRequest(1L, 1));

        verify(cardListRepository, never()).shiftDown(any(), anyInt(), anyInt());
        verify(cardListRepository, never()).shiftUp(any(), anyInt(), anyInt());
        verify(cardListRepository, never()).save(any());
    }

    // ── Cross-board move ───────────────────────────────────────────────────

    @Test
    void crossBoardMove_callsDecrementOnSourceAndIncrementOnDest() {
        CardList list = makeList(10L, "L", board, 1);
        when(boardService.getBoardById(user, 1L)).thenReturn(board);
        when(boardService.getBoardById(user, 2L)).thenReturn(board2);
        when(cardListRepository.findById(10L)).thenReturn(Optional.of(list));
        when(cardListRepository.countByBoard_Id(2L)).thenReturn(2);
        when(cardListRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cardListService.moveCardList(user, 1L, 10L, new MoveCardListRequest(2L, 1));

        verify(cardListRepository).decrementPositionsAfter(1L, 1);
        verify(cardListRepository).incrementPositionsFrom(2L, 1);
    }

    // ── Negative position ──────────────────────────────────────────────────

    @Test
    void move_negativePosition_throwsInvalidPosition() {
        assertThatThrownBy(() ->
                cardListService.moveCardList(user, 1L, 10L, new MoveCardListRequest(1L, -1))
        ).isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode())
                .isEqualTo(ErrorCode.INVALID_POSITION);
    }

    // ── Authorization ──────────────────────────────────────────────────────

    @Test
    void crossBoardMove_unauthorizedDest_throwsException() {
        CardList list = makeList(10L, "L", board, 0);
        when(boardService.getBoardById(user, 1L)).thenReturn(board);
        when(cardListRepository.findById(10L)).thenReturn(Optional.of(list));

        // Target board lookup throws — simulates ownership failure
        when(boardService.getBoardById(user, 99L)).thenThrow(
                new ApiException(org.springframework.http.HttpStatus.NOT_FOUND,
                        ErrorCode.BOARD_NOT_FOUND, "Board not found"));

        assertThatThrownBy(() ->
                cardListService.moveCardList(user, 1L, 10L, new MoveCardListRequest(99L, 0))
        ).isInstanceOf(ApiException.class);
    }

    // ── Helper ─────────────────────────────────────────────────────────────

    private CardList makeList(Long id, String name, Board board, int position) {
        CardList l = new CardList();
        l.setId(id);
        l.setName(name);
        l.setBoard(board);
        l.setPosition(position);
        return l;
    }
}
