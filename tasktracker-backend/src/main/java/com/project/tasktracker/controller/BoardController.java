package com.project.tasktracker.controller;

import com.project.tasktracker.dto.CreateBoardRequest;
import com.project.tasktracker.model.Board;
import com.project.tasktracker.service.BoardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/boards")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    public Board createBoard(@RequestBody CreateBoardRequest request){
        return boardService.createBoard(request.getName());
    }

    @GetMapping
    public List<Board> getBoards(){
        return boardService.getBoards();
    }

    @GetMapping("/{boardId}")
    public Board getBoardById(@PathVariable Long boardId){
        return boardService.getBoardById(boardId);
    }

    @PutMapping("/{boardId}")
    public Board updateBoard(
            @PathVariable Long boardId,
            @RequestBody CreateBoardRequest request
    ) {
        return boardService.updateBoard(boardId, request.getName());
    }

    @DeleteMapping("/{boardId}")
    public void deleteBoard(@PathVariable Long boardId) {
        boardService.deleteBoard(boardId);
    }
}
