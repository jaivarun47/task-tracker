package com.project.tasktracker.repository;

import com.project.tasktracker.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    // ── Ordered reads ──────────────────────────────────────────────────────

    /** Primary fetch — cards ordered by position (authoritative). */
    List<Card> findByCardList_IdOrderByPositionAsc(Long listId);

    /** Migration-only fetch — ordered by DB-assigned id for deterministic initial assignment. */
    List<Card> findByCardList_IdOrderByIdAsc(Long listId);

    /** Count of cards in a list; used to compute next append position. */
    int countByCardList_Id(Long listId);

    void deleteByCardList_Id(Long listId);

    // ── Bulk position updates (all use flushAutomatically + clearAutomatically) ──
    //
    // IMPORTANT: flushAutomatically = true  — flush pending dirty state BEFORE the bulk
    //            update so that no in-memory changes conflict with the JPQL UPDATE.
    //            clearAutomatically = true  — evict all managed entities AFTER the bulk
    //            update so that stale first-level-cache values cannot overwrite the
    //            correct DB state when save() is subsequently called.

    /**
     * Close the gap left by a removed card.
     * Decrements positions of all cards in {@code listId} that are strictly
     * greater than {@code pos}.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Card c SET c.position = c.position - 1 " +
           "WHERE c.cardList.id = :listId AND c.position > :pos")
    void decrementPositionsAfter(@Param("listId") Long listId, @Param("pos") int pos);

    /**
     * Make room for an insertion at {@code pos}.
     * Increments positions of all cards in {@code listId} that are greater
     * than or equal to {@code pos}.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Card c SET c.position = c.position + 1 " +
           "WHERE c.cardList.id = :listId AND c.position >= :pos")
    void incrementPositionsFrom(@Param("listId") Long listId, @Param("pos") int pos);

    /**
     * Same-list reorder — moving an item FORWARD (newPos > oldPos).
     * Shifts items in the half-open range (lower, upper] down by 1.
     * Range: positions strictly greater than {@code lower} and ≤ {@code upper}.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Card c SET c.position = c.position - 1 " +
           "WHERE c.cardList.id = :listId AND c.position > :lower AND c.position <= :upper")
    void shiftDown(@Param("listId") Long listId, @Param("lower") int lower, @Param("upper") int upper);

    /**
     * Same-list reorder — moving an item BACKWARD (newPos < oldPos).
     * Shifts items in the half-open range [lower, upper) up by 1.
     * Range: positions ≥ {@code lower} and strictly less than {@code upper}.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Card c SET c.position = c.position + 1 " +
           "WHERE c.cardList.id = :listId AND c.position >= :lower AND c.position < :upper")
    void shiftUp(@Param("listId") Long listId, @Param("lower") int lower, @Param("upper") int upper);
}