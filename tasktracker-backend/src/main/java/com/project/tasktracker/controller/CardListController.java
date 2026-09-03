package com.project.tasktracker.controller;

import com.project.tasktracker.dto.CardListDto;
import com.project.tasktracker.dto.CreateCardListRequest;
import com.project.tasktracker.dto.MoveCardListRequest;
import com.project.tasktracker.mapper.ModelMapper;
import com.project.tasktracker.model.User;
import com.project.tasktracker.security.CurrentUser;
import com.project.tasktracker.service.CardListService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/boards/{boardId}/lists")
public class CardListController {

    private final CardListService cardListService;

    public CardListController(CardListService cardListService) {
        this.cardListService = cardListService;
    }

    @PostMapping
    public CardListDto createCardList(
            @CurrentUser User user,
            @PathVariable Long boardId,
            @RequestBody CreateCardListRequest request
    ) {
        return ModelMapper.toCardListDto(cardListService.createCardList(user, boardId, request));
    }

    @GetMapping
    public List<CardListDto> getListsByBoard(
            @CurrentUser User user,
            @PathVariable Long boardId
    ) {
        return cardListService.getListsByBoard(user, boardId).stream()
                .map(ModelMapper::toCardListDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{listId}")
    public CardListDto getListById(
            @CurrentUser User user,
            @PathVariable Long boardId,
            @PathVariable Long listId
    ) {
        return ModelMapper.toCardListDto(cardListService.getListById(user, boardId, listId));
    }

    @PutMapping("/{listId}")
    public CardListDto updateCardList(
            @CurrentUser User user,
            @PathVariable Long boardId,
            @PathVariable Long listId,
            @RequestBody CreateCardListRequest request
    ) {
        return ModelMapper.toCardListDto(cardListService.updateCardList(user, boardId, listId, request));
    }

    @DeleteMapping("/{listId}")
    public void deleteCardList(
            @CurrentUser User user,
            @PathVariable Long boardId,
            @PathVariable Long listId
    ) {
        cardListService.deleteList(user, boardId, listId);
    }

    /**
     * Move a list to a (possibly different) board at a target position.
     *
     * <p>POST body: {@code { "targetBoardId": 456, "position": 1 }}
     *
     * <p>{@code boardId} in the path is the list's CURRENT (source) board.
     * Cards belonging to the list are NOT modified during the move.
     */
    @PatchMapping("/{listId}/move")
    public CardListDto moveCardList(
            @CurrentUser User user,
            @PathVariable Long boardId,
            @PathVariable Long listId,
            @RequestBody MoveCardListRequest request
    ) {
        return ModelMapper.toCardListDto(cardListService.moveCardList(user, boardId, listId, request));
    }
}
