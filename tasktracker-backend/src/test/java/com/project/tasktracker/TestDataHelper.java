package com.project.tasktracker;

import com.project.tasktracker.dto.CreateCardListRequest;
import com.project.tasktracker.dto.CreateCardRequest;
import com.project.tasktracker.model.Board;
import com.project.tasktracker.model.Card;
import com.project.tasktracker.model.CardList;
import com.project.tasktracker.model.User;
import com.project.tasktracker.repository.BoardRepository;
import com.project.tasktracker.repository.CardListRepository;
import com.project.tasktracker.repository.CardRepository;
import com.project.tasktracker.repository.UserRepository;
import com.project.tasktracker.service.CardListService;
import com.project.tasktracker.service.CardService;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Factory helpers shared across integration test classes.
 * Not a Spring component — instantiated directly in test classes.
 */
public class TestDataHelper {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CardListRepository cardListRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;
    private final CardListService cardListService;

    public TestDataHelper(
            UserRepository userRepository,
            BoardRepository boardRepository,
            CardListRepository cardListRepository,
            CardRepository cardRepository,
            CardService cardService,
            CardListService cardListService) {
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
        this.cardListRepository = cardListRepository;
        this.cardRepository = cardRepository;
        this.cardService = cardService;
        this.cardListService = cardListService;
    }

    public User createUser() {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setEmail("test-" + UUID.randomUUID() + "@example.com");
        return userRepository.save(u);
    }

    public Board createBoard(User user, String name) {
        Board b = new Board();
        b.setName(name);
        b.setUser(user);
        return boardRepository.save(b);
    }

    public CardList createList(User user, Long boardId, String name) {
        CreateCardListRequest req = new CreateCardListRequest();
        req.setName(name);
        return cardListService.createCardList(user, boardId, req);
    }

    public Card createCard(User user, Long listId, String name) {
        CreateCardRequest req = new CreateCardRequest();
        req.setName(name);
        req.setDescription("desc-" + name);
        return cardService.createCard(user, listId, req);
    }

    /** Fetch positions of all cards in a list, ordered by position. */
    public java.util.List<Integer> cardPositions(Long listId) {
        return cardRepository.findByCardList_IdOrderByPositionAsc(listId)
                .stream().map(Card::getPosition).toList();
    }

    /** Fetch positions of all lists in a board, ordered by position. */
    public java.util.List<Integer> listPositions(Long boardId) {
        return cardListRepository.findByBoard_IdOrderByPositionAsc(boardId)
                .stream().map(CardList::getPosition).toList();
    }

    /** Assert the given position sequence equals exactly [0, 1, …, N-1]. */
    public static void assertContiguous(java.util.List<Integer> positions, int n) {
        assert positions.size() == n
                : "Expected " + n + " positions but got " + positions.size();
        for (int i = 0; i < n; i++) {
            assert positions.get(i) == i
                    : "Position " + i + " expected but got " + positions.get(i) + " in " + positions;
        }
    }
}
