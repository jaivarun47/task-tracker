package com.project.tasktracker;

import com.project.tasktracker.dto.MoveCardListRequest;
import com.project.tasktracker.error.ApiException;
import com.project.tasktracker.model.Board;
import com.project.tasktracker.model.Card;
import com.project.tasktracker.model.CardList;
import com.project.tasktracker.model.User;
import com.project.tasktracker.repository.BoardRepository;
import com.project.tasktracker.repository.CardListRepository;
import com.project.tasktracker.repository.CardRepository;
import com.project.tasktracker.repository.SessionRepository;
import com.project.tasktracker.repository.UserRepository;
import com.project.tasktracker.service.CardListService;
import com.project.tasktracker.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * PostgreSQL-backed integration tests for CardList movement.
 * All 8 required scenarios are covered.
 */
class CardListMovementIntegrationTest extends AbstractIntegrationTest {

    @Autowired CardService cardService;
    @Autowired CardListService cardListService;
    @Autowired UserRepository userRepository;
    @Autowired BoardRepository boardRepository;
    @Autowired CardListRepository cardListRepository;
    @Autowired CardRepository cardRepository;
    @Autowired SessionRepository sessionRepository;

    TestDataHelper helper;

    @BeforeEach
    void setUp() {
        helper = new TestDataHelper(
                userRepository, boardRepository, cardListRepository,
                cardRepository, cardService, cardListService);
        // Clean slate: must delete sessions first due to FK constraint
        sessionRepository.deleteAll();
        cardRepository.deleteAll();
        cardListRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ─────────────────────────────────────────────────────────────────────
    // 1. Same-board reorder — list moves correctly
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void sameBoardReorder_listMovesCorrectly() {
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");
        CardList todo  = helper.createList(user, board.getId(), "TODO");     // 0
        CardList prog  = helper.createList(user, board.getId(), "PROGRESS"); // 1
        CardList done  = helper.createList(user, board.getId(), "DONE");     // 2

        // Move DONE (pos 2) → pos 0
        CardList moved = cardListService.moveCardList(user, board.getId(), done.getId(),
                new MoveCardListRequest(board.getId(), 0));

        assertThat(moved.getId()).isEqualTo(done.getId());
        assertThat(moved.getPosition()).isEqualTo(0);

        List<CardList> ordered = cardListRepository.findByBoard_IdOrderByPositionAsc(board.getId());
        assertThat(ordered).extracting(CardList::getName).containsExactly("DONE", "TODO", "PROGRESS");
        assertContiguousLists(board.getId(), 3);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2. Cross-board move — list moves to target board
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void crossBoardMove_listMovesToTargetBoard() {
        User user = helper.createUser();
        Board boardA = helper.createBoard(user, "A");
        Board boardB = helper.createBoard(user, "B");
        CardList list = helper.createList(user, boardA.getId(), "L");
        Long origId = list.getId();

        CardList moved = cardListService.moveCardList(user, boardA.getId(), list.getId(),
                new MoveCardListRequest(boardB.getId(), 0));

        assertThat(moved.getId()).isEqualTo(origId);
        CardList fresh = cardListRepository.findById(origId).orElseThrow();
        assertThat(fresh.getBoard().getId()).isEqualTo(boardB.getId());
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3. Cross-board move — cards remain intact
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void crossBoardMove_cardsRemainIntact() {
        User user = helper.createUser();
        Board boardA = helper.createBoard(user, "A");
        Board boardB = helper.createBoard(user, "B");
        CardList list = helper.createList(user, boardA.getId(), "L");
        Card c1 = helper.createCard(user, list.getId(), "C1");
        Card c2 = helper.createCard(user, list.getId(), "C2");

        cardListService.moveCardList(user, boardA.getId(), list.getId(),
                new MoveCardListRequest(boardB.getId(), 0));

        // Cards still exist, IDs unchanged, attached to the moved list
        Card freshC1 = cardRepository.findById(c1.getId()).orElseThrow();
        Card freshC2 = cardRepository.findById(c2.getId()).orElseThrow();
        assertThat(freshC1.getCardList().getId()).isEqualTo(list.getId());
        assertThat(freshC2.getCardList().getId()).isEqualTo(list.getId());
        assertThat(freshC1.getName()).isEqualTo("C1");
        assertThat(freshC2.getName()).isEqualTo("C2");
        // Internal card positions still contiguous
        assertContiguousCards(list.getId(), 2);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4. Cross-board move — source positions repaired
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void crossBoardMove_sourcePositionsRepaired() {
        User user = helper.createUser();
        Board boardA = helper.createBoard(user, "A");
        Board boardB = helper.createBoard(user, "B");
        CardList l0 = helper.createList(user, boardA.getId(), "L0"); // 0
        CardList l1 = helper.createList(user, boardA.getId(), "L1"); // 1
        CardList l2 = helper.createList(user, boardA.getId(), "L2"); // 2

        // Move l1 out
        cardListService.moveCardList(user, boardA.getId(), l1.getId(),
                new MoveCardListRequest(boardB.getId(), 0));

        assertContiguousLists(boardA.getId(), 2);
        List<CardList> remaining = cardListRepository.findByBoard_IdOrderByPositionAsc(boardA.getId());
        assertThat(remaining).extracting(CardList::getName).containsExactly("L0", "L2");
    }

    // ─────────────────────────────────────────────────────────────────────
    // 5. Cross-board move — destination positions shifted
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void crossBoardMove_destinationPositionsShifted() {
        User user = helper.createUser();
        Board boardA = helper.createBoard(user, "A");
        Board boardB = helper.createBoard(user, "B");
        CardList moving = helper.createList(user, boardA.getId(), "M");
        helper.createList(user, boardB.getId(), "D0"); // 0
        helper.createList(user, boardB.getId(), "D1"); // 1

        // Insert at pos 0 in boardB
        cardListService.moveCardList(user, boardA.getId(), moving.getId(),
                new MoveCardListRequest(boardB.getId(), 0));

        assertContiguousLists(boardB.getId(), 3);
        List<CardList> dstLists = cardListRepository.findByBoard_IdOrderByPositionAsc(boardB.getId());
        assertThat(dstLists).extracting(CardList::getName).containsExactly("M", "D0", "D1");
    }

    // ─────────────────────────────────────────────────────────────────────
    // 6. List create — position is next in board
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void listCreate_positionIsNextInBoard() {
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");

        CardList l0 = helper.createList(user, board.getId(), "L0");
        CardList l1 = helper.createList(user, board.getId(), "L1");
        CardList l2 = helper.createList(user, board.getId(), "L2");

        assertThat(l0.getPosition()).isEqualTo(0);
        assertThat(l1.getPosition()).isEqualTo(1);
        assertThat(l2.getPosition()).isEqualTo(2);
        assertContiguousLists(board.getId(), 3);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 7. List delete — position gap closed
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void listDelete_positionGapClosed() {
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");
        CardList l0 = helper.createList(user, board.getId(), "L0"); // 0
        CardList l1 = helper.createList(user, board.getId(), "L1"); // 1
        CardList l2 = helper.createList(user, board.getId(), "L2"); // 2

        cardListService.deleteList(user, board.getId(), l1.getId());

        assertContiguousLists(board.getId(), 2);
        List<CardList> remaining = cardListRepository.findByBoard_IdOrderByPositionAsc(board.getId());
        assertThat(remaining).extracting(CardList::getName).containsExactly("L0", "L2");
    }

    // ─────────────────────────────────────────────────────────────────────
    // 8. Authorization — cross-user list move rejected
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void unauthorizedCrossUserListMove_rejected() {
        User userA = helper.createUser();
        User userB = helper.createUser();
        Board boardA = helper.createBoard(userA, "A");
        Board boardB = helper.createBoard(userB, "B");
        CardList listA = helper.createList(userA, boardA.getId(), "LA");

        // userA tries to move their list into userB's board
        assertThatThrownBy(() ->
                cardListService.moveCardList(userA, boardA.getId(), listA.getId(),
                        new MoveCardListRequest(boardB.getId(), 0))
        ).isInstanceOf(ApiException.class);

        // List must remain in boardA
        CardList fresh = cardListRepository.findById(listA.getId()).orElseThrow();
        assertThat(fresh.getBoard().getId()).isEqualTo(boardA.getId());
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void assertContiguousLists(Long boardId, int expectedCount) {
        List<Integer> positions = helper.listPositions(boardId);
        assertThat(positions).hasSize(expectedCount);
        for (int i = 0; i < expectedCount; i++) {
            assertThat(positions.get(i))
                    .as("List position at index %d should be %d but was %d in %s",
                            i, i, positions.get(i), positions)
                    .isEqualTo(i);
        }
    }

    private void assertContiguousCards(Long listId, int expectedCount) {
        List<Integer> positions = helper.cardPositions(listId);
        assertThat(positions).hasSize(expectedCount);
        for (int i = 0; i < expectedCount; i++) {
            assertThat(positions.get(i)).isEqualTo(i);
        }
    }
}
