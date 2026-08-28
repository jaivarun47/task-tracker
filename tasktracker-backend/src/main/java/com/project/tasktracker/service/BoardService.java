package com.project.tasktracker.service;

import com.project.tasktracker.error.ApiException;
import com.project.tasktracker.error.ErrorCode;
import com.project.tasktracker.model.Board;
import com.project.tasktracker.model.User;
import com.project.tasktracker.repository.BoardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public Board createBoard(User user, String name) {
        Board board = new Board();
        board.setName(name);
        board.setUser(user);
        return boardRepository.save(board);
    }

    public Board getBoardById(User user, Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.BOARD_NOT_FOUND, "Board not found"));

        if (board.getUser() == null || !board.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, ErrorCode.BOARD_NOT_FOUND, "Board not found");
        }

        return board;
    }

    public List<Board> getBoards(User user) {
        if (user == null) return List.of();
        return boardRepository.findByUser_Id(user.getId());
    }

    public Board updateBoard(User user, Long id, String name) {
        Board board = getBoardById(user, id);
        if (name != null) {
            board.setName(name);
        }
        return boardRepository.save(board);
    }

    public void deleteBoard(User user, Long id) {
        // Ensure board exists and belongs to authenticated user (throws 404 if not)
        getBoardById(user, id);
        // JPA Cascade handles the deletion of lists and cards
        boardRepository.deleteById(id);
    }
}
