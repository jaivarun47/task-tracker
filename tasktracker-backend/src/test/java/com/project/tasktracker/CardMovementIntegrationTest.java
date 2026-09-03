package com.project.tasktracker;

import com.project.tasktracker.dto.MoveCardRequest;
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
 * PostgreSQL-backed integration tests for Card movement.
 * All 15 required scenarios are covered.
 */
class CardMovementIntegrationTest extends AbstractIntegrationTest {

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
    // 1. Same-list reorder — middle to beginning
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void sameListReorder_middleToBeginning() {
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");
        CardList list = helper.createList(user, board.getId(), "L");
        Card a = helper.createCard(user, list.getId(), "A"); // pos 0
        Card b = helper.createCard(user, list.getId(), "B"); // pos 1
        Card c = helper.createCard(user, list.getId(), "C"); // pos 2
        Card d = helper.createCard(user, list.getId(), "D"); // pos 3

        // Move D (pos 3) → pos 1
        MoveCardRequest req = new MoveCardRequest(list.getId(), 1);
        Card moved = cardService.moveCard(user, list.getId(), d.getId(), req);

        assertThat(moved.getId()).isEqualTo(d.getId());
        assertThat(moved.getPosition()).isEqualTo(1);

        List<Card> ordered = cardRepository.findByCardList_IdOrderByPositionAsc(list.getId());
        assertThat(ordered).extracting(Card::getName).containsExactly("A", "D", "B", "C");
        assertContiguousCards(list.getId(), 4);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2. Same-list reorder — first to last
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void sameListReorder_firstToLast() {
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");
        CardList list = helper.createList(user, board.getId(), "L");
        helper.createCard(user, list.getId(), "A"); // pos 0
        helper.createCard(user, list.getId(), "B"); // pos 1
        helper.createCard(user, list.getId(), "C"); // pos 2
        Card a = cardRepository.findByCardList_IdOrderByPositionAsc(list.getId()).get(0);

        cardService.moveCard(user, list.getId(), a.getId(), new MoveCardRequest(list.getId(), 2));

        List<Card> ordered = cardRepository.findByCardList_IdOrderByPositionAsc(list.getId());
        assertThat(ordered).extracting(Card::getName).containsExactly("B", "C", "A");
        assertContiguousCards(list.getId(), 3);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3. Same-list reorder — last to first
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void sameListReorder_lastToFirst() {
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");
        CardList list = helper.createList(user, board.getId(), "L");
        helper.createCard(user, list.getId(), "A");
        helper.createCard(user, list.getId(), "B");
        helper.createCard(user, list.getId(), "C");
        Card c = cardRepository.findByCardList_IdOrderByPositionAsc(list.getId()).get(2);

        cardService.moveCard(user, list.getId(), c.getId(), new MoveCardRequest(list.getId(), 0));

        List<Card> ordered = cardRepository.findByCardList_IdOrderByPositionAsc(list.getId());
        assertThat(ordered).extracting(Card::getName).containsExactly("C", "A", "B");
        assertContiguousCards(list.getId(), 3);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4. Same-list reorder — same position is a no-op
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void sameListReorder_samePosition_isNoOp() {
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");
        CardList list = helper.createList(user, board.getId(), "L");
        helper.createCard(user, list.getId(), "A");
        helper.createCard(user, list.getId(), "B");
        Card b = cardRepository.findByCardList_IdOrderByPositionAsc(list.getId()).get(1);

        Card result = cardService.moveCard(user, list.getId(), b.getId(), new MoveCardRequest(list.getId(), 1));

        assertThat(result.getPosition()).isEqualTo(1);
        assertContiguousCards(list.getId(), 2);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 5. Cross-list move — card moves to target list
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void crossListMove_cardMovesToTargetList() {
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");
        CardList src = helper.createList(user, board.getId(), "Src");
        CardList dst = helper.createList(user, board.getId(), "Dst");
        Card card = helper.createCard(user, src.getId(), "X");
        String origName = card.getName();
        String origDesc = card.getDescription();
        boolean origCompleted = card.isCompleted();
        Long origId = card.getId();

        Card moved = cardService.moveCard(user, src.getId(), card.getId(),
                new MoveCardRequest(dst.getId(), 0));

        // Identity and data preserved
        assertThat(moved.getId()).isEqualTo(origId);
        assertThat(moved.getName()).isEqualTo(origName);
        assertThat(moved.getDescription()).isEqualTo(origDesc);
        assertThat(moved.isCompleted()).isEqualTo(origCompleted);

        // Relationship changed
        Card fresh = cardRepository.findById(origId).orElseThrow();
        assertThat(fresh.getCardList().getId()).isEqualTo(dst.getId());
    }

    // ─────────────────────────────────────────────────────────────────────
    // 6. Cross-list move — source positions repaired
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void crossListMove_sourcePositionsRepaired() {
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");
        CardList src = helper.createList(user, board.getId(), "Src");
        CardList dst = helper.createList(user, board.getId(), "Dst");
        helper.createCard(user, src.getId(), "A"); // 0
        Card b = helper.createCard(user, src.getId(), "B"); // 1
        helper.createCard(user, src.getId(), "C"); // 2

        // Move B out of src
        cardService.moveCard(user, src.getId(), b.getId(), new MoveCardRequest(dst.getId(), 0));

        // src should now have A=0, C=1 — no gap
        assertContiguousCards(src.getId(), 2);
        List<Card> srcCards = cardRepository.findByCardList_IdOrderByPositionAsc(src.getId());
        assertThat(srcCards).extracting(Card::getName).containsExactly("A", "C");
    }

    // ─────────────────────────────────────────────────────────────────────
    // 7. Cross-list move — destination positions shifted
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void crossListMove_destinationPositionsShifted() {
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");
        CardList src = helper.createList(user, board.getId(), "Src");
        CardList dst = helper.createList(user, board.getId(), "Dst");
        Card x = helper.createCard(user, src.getId(), "X");
        helper.createCard(user, dst.getId(), "D0"); // 0
        helper.createCard(user, dst.getId(), "D1"); // 1

        // Insert X at position 0 in dst
        cardService.moveCard(user, src.getId(), x.getId(), new MoveCardRequest(dst.getId(), 0));

        assertContiguousCards(dst.getId(), 3);
        List<Card> dstCards = cardRepository.findByCardList_IdOrderByPositionAsc(dst.getId());
        assertThat(dstCards).extracting(Card::getName).containsExactly("X", "D0", "D1");
    }

    // ─────────────────────────────────────────────────────────────────────
    // 8. Cross-board card move via target list
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void crossBoardCardMove_viaTargetList() {
        User user = helper.createUser();
        Board boardA = helper.createBoard(user, "A");
        Board boardB = helper.createBoard(user, "B");
        CardList listA = helper.createList(user, boardA.getId(), "LA");
        CardList listB = helper.createList(user, boardB.getId(), "LB");
        Card card = helper.createCard(user, listA.getId(), "X");
        Long origId = card.getId();

        cardService.moveCard(user, listA.getId(), card.getId(), new MoveCardRequest(listB.getId(), 0));

        Card fresh = cardRepository.findById(origId).orElseThrow();
        assertThat(fresh.getId()).isEqualTo(origId);
        assertThat(fresh.getCardList().getId()).isEqualTo(listB.getId());
        CardList targetList = cardListRepository.findById(listB.getId()).orElseThrow();
        assertThat(targetList.getBoard().getId()).isEqualTo(boardB.getId());
    }

    // ─────────────────────────────────────────────────────────────────────
    // 9. Card create — position is next in list
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void cardCreate_positionIsNextInList() {
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");
        CardList list = helper.createList(user, board.getId(), "L");

        Card c0 = helper.createCard(user, list.getId(), "C0");
        Card c1 = helper.createCard(user, list.getId(), "C1");
        Card c2 = helper.createCard(user, list.getId(), "C2");

        assertThat(c0.getPosition()).isEqualTo(0);
        assertThat(c1.getPosition()).isEqualTo(1);
        assertThat(c2.getPosition()).isEqualTo(2);
        assertContiguousCards(list.getId(), 3);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 10. Card delete — position gap closed
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void cardDelete_positionGapClosed() {
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");
        CardList list = helper.createList(user, board.getId(), "L");
        helper.createCard(user, list.getId(), "A"); // 0
        Card b = helper.createCard(user, list.getId(), "B"); // 1
        helper.createCard(user, list.getId(), "C"); // 2

        cardService.deleteCard(user, list.getId(), b.getId());

        assertContiguousCards(list.getId(), 2);
        List<Card> remaining = cardRepository.findByCardList_IdOrderByPositionAsc(list.getId());
        assertThat(remaining).extracting(Card::getName).containsExactly("A", "C");
    }

    // ─────────────────────────────────────────────────────────────────────
    // 11. Stale-context safety — bulk updates not overwritten
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void staleContextSafety_bulkUpdateNotOverwritten() {
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");
        CardList list = helper.createList(user, board.getId(), "L");
        Card a = helper.createCard(user, list.getId(), "A"); // 0
        Card b = helper.createCard(user, list.getId(), "B"); // 1
        Card c = helper.createCard(user, list.getId(), "C"); // 2

        // Move A (pos 0) to pos 2. During this call:
        //   1. Card A is loaded into the persistence context (position=0)
        //   2. shiftDown is called — bulk UPDATE shifts B and C
        //   3. clearAutomatically=true evicts all entities from context
        //   4. Service re-loads Card A from DB
        //   5. Sets position=2 and saves
        // If the stale Card A (position=0) were saved instead of the re-loaded one,
        // we would end up with A=0, B=0, C=1 — clearly wrong.
        cardService.moveCard(user, list.getId(), a.getId(), new MoveCardRequest(list.getId(), 2));

        List<Card> ordered = cardRepository.findByCardList_IdOrderByPositionAsc(list.getId());
        assertThat(ordered).extracting(Card::getName).containsExactly("B", "C", "A");
        assertContiguousCards(list.getId(), 3);

        // Explicitly verify sibling positions were not overwritten
        Card freshB = cardRepository.findByCardList_IdOrderByPositionAsc(list.getId()).get(0);
        assertThat(freshB.getName()).isEqualTo("B");
        assertThat(freshB.getPosition()).isEqualTo(0);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 12. orphanRemoval — move does NOT delete the card
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void orphanRemoval_moveDoesNotDeleteCard() {
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");
        CardList src = helper.createList(user, board.getId(), "Src");
        CardList dst = helper.createList(user, board.getId(), "Dst");
        Card card = helper.createCard(user, src.getId(), "X");
        Long cardId = card.getId();

        cardService.moveCard(user, src.getId(), cardId, new MoveCardRequest(dst.getId(), 0));

        // Card must still exist with the same ID
        assertThat(cardRepository.findById(cardId)).isPresent();
        // Old list must still exist
        assertThat(cardListRepository.findById(src.getId())).isPresent();
    }

    // ─────────────────────────────────────────────────────────────────────
    // 13. Cascade delete — deleting a list deletes its cards
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void cascadeDelete_listDeletesCascadeToCards() {
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");
        CardList list = helper.createList(user, board.getId(), "L");
        Card c1 = helper.createCard(user, list.getId(), "C1");
        Card c2 = helper.createCard(user, list.getId(), "C2");

        cardListService.deleteList(user, board.getId(), list.getId());

        assertThat(cardRepository.findById(c1.getId())).isEmpty();
        assertThat(cardRepository.findById(c2.getId())).isEmpty();
        assertThat(cardListRepository.findById(list.getId())).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────────────
    // 14. Transactional rollback — failed move is atomic
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void transactionalRollback_failedMoveIsAtomic() {
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");
        CardList src = helper.createList(user, board.getId(), "Src");
        helper.createCard(user, src.getId(), "A"); // 0
        Card b = helper.createCard(user, src.getId(), "B"); // 1

        // Attempt to move into a non-existent list — should throw and roll back
        assertThatThrownBy(() ->
                cardService.moveCard(user, src.getId(), b.getId(),
                        new MoveCardRequest(Long.MAX_VALUE, 0))
        ).isInstanceOf(ApiException.class);

        // Source positions must be unchanged
        assertContiguousCards(src.getId(), 2);
        List<Card> remaining = cardRepository.findByCardList_IdOrderByPositionAsc(src.getId());
        assertThat(remaining).extracting(Card::getName).containsExactly("A", "B");
    }

    // ─────────────────────────────────────────────────────────────────────
    // 15. Authorization — cross-user move rejected
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void unauthorizedCrossUserMove_rejected() {
        User userA = helper.createUser();
        User userB = helper.createUser();

        Board boardA = helper.createBoard(userA, "A");
        Board boardB = helper.createBoard(userB, "B");
        CardList listA = helper.createList(userA, boardA.getId(), "LA");
        CardList listB = helper.createList(userB, boardB.getId(), "LB");
        Card card = helper.createCard(userA, listA.getId(), "X");

        // userA tries to move their card into userB's list
        assertThatThrownBy(() ->
                cardService.moveCard(userA, listA.getId(), card.getId(),
                        new MoveCardRequest(listB.getId(), 0))
        ).isInstanceOf(ApiException.class);

        // Card must remain in listA
        Card fresh = cardRepository.findById(card.getId()).orElseThrow();
        assertThat(fresh.getCardList().getId()).isEqualTo(listA.getId());
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void assertContiguousCards(Long listId, int expectedCount) {
        List<Integer> positions = helper.cardPositions(listId);
        assertThat(positions).hasSize(expectedCount);
        for (int i = 0; i < expectedCount; i++) {
            assertThat(positions.get(i))
                    .as("Position at index %d should be %d but was %d in %s", i, i, positions.get(i), positions)
                    .isEqualTo(i);
        }
    }
}
