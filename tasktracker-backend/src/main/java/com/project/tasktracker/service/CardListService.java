package com.project.tasktracker.service;
import com.project.tasktracker.dto.CreateCardListRequest;
import com.project.tasktracker.error.ApiException;
import com.project.tasktracker.error.ErrorCode;
import com.project.tasktracker.model.Board;
import com.project.tasktracker.model.CardList;
import com.project.tasktracker.repository.CardRepository;
import com.project.tasktracker.repository.CardListRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardListService {

    private final BoardService boardService;
    private final CardListRepository cardListRepository;
    private final CardRepository cardRepository;

    public CardListService(BoardService boardService, CardListRepository cardListRepository, CardRepository cardRepository) {
        this.boardService = boardService;
        this.cardListRepository = cardListRepository;
        this.cardRepository = cardRepository;
    }

    public CardList createCardList(Long boardId, CreateCardListRequest request) {
        boardService.getBoardById(boardId);
        CardList list = new CardList();
        list.setName(request.getName());
        list.setBoardId(boardId);
        return cardListRepository.save(list);
    }

    public List<CardList> getListsByBoard(Long boardId) {
        boardService.getBoardById(boardId);
        return cardListRepository.findByBoardId(boardId);
    }
    public CardList getListById(Long id) {
        return cardListRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.LIST_NOT_FOUND, "List not found"));
    }

    public CardList getListById(Long boardId, Long listId) {
        boardService.getBoardById(boardId);
        CardList list = getListById(listId);
        if (list.getBoardId() == null || !list.getBoardId().equals(boardId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, ErrorCode.LIST_NOT_FOUND, "List not found");
        }
        return list;
    }
    public void deleteList(Long id){
        // Ensure the list exists; also keeps error semantics consistent with the rest of the API.
        getListById(id);
        cardRepository.deleteByListId(id);
        cardListRepository.deleteById(id);
    }

    public CardList updateCardList(Long boardId, Long listId, CreateCardListRequest request) {
        boardService.getBoardById(boardId);
        CardList list = getListById(listId);
        if (list.getBoardId() == null || !list.getBoardId().equals(boardId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, ErrorCode.LIST_NOT_FOUND, "List not found");
        }

        if (request.getName() != null) {
            list.setName(request.getName());
        }
        return cardListRepository.save(list);
    }

    public void deleteList(Long boardId, Long listId) {
        boardService.getBoardById(boardId);
        CardList list = getListById(listId);
        if (list.getBoardId() == null || !list.getBoardId().equals(boardId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, ErrorCode.LIST_NOT_FOUND, "List not found");
        }

        cardRepository.deleteByListId(listId);
        cardListRepository.deleteById(listId);
    }
}
