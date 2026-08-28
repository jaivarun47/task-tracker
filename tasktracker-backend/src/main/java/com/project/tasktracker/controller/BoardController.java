package com.project.tasktracker.controller;

import com.project.tasktracker.dto.BoardDto;
import com.project.tasktracker.dto.CreateBoardRequest;
import com.project.tasktracker.mapper.ModelMapper;
import com.project.tasktracker.model.User;
import com.project.tasktracker.security.CurrentUser;
import com.project.tasktracker.service.BoardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/boards")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    public BoardDto createBoard(
            @CurrentUser User user,
            @RequestBody CreateBoardRequest request
    ) {
        return ModelMapper.toBoardDto(boardService.createBoard(user, request.getName()));
    }

    @GetMapping
    public List<BoardDto> getBoards(@CurrentUser User user) {
        return boardService.getBoards(user).stream()
                .map(ModelMapper::toBoardDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{boardId}")
    public BoardDto getBoardById(
            @CurrentUser User user,
            @PathVariable Long boardId
    ) {
        return ModelMapper.toBoardDto(boardService.getBoardById(user, boardId));
    }

    @PutMapping("/{boardId}")
    public BoardDto updateBoard(
            @CurrentUser User user,
            @PathVariable Long boardId,
            @RequestBody CreateBoardRequest request
    ) {
        return ModelMapper.toBoardDto(boardService.updateBoard(user, boardId, request.getName()));
    }

    @DeleteMapping("/{boardId}")
    public void deleteBoard(
            @CurrentUser User user,
            @PathVariable Long boardId
    ) {
        boardService.deleteBoard(user, boardId);
    }
}
