package com.project.tasktracker;

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
import com.project.tasktracker.service.PositionMigrationRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for {@link PositionMigrationRunner}.
 *
 * <p>Simulates the "pre-migration" state by directly inserting rows with
 * duplicate/invalid positions, then invoking the runner and verifying the
 * resulting DB state.
 */
class PositionMigrationIntegrationTest extends AbstractIntegrationTest {

    @Autowired PositionMigrationRunner migrationRunner;
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
        // Must delete sessions first to satisfy FK constraint
        sessionRepository.deleteAll();
        cardRepository.deleteAll();
        cardListRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ─────────────────────────────────────────────────────────────────────
    // 1. After migration, no two siblings share the same position
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void migration_existingRowsGetDistinctPositions() throws Exception {
        // Simulate pre-migration state: all rows land at position 0 (the DB default)
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");

        // Directly save lists with duplicate positions to mimic post-DDL-add state
        CardList l1 = saveListDirect(board, "L1", 0);
        CardList l2 = saveListDirect(board, "L2", 0);
        CardList l3 = saveListDirect(board, "L3", 0);

        // Verify the broken state before migration
        List<Integer> before = helper.listPositions(board.getId());
        assertThat(before).containsExactly(0, 0, 0); // all zeros — broken

        // Run migration
        migrationRunner.migrateAll();

        // After migration, positions must be distinct
        List<Integer> after = helper.listPositions(board.getId());
        assertThat(after).doesNotHaveDuplicates();
        assertThat(after).hasSize(3);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2. Positions are zero-based and ascending after migration
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void migration_positionsAreZeroBasedAscending() throws Exception {
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");

        // Simulate broken state with duplicate positions for both lists and cards
        CardList l1 = saveListDirect(board, "L1", 0);
        CardList l2 = saveListDirect(board, "L2", 0);
        saveCardDirect(l1, "C1", 0);
        saveCardDirect(l1, "C2", 0);
        saveCardDirect(l1, "C3", 0);

        migrationRunner.migrateAll();

        // Lists: 0, 1
        List<Integer> listPos = helper.listPositions(board.getId());
        assertIsZeroBasedAscending(listPos);

        // Cards in L1: 0, 1, 2
        List<Integer> cardPos = helper.cardPositions(l1.getId());
        assertIsZeroBasedAscending(cardPos);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3. Migration is idempotent — running twice does not corrupt positions
    // ─────────────────────────────────────────────────────────────────────
    @Test
    void migration_isIdempotent() throws Exception {
        User user = helper.createUser();
        Board board = helper.createBoard(user, "B");
        CardList list = helper.createList(user, board.getId(), "L");
        helper.createCard(user, list.getId(), "C1");
        helper.createCard(user, list.getId(), "C2");

        // Positions already correct from service layer
        List<Integer> before = helper.cardPositions(list.getId());
        assertIsZeroBasedAscending(before);

        // Run migration twice
        migrationRunner.migrateAll();
        migrationRunner.migrateAll();

        // Positions must remain unchanged
        List<Integer> after = helper.cardPositions(list.getId());
        assertThat(after).isEqualTo(before);
        assertIsZeroBasedAscending(helper.listPositions(board.getId()));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /** Directly saves a CardList with an explicitly supplied position, bypassing service logic. */
    private CardList saveListDirect(Board board, String name, int position) {
        CardList l = new CardList();
        l.setName(name);
        l.setBoard(board);
        l.setPosition(position);
        return cardListRepository.save(l);
    }

    /** Directly saves a Card with an explicitly supplied position, bypassing service logic. */
    private Card saveCardDirect(CardList list, String name, int position) {
        Card c = new Card();
        c.setName(name);
        c.setDescription("d");
        c.setCardList(list);
        c.setPosition(position);
        c.setCreatedAt(java.time.LocalDateTime.now());
        return cardRepository.save(c);
    }

    private void assertIsZeroBasedAscending(List<Integer> positions) {
        assertThat(positions).isNotEmpty();
        List<Integer> expected = IntStream.range(0, positions.size()).boxed().toList();
        assertThat(positions).isEqualTo(expected);
    }
}
