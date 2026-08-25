package com.project.tasktracker.service;

import com.project.tasktracker.TaskTrackerApplication;
import com.project.tasktracker.error.ApiException;
import com.project.tasktracker.error.ErrorCode;
import com.project.tasktracker.model.Board;
import com.project.tasktracker.model.User;
import com.project.tasktracker.repository.BoardRepository;
import com.project.tasktracker.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public BoardService(BoardRepository boardRepository, UserRepository userRepository) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }

    private User getOrCreateUser(UUID userId) {
        if (userId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.USER_NOT_FOUND, "Missing X-Guest-User-Id header");
        }
        return userRepository.findById(userId).orElseGet(() -> {
            User newUser = new User(userId, "guest-" + userId + "@tasktracker.local");
            return userRepository.save(newUser);
        });
    }

    public Board createBoard(UUID userId, String name){
        User user = getOrCreateUser(userId);
        
        Board board = new Board();
        board.setName(name);
        board.setUser(user);
        return boardRepository.save(board);
    }

    public Board getBoardById(Long id){
        return boardRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.BOARD_NOT_FOUND, "Board not found"));
    }

    public List<Board> getBoards(UUID userId){
        if (userId == null) return List.of();
        return boardRepository.findByUser_Id(userId);
    }

    public Board updateBoard(Long id, String name) {
        Board board = getBoardById(id);
        if (name != null) {
            board.setName(name);
        }
        return boardRepository.save(board);
    }

    public void deleteBoard(Long id) {
        // Ensure board exists (throws 404 if not)
        getBoardById(id);
        
        // JPA Cascade handles the deletion of lists and cards
        boardRepository.deleteById(id);
    }
}
