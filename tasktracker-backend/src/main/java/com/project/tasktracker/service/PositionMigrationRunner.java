package com.project.tasktracker.service;

import com.project.tasktracker.model.Card;
import com.project.tasktracker.model.CardList;
import com.project.tasktracker.repository.BoardRepository;
import com.project.tasktracker.repository.CardListRepository;
import com.project.tasktracker.repository.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Runs once after the Spring context starts to normalise {@code position}
 * values on {@code card_lists} and {@code cards} rows.
 *
 * <h3>Why this is needed</h3>
 * {@code spring.jpa.hibernate.ddl-auto=update} adds the {@code position} column
 * to existing tables but cannot assign meaningful values to pre-existing rows.
 * All pre-existing rows therefore start with position&nbsp;=&nbsp;0 (the column
 * default), which violates the uniqueness invariant.
 *
 * <h3>Idempotency</h3>
 * Before touching any parent group, the runner validates whether the group's
 * positions already form a contiguous {@code 0..N-1} sequence.  If they do,
 * no updates are performed for that group.  Re-running the runner on an
 * already-migrated database is therefore safe and produces no writes.
 *
 * <h3>Invariant validated</h3>
 * Positions are considered valid for a group of N children iff:
 * <ol>
 *   <li>The sorted distinct position values equal exactly {0, 1, …, N-1}.</li>
 * </ol>
 * Any deviation (duplicates, gaps, negatives, missing values) triggers
 * a full re-assignment ordered by {@code id ASC}.
 */
@Component
public class PositionMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PositionMigrationRunner.class);

    private final BoardRepository boardRepository;
    private final CardListRepository cardListRepository;
    private final CardRepository cardRepository;

    public PositionMigrationRunner(
            BoardRepository boardRepository,
            CardListRepository cardListRepository,
            CardRepository cardRepository) {
        this.boardRepository = boardRepository;
        this.cardListRepository = cardListRepository;
        this.cardRepository = cardRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("[PositionMigration] Starting position normalisation check …");
        migrateAll();
        log.info("[PositionMigration] Position normalisation complete.");
    }

    /**
     * Outer transaction: iterates all boards and delegates to per-list helpers.
     * A single outer transaction keeps all writes atomic; if any step fails the
     * entire migration rolls back so the DB is never left in a half-migrated state.
     */
    @Transactional
    public void migrateAll() {
        int totalListsNormalised = 0;
        int totalCardsNormalised = 0;

        List<Long> boardIds = boardRepository.findAll().stream()
                .map(b -> b.getId())
                .toList();

        for (Long boardId : boardIds) {
            // Fetch lists ordered by ID for deterministic initial assignment
            List<CardList> lists = cardListRepository.findByBoard_IdOrderByIdAsc(boardId);

            if (!isContiguous(lists.stream().map(CardList::getPosition).toList(), lists.size())) {
                log.info("[PositionMigration] Board {} — normalising {} lists", boardId, lists.size());
                for (int i = 0; i < lists.size(); i++) {
                    CardList l = lists.get(i);
                    if (l.getPosition() != i) {
                        l.setPosition(i);
                        cardListRepository.save(l);
                    }
                }
                totalListsNormalised += lists.size();
            }

            // For each list, normalise card positions
            for (CardList list : lists) {
                List<Card> cards = cardRepository.findByCardList_IdOrderByIdAsc(list.getId());
                if (!isContiguous(cards.stream().map(Card::getPosition).toList(), cards.size())) {
                    log.info("[PositionMigration] List {} — normalising {} cards", list.getId(), cards.size());
                    for (int i = 0; i < cards.size(); i++) {
                        Card c = cards.get(i);
                        if (c.getPosition() != i) {
                            c.setPosition(i);
                            cardRepository.save(c);
                        }
                    }
                    totalCardsNormalised += cards.size();
                }
            }
        }

        log.info("[PositionMigration] Lists normalised: {}, Cards normalised: {}",
                totalListsNormalised, totalCardsNormalised);

        // Post-migration diagnostic: warn if any duplicate positions remain
        verifyNoRemainingDuplicates();
    }

    /**
     * Returns {@code true} iff the given list of positions forms a contiguous
     * {@code 0..N-1} sequence with no duplicates, no gaps, and no negatives.
     *
     * @param positions list of position values (may be unsorted, may contain duplicates)
     * @param n         expected count
     */
    static boolean isContiguous(List<Integer> positions, int n) {
        if (positions.size() != n) return false;
        if (n == 0) return true;
        // Each expected value 0..N-1 must appear exactly once
        List<Integer> sorted = positions.stream().sorted().toList();
        return IntStream.range(0, n).allMatch(i -> sorted.get(i) == i);
    }

    /**
     * Diagnostic pass: detects any parent groups that still have duplicate positions
     * and emits a warning.  Does not modify data — this is a read-only verification step.
     */
    private void verifyNoRemainingDuplicates() {
        boolean clean = true;

        for (var board : boardRepository.findAll()) {
            List<CardList> lists = cardListRepository.findByBoard_IdOrderByPositionAsc(board.getId());
            List<Integer> listPositions = lists.stream().map(CardList::getPosition).toList();
            if (!isContiguous(listPositions, lists.size())) {
                log.warn("[PositionMigration] WARNING — Board {} still has invalid list positions: {}",
                        board.getId(), listPositions);
                clean = false;
            }

            for (CardList list : lists) {
                List<Card> cards = cardRepository.findByCardList_IdOrderByPositionAsc(list.getId());
                List<Integer> cardPositions = cards.stream().map(Card::getPosition).toList();
                if (!isContiguous(cardPositions, cards.size())) {
                    log.warn("[PositionMigration] WARNING — List {} still has invalid card positions: {}",
                            list.getId(), cardPositions);
                    clean = false;
                }
            }
        }

        if (clean) {
            log.info("[PositionMigration] Diagnostic: all position sequences are valid.");
        } else {
            log.warn("[PositionMigration] Diagnostic: some sequences are still invalid — see warnings above.");
        }
    }
}
