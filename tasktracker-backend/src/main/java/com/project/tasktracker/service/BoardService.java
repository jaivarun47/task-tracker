package com.project.tasktracker.service;

import com.project.tasktracker.error.ApiException;
import com.project.tasktracker.error.ErrorCode;
import com.project.tasktracker.model.Board;
import com.project.tasktracker.model.CardList;
import com.project.tasktracker.repository.BoardRepository;
import com.project.tasktracker.repository.CardListRepository;
import com.project.tasktracker.repository.CardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final CardListRepository cardListRepository;
    private final CardRepository cardRepository;

    public BoardService(BoardRepository boardRepository, CardListRepository cardListRepository, CardRepository cardRepository) {
        this.boardRepository = boardRepository;
        this.cardListRepository = cardListRepository;
        this.cardRepository = cardRepository;
    }

    public Board createBoard(String name){
        Board board = new Board();
        board.setName(name);
        return boardRepository.save(board);
    }

    public Board getBoardById(Long id){
        return boardRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.BOARD_NOT_FOUND, "Board not found"));
    }

    public List<Board> getBoards(){
        return boardRepository.findAll();
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

        // Manual cascade because models store foreign keys as scalar ids (no JPA relationships)
        List<CardList> lists = cardListRepository.findByBoardId(id);
        for (CardList list : lists) {
            cardRepository.deleteByListId(list.getId());
        }
        cardListRepository.deleteAll(lists);
        boardRepository.deleteById(id);
    }
}
