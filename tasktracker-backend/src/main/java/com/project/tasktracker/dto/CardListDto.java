package com.project.tasktracker.dto;

public class CardListDto {
    private Long id;
    private String name;
    private Long boardId;
    private int position;

    public CardListDto() {}

    public CardListDto(Long id, String name, Long boardId, int position) {
        this.id = id;
        this.name = name;
        this.boardId = boardId;
        this.position = position;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
