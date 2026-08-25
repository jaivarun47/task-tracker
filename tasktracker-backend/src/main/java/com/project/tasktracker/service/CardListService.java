package com.project.tasktracker.service;
import com.project.tasktracker.dto.CreateCardListRequest;
import com.project.tasktracker.error.ApiException;
import com.project.tasktracker.error.ErrorCode;
import com.project.tasktracker.model.Board;
import com.project.tasktracker.model.CardList;
import com.project.tasktracker.repository.CardListRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CardListService {

    private final BoardService boardService;
    private final CardListRepository cardListRepository;

    public CardListService(BoardService boardService, CardListRepository cardListRepository) {
        this.boardService = boardService;
        this.cardListRepository = cardListRepository;
    }

    public CardList createCardList(Long boardId, CreateCardListRequest request) {
        Board board = boardService.getBoardById(boardId);
        CardList list = new CardList();
        list.setName(request.getName());
        list.setBoard(board);
        return cardListRepository.save(list);
    }

    public List<CardList> getListsByBoard(Long boardId) {
        boardService.getBoardById(boardId);
        return cardListRepository.findByBoard_Id(boardId);
    }
    
    public CardList getListById(Long id) {
        return cardListRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.LIST_NOT_FOUND, "List not found"));
    }

    public CardList getListById(Long boardId, Long listId) {
        boardService.getBoardById(boardId);
        CardList list = getListById(listId);
        if (list.getBoard() == null || !list.getBoard().getId().equals(boardId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, ErrorCode.LIST_NOT_FOUND, "List not found");
        }
        return list;
    }

    public CardList updateCardList(Long boardId, Long listId, CreateCardListRequest request) {
        boardService.getBoardById(boardId);
        CardList list = getListById(listId);
        if (list.getBoard() == null || !list.getBoard().getId().equals(boardId)) {
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
        if (list.getBoard() == null || !list.getBoard().getId().equals(boardId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, ErrorCode.LIST_NOT_FOUND, "List not found");
        }

        // JPA Cascade handles the deletion of cards
        cardListRepository.deleteById(listId);
    }
}
