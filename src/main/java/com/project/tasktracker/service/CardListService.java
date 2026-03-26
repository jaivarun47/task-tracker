package com.project.tasktracker.service;
import com.project.tasktracker.dto.CreateCardListRequest;
import com.project.tasktracker.error.ApiException;
import com.project.tasktracker.error.ErrorCode;
import com.project.tasktracker.model.Board;
import com.project.tasktracker.model.CardList;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CardListService {

    private final Map<Long, CardList> lists = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final BoardService boardService;

    public CardListService(BoardService boardService) {
        this.boardService = boardService;
    }

    public CardList createCardList(Long boardId, CreateCardListRequest request) {
        boardService.getBoardById(boardId);
        CardList list = new CardList();
        list.setId(idGenerator.getAndIncrement());
        list.setName(request.getName());
        list.setBoardId(boardId);
        lists.put(list.getId(), list);
        return list;
    }

    public List<CardList> getListsByBoard(Long boardId) {
        boardService.getBoardById(boardId);
        List<CardList> result = new ArrayList<>();

        for (CardList list : lists.values()) {
            if (Objects.equals(list.getBoardId(), boardId)) {
                result.add(list);
            }
        }
        return result;
    }
    public CardList getListById(Long id) {

        CardList list = lists.get(id);

        if (list == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, ErrorCode.LIST_NOT_FOUND, "List not found");
        }
    
        return list;
    }
    public void deleteList(Long id){
        lists.remove(id);
    }
}
