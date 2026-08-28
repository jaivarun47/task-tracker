package com.project.tasktracker.service;

import com.project.tasktracker.dto.CreateCardListRequest;
import com.project.tasktracker.error.ApiException;
import com.project.tasktracker.error.ErrorCode;
import com.project.tasktracker.model.Board;
import com.project.tasktracker.model.CardList;
import com.project.tasktracker.model.User;
import com.project.tasktracker.repository.CardListRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CardListService {

    private final BoardService boardService;
    private final CardListRepository cardListRepository;

    public CardListService(BoardService boardService, CardListRepository cardListRepository) {
        this.boardService = boardService;
        this.cardListRepository = cardListRepository;
    }

    public CardList createCardList(User user, Long boardId, CreateCardListRequest request) {
        Board board = boardService.getBoardById(user, boardId);
        CardList list = new CardList();
        list.setName(request.getName());
        list.setBoard(board);
        return cardListRepository.save(list);
    }

    public List<CardList> getListsByBoard(User user, Long boardId) {
        boardService.getBoardById(user, boardId);
        return cardListRepository.findByBoard_Id(boardId);
    }

    public CardList getListById(User user, Long boardId, Long listId) {
        boardService.getBoardById(user, boardId);
        CardList list = cardListRepository.findById(listId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.LIST_NOT_FOUND, "List not found"));

        if (list.getBoard() == null || !list.getBoard().getId().equals(boardId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, ErrorCode.LIST_NOT_FOUND, "List not found");
        }

        return list;
    }

    public CardList updateCardList(User user, Long boardId, Long listId, CreateCardListRequest request) {
        CardList list = getListById(user, boardId, listId);
        if (request.getName() != null) {
            list.setName(request.getName());
        }
        return cardListRepository.save(list);
    }

    public void deleteList(User user, Long boardId, Long listId) {
        getListById(user, boardId, listId);
        // JPA Cascade handles the deletion of cards
        cardListRepository.deleteById(listId);
    }
}
