package com.project.tasktracker.service;

import com.project.tasktracker.error.ApiException;
import com.project.tasktracker.error.ErrorCode;
import com.project.tasktracker.model.Board;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BoardService {
    private final Map<Long, Board> boardStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Board createBoard(String name){
        Long id = idGenerator.getAndIncrement();
        Board board = new Board(id, name);
        boardStore.put(id, board);
        return board;
    }

    public Board getBoardById(Long id){
        Board board = boardStore.get(id);
        if (board == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, ErrorCode.BOARD_NOT_FOUND, "Board not found");
        }
        return board;
    }

    public List<Board> getBoards(){
        return new ArrayList<>(boardStore.values());
    }
}
