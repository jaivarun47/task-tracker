package com.project.tasktracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "cards",
    indexes = {
        @Index(name = "idx_cards_list_position", columnList = "list_id, position")
    }
)
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private boolean completed;
    private LocalDateTime createdAt;

    /**
     * Zero-based position within the parent CardList.
     * Uses a DB default of 0 so that ddl-auto=update can safely add this
     * column to an existing table without failing on NOT NULL constraints.
     * PositionMigrationRunner normalises positions to 0..N-1 on startup.
     */
    @Column(nullable = false, columnDefinition = "integer default 0")
    private int position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false)
    private CardList cardList;

    public Card() {
    }

    public Card(Long id, String name, String description, boolean completed,
                LocalDateTime createdAt, CardList cardList) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.completed = completed;
        this.createdAt = createdAt;
        this.cardList = cardList;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public CardList getCardList() { return cardList; }
    public void setCardList(CardList cardList) { this.cardList = cardList; }
}
