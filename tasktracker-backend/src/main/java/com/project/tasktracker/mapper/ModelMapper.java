package com.project.tasktracker.mapper;

import com.project.tasktracker.dto.BoardDto;
import com.project.tasktracker.dto.CardDto;
import com.project.tasktracker.dto.CardListDto;
import com.project.tasktracker.dto.UserDto;
import com.project.tasktracker.model.Board;
import com.project.tasktracker.model.Card;
import com.project.tasktracker.model.CardList;
import com.project.tasktracker.model.User;

public class ModelMapper {

    public static UserDto toUserDto(User user) {
        if (user == null) return null;
        return new UserDto(user.getId(), user.getEmail());
    }

    public static BoardDto toBoardDto(Board board) {
        if (board == null) return null;
        return new BoardDto(
                board.getId(),
                board.getName(),
                board.getUser() != null ? board.getUser().getId() : null
        );
    }

    public static CardListDto toCardListDto(CardList list) {
        if (list == null) return null;
        return new CardListDto(
                list.getId(),
                list.getName(),
                list.getBoard() != null ? list.getBoard().getId() : null,
                list.getPosition()
        );
    }

    public static CardDto toCardDto(Card card) {
        if (card == null) return null;
        return new CardDto(
                card.getId(),
                card.getName(),
                card.getDescription(),
                card.isCompleted(),
                card.getCreatedAt(),
                card.getCardList() != null ? card.getCardList().getId() : null,
                card.getPosition()
        );
    }
}
