package com.project.tasktracker.controller;

import com.project.tasktracker.dto.CardListDto;
import com.project.tasktracker.dto.CreateCardListRequest;
import com.project.tasktracker.mapper.ModelMapper;
import com.project.tasktracker.service.CardListService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/boards/{boardId}/lists")
public class CardListController {

    private final CardListService cardListService;

    public CardListController(CardListService cardListService){
        this.cardListService = cardListService;
    }

    @PostMapping
    public CardListDto createCardList(
            @PathVariable Long boardId,
            @RequestBody CreateCardListRequest request){

        return ModelMapper.toCardListDto(cardListService.createCardList(boardId, request));
    }

    @GetMapping
    public List<CardListDto> getListsByBoard(@PathVariable Long boardId){
        return cardListService.getListsByBoard(boardId).stream()
                .map(ModelMapper::toCardListDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{listId}")
    public CardListDto getListById(
            @PathVariable Long boardId,
            @PathVariable Long listId
    ) {
        return ModelMapper.toCardListDto(cardListService.getListById(boardId, listId));
    }

    @PutMapping("/{listId}")
    public CardListDto updateCardList(
            @PathVariable Long boardId,
            @PathVariable Long listId,
            @RequestBody CreateCardListRequest request
    ) {
        return ModelMapper.toCardListDto(cardListService.updateCardList(boardId, listId, request));
    }

    @DeleteMapping("/{listId}")
    public void deleteCardList(
            @PathVariable Long boardId,
            @PathVariable Long listId
    ) {
        cardListService.deleteList(boardId, listId);
    }
}
