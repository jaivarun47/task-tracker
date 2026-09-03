package com.project.tasktracker;

import com.project.tasktracker.dto.CreateCardRequest;
import com.project.tasktracker.dto.MoveCardRequest;
import com.project.tasktracker.error.ApiException;
import com.project.tasktracker.error.ErrorCode;
import com.project.tasktracker.model.Board;
import com.project.tasktracker.model.Card;
import com.project.tasktracker.model.CardList;
import com.project.tasktracker.model.User;
import com.project.tasktracker.repository.CardListRepository;
import com.project.tasktracker.repository.CardRepository;
import com.project.tasktracker.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Mockito-based unit tests for {@link CardService} business logic.
 * Fast, no database required.
 */
@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock CardListRepository cardListRepository;
    @Mock CardRepository cardRepository;
    @InjectMocks CardService cardService;

    User user;
    Board board;
    CardList list;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        board = new Board();
        board.setId(1L);
        board.setUser(user);
        list = new CardList();
        list.setId(10L);
        list.setBoard(board);
    }

    // ── Create ─────────────────────────────────────────────────────────────

    @Test
    void createCard_appendsAtCount() {
        when(cardListRepository.findById(10L)).thenReturn(Optional.of(list));
        when(cardRepository.countByCardList_Id(10L)).thenReturn(3);
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateCardRequest req = new CreateCardRequest();
        req.setName("X");
        req.setDescription("desc");

        Card result = cardService.createCard(user, 10L, req);

        assertThat(result.getPosition()).isEqualTo(3);
        verify(cardRepository).save(argThat(c -> c.getPosition() == 3));
    }

    // ── Delete ─────────────────────────────────────────────────────────────

    @Test
    void deleteCard_closesGap() {
        Card card = makeCard(42L, "C", list, 2);
        when(cardListRepository.findById(10L)).thenReturn(Optional.of(list));
        when(cardRepository.findById(42L)).thenReturn(Optional.of(card));

        cardService.deleteCard(user, 10L, 42L);

        verify(cardRepository).deleteById(42L);
        verify(cardRepository).decrementPositionsAfter(10L, 2);
    }

    // ── Same-list reorder ──────────────────────────────────────────────────

    @Test
    void sameListMove_forward_callsShiftDown() {
        Card card = makeCard(5L, "C", list, 1); // pos 1
        when(cardListRepository.findById(10L)).thenReturn(Optional.of(list));
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));
        when(cardRepository.countByCardList_Id(10L)).thenReturn(4);
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card)); // re-load

        MoveCardRequest req = new MoveCardRequest(10L, 3); // forward: 1 → 3
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cardService.moveCard(user, 10L, 5L, req);

        verify(cardRepository).shiftDown(10L, 1, 3);
        verify(cardRepository, never()).shiftUp(any(), anyInt(), anyInt());
    }

    @Test
    void sameListMove_backward_callsShiftUp() {
        Card card = makeCard(5L, "C", list, 3); // pos 3
        when(cardListRepository.findById(10L)).thenReturn(Optional.of(list));
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));
        when(cardRepository.countByCardList_Id(10L)).thenReturn(4);
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MoveCardRequest req = new MoveCardRequest(10L, 0); // backward: 3 → 0
        cardService.moveCard(user, 10L, 5L, req);

        verify(cardRepository).shiftUp(10L, 0, 3);
        verify(cardRepository, never()).shiftDown(any(), anyInt(), anyInt());
    }

    @Test
    void sameListMove_samePosition_isNoOp() {
        Card card = makeCard(5L, "C", list, 2);
        when(cardListRepository.findById(10L)).thenReturn(Optional.of(list));
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));
        when(cardRepository.countByCardList_Id(10L)).thenReturn(4);

        cardService.moveCard(user, 10L, 5L, new MoveCardRequest(10L, 2));

        verify(cardRepository, never()).shiftDown(any(), anyInt(), anyInt());
        verify(cardRepository, never()).shiftUp(any(), anyInt(), anyInt());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void move_oversizedPosition_isClamped_sameList() {
        Card card = makeCard(5L, "C", list, 0);
        when(cardListRepository.findById(10L)).thenReturn(Optional.of(list));
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));
        when(cardRepository.countByCardList_Id(10L)).thenReturn(3); // max valid = 2
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Requesting position 99 should be clamped to 2
        cardService.moveCard(user, 10L, 5L, new MoveCardRequest(10L, 99));

        verify(cardRepository).shiftDown(10L, 0, 2); // clamped to 2
    }

    // ── Negative position ──────────────────────────────────────────────────

    @Test
    void move_negativePosition_throwsInvalidPosition() {
        assertThatThrownBy(() ->
                cardService.moveCard(user, 10L, 5L, new MoveCardRequest(10L, -1))
        ).isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode())
                .isEqualTo(ErrorCode.INVALID_POSITION);
    }

    // ── Authorization ──────────────────────────────────────────────────────

    @Test
    void move_unauthorizedTargetList_throwsForbidden() {
        // Source list belongs to our user
        when(cardListRepository.findById(10L)).thenReturn(Optional.of(list));
        Card card = makeCard(5L, "C", list, 0);
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        // Target list belongs to a different user
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        Board otherBoard = new Board();
        otherBoard.setId(99L);
        otherBoard.setUser(otherUser);
        CardList otherList = new CardList();
        otherList.setId(20L);
        otherList.setBoard(otherBoard);
        when(cardListRepository.findById(20L)).thenReturn(Optional.of(otherList));

        assertThatThrownBy(() ->
                cardService.moveCard(user, 10L, 5L, new MoveCardRequest(20L, 0))
        ).isInstanceOf(ApiException.class);
    }

    // ── Helper ─────────────────────────────────────────────────────────────

    private Card makeCard(Long id, String name, CardList list, int position) {
        Card c = new Card();
        c.setId(id);
        c.setName(name);
        c.setCardList(list);
        c.setPosition(position);
        c.setCreatedAt(LocalDateTime.now());
        return c;
    }
}
