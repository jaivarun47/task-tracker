package com.project.tasktracker.repository;

import com.project.tasktracker.model.CardList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardListRepository extends JpaRepository<CardList, Long> {

    // ── Ordered reads ──────────────────────────────────────────────────────

    /** Primary fetch — lists ordered by position (authoritative). */
    List<CardList> findByBoard_IdOrderByPositionAsc(Long boardId);

    /** Migration-only fetch — ordered by DB-assigned id for deterministic initial assignment. */
    List<CardList> findByBoard_IdOrderByIdAsc(Long boardId);

    /** Count of lists in a board; used to compute next append position. */
    int countByBoard_Id(Long boardId);

    // ── Bulk position updates (all use flushAutomatically + clearAutomatically) ──
    //
    // See CardRepository for the rationale on flush/clear flags.

    /**
     * Close the gap left by a removed list.
     * Decrements positions of all lists in {@code boardId} that are strictly
     * greater than {@code pos}.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE CardList l SET l.position = l.position - 1 " +
           "WHERE l.board.id = :boardId AND l.position > :pos")
    void decrementPositionsAfter(@Param("boardId") Long boardId, @Param("pos") int pos);

    /**
     * Make room for an insertion at {@code pos}.
     * Increments positions of all lists in {@code boardId} that are greater
     * than or equal to {@code pos}.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE CardList l SET l.position = l.position + 1 " +
           "WHERE l.board.id = :boardId AND l.position >= :pos")
    void incrementPositionsFrom(@Param("boardId") Long boardId, @Param("pos") int pos);

    /**
     * Same-board reorder — moving a list FORWARD (newPos > oldPos).
     * Shifts lists in the half-open range (lower, upper] down by 1.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE CardList l SET l.position = l.position - 1 " +
           "WHERE l.board.id = :boardId AND l.position > :lower AND l.position <= :upper")
    void shiftDown(@Param("boardId") Long boardId, @Param("lower") int lower, @Param("upper") int upper);

    /**
     * Same-board reorder — moving a list BACKWARD (newPos < oldPos).
     * Shifts lists in the half-open range [lower, upper) up by 1.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE CardList l SET l.position = l.position + 1 " +
           "WHERE l.board.id = :boardId AND l.position >= :lower AND l.position < :upper")
    void shiftUp(@Param("boardId") Long boardId, @Param("lower") int lower, @Param("upper") int upper);
}