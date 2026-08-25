package com.project.tasktracker.controller;

import com.project.tasktracker.dto.BoardDto;
import com.project.tasktracker.dto.CreateBoardRequest;
import com.project.tasktracker.mapper.ModelMapper;
import com.project.tasktracker.service.BoardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/boards")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    public BoardDto createBoard(
            @RequestHeader(value = "X-Guest-User-Id", required = false) UUID userId,
            @RequestBody CreateBoardRequest request
    ) {
        return ModelMapper.toBoardDto(boardService.createBoard(userId, request.getName()));
    }

    @GetMapping
    public List<BoardDto> getBoards(@RequestHeader(value = "X-Guest-User-Id", required = false) UUID userId){
        return boardService.getBoards(userId).stream()
                .map(ModelMapper::toBoardDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{boardId}")
    public BoardDto getBoardById(@PathVariable Long boardId){
        return ModelMapper.toBoardDto(boardService.getBoardById(boardId));
    }

    @PutMapping("/{boardId}")
    public BoardDto updateBoard(
            @PathVariable Long boardId,
            @RequestBody CreateBoardRequest request
    ) {
        return ModelMapper.toBoardDto(boardService.updateBoard(boardId, request.getName()));
    }

    @DeleteMapping("/{boardId}")
    public void deleteBoard(@PathVariable Long boardId) {
        boardService.deleteBoard(boardId);
    }
}
