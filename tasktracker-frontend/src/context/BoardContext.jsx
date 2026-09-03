import { useEffect, useReducer, useRef, useCallback } from 'react';
import * as api from '../api/taskApi';
import { BoardContext } from './boardContextDef';

const SIDEBAR_COLLAPSED_KEY = 'tasktracker_sidebar_collapsed';

const initialState = {
  boards: [],
  selectedBoardId: null,
  lists: [], // Array of { ...list, cards: [...] }
  loading: true,
  boardLoading: false,
  error: null,
  activeListId: null,
  isSidebarCollapsed: localStorage.getItem(SIDEBAR_COLLAPSED_KEY) === 'true',
  toasts: [],
  draggedItem: null,
};

function boardReducer(state, action) {
  switch (action.type) {
    case 'SET_BOARDS': {
      return {
        ...state,
        boards: action.payload,
        selectedBoardId: action.selectedBoardId !== undefined ? action.selectedBoardId : state.selectedBoardId,
        loading: false,
      };
    }
    case 'SET_BOARD_LOADING': {
      return { ...state, boardLoading: action.payload };
    }
    case 'SET_SELECTED_BOARD': {
      return {
        ...state,
        selectedBoardId: action.payload,
        activeListId: null,
      };
    }
    case 'SET_LISTS_AND_CARDS': {
      return {
        ...state,
        lists: action.payload,
        boardLoading: false,
        error: null,
      };
    }
    case 'SET_ACTIVE_LIST_ID': {
      return { ...state, activeListId: action.payload };
    }
    case 'SET_SIDEBAR_COLLAPSED': {
      localStorage.setItem(SIDEBAR_COLLAPSED_KEY, String(action.payload));
      return { ...state, isSidebarCollapsed: action.payload };
    }
    case 'SET_DRAGGED_ITEM': {
      return { ...state, draggedItem: action.payload };
    }
    case 'SET_ERROR': {
      return { ...state, error: action.payload, loading: false, boardLoading: false };
    }
    case 'ADD_TOAST': {
      return {
        ...state,
        toasts: [...state.toasts, action.payload],
      };
    }
    case 'REMOVE_TOAST': {
      return {
        ...state,
        toasts: state.toasts.filter((t) => t.id !== action.payload),
      };
    }
    case 'OPTIMISTIC_UPDATE_LISTS': {
      return {
        ...state,
        lists: action.payload,
      };
    }
    default:
      return state;
  }
}

export function BoardProvider({ children }) {
  const [state, dispatch] = useReducer(boardReducer, initialState);
  const stateRef = useRef(state);

  useEffect(() => {
    stateRef.current = state;
  }, [state]);

  // Track latest move operation to ignore stale out-of-order responses
  const lastMoveOpRef = useRef(0);

  const addToast = useCallback((message, type = 'info') => {
    const id = Date.now() + Math.random().toString(36).slice(2, 7);
    dispatch({ type: 'ADD_TOAST', payload: { id, message, type } });
    setTimeout(() => {
      dispatch({ type: 'REMOVE_TOAST', payload: id });
    }, 4000);
  }, []);

  const removeToast = useCallback((id) => {
    dispatch({ type: 'REMOVE_TOAST', payload: id });
  }, []);

  const toggleSidebar = useCallback(() => {
    dispatch({ type: 'SET_SIDEBAR_COLLAPSED', payload: !stateRef.current.isSidebarCollapsed });
  }, []);

  const setActiveListId = useCallback((id) => {
    dispatch({ type: 'SET_ACTIVE_LIST_ID', payload: id });
  }, []);

  const setDraggedItem = useCallback((item) => {
    dispatch({ type: 'SET_DRAGGED_ITEM', payload: item });
  }, []);

  // ── Data Fetching ─────────────────────────────────────────────────────────

  const refreshBoards = useCallback(async (preferredBoardId) => {
    try {
      const nextBoards = await api.getBoards();
      let targetId = null;

      if (nextBoards.length > 0) {
        if (preferredBoardId && nextBoards.some((b) => b.id === preferredBoardId)) {
          targetId = preferredBoardId;
        } else if (stateRef.current.selectedBoardId && nextBoards.some((b) => b.id === stateRef.current.selectedBoardId)) {
          targetId = stateRef.current.selectedBoardId;
        } else {
          targetId = nextBoards[0].id;
        }
      }

      dispatch({ type: 'SET_BOARDS', payload: nextBoards, selectedBoardId: targetId });
      return nextBoards;
    } catch (err) {
      if (err?.status === 401) {
        dispatch({ type: 'SET_BOARDS', payload: [], selectedBoardId: null });
      } else {
        dispatch({ type: 'SET_ERROR', payload: err?.message || 'Failed to load boards' });
      }
      throw err;
    }
  }, []);

  const loadBoardDetails = useCallback(async (boardId) => {
    if (!boardId) {
      dispatch({ type: 'SET_LISTS_AND_CARDS', payload: [] });
      return;
    }

    dispatch({ type: 'SET_BOARD_LOADING', payload: true });
    try {
      const fetchedLists = await api.getListsByBoard(boardId);
      // Sort lists by authoritative position
      const sortedLists = [...fetchedLists].sort((a, b) => (a.position ?? 0) - (b.position ?? 0));

      const listsWithCards = await Promise.all(
        sortedLists.map(async (list) => {
          const cards = await api.getCardsByList(list.id);
          // Sort cards by authoritative position
          const sortedCards = [...cards].sort((a, b) => (a.position ?? 0) - (b.position ?? 0));
          return {
            ...list,
            cards: sortedCards,
          };
        }),
      );

      dispatch({ type: 'SET_LISTS_AND_CARDS', payload: listsWithCards });
    } catch (err) {
      if (err?.status === 401) {
        dispatch({ type: 'SET_BOARDS', payload: [], selectedBoardId: null });
        dispatch({ type: 'SET_LISTS_AND_CARDS', payload: [] });
      } else {
        addToast(err?.message || 'Failed to load board details', 'error');
      }
    }
  }, [addToast]);

  const selectBoard = useCallback((boardId) => {
    if (boardId === stateRef.current.selectedBoardId) return;
    dispatch({ type: 'SET_SELECTED_BOARD', payload: boardId });
  }, []);

  // ── Board CRUD ────────────────────────────────────────────────────────────

  const createBoard = useCallback(async (name) => {
    try {
      const created = await api.createBoard(name.trim());
      await refreshBoards(created.id);
      addToast(`Board "${created.name}" created`, 'success');
      return created;
    } catch (err) {
      addToast(err?.message || 'Failed to create board', 'error');
      throw err;
    }
  }, [refreshBoards, addToast]);

  const updateBoard = useCallback(async (boardId, name) => {
    try {
      const updated = await api.updateBoard(boardId, name.trim());
      const nextBoards = stateRef.current.boards.map((b) => (b.id === boardId ? { ...b, name: updated.name } : b));
      dispatch({ type: 'SET_BOARDS', payload: nextBoards });
      addToast('Board renamed', 'success');
      return updated;
    } catch (err) {
      addToast(err?.message || 'Failed to rename board', 'error');
      throw err;
    }
  }, [addToast]);

  const deleteBoard = useCallback(async (boardId) => {
    try {
      await api.deleteBoard(boardId);
      const remainingBoards = stateRef.current.boards.filter((b) => b.id !== boardId);
      const nextSelected = remainingBoards.length > 0 ? remainingBoards[0].id : null;
      dispatch({ type: 'SET_BOARDS', payload: remainingBoards, selectedBoardId: nextSelected });
      addToast('Board deleted', 'info');
    } catch (err) {
      addToast(err?.message || 'Failed to delete board', 'error');
      throw err;
    }
  }, [addToast]);

  // ── List CRUD ─────────────────────────────────────────────────────────────

  const createList = useCallback(async (name) => {
    const currentBoardId = stateRef.current.selectedBoardId;
    if (!currentBoardId) return;
    try {
      const created = await api.createCardList(currentBoardId, name.trim());
      const newList = { ...created, cards: [] };
      dispatch({
        type: 'OPTIMISTIC_UPDATE_LISTS',
        payload: [...stateRef.current.lists, newList],
      });
      addToast(`List "${created.name}" created`, 'success');
      return created;
    } catch (err) {
      addToast(err?.message || 'Failed to create list', 'error');
      throw err;
    }
  }, [addToast]);

  const updateList = useCallback(async (listId, name) => {
    const currentBoardId = stateRef.current.selectedBoardId;
    if (!currentBoardId) return;
    try {
      const updated = await api.updateCardList(currentBoardId, listId, name.trim());
      const nextLists = stateRef.current.lists.map((l) => (l.id === listId ? { ...l, name: updated.name } : l));
      dispatch({ type: 'OPTIMISTIC_UPDATE_LISTS', payload: nextLists });
      addToast('List renamed', 'success');
      return updated;
    } catch (err) {
      addToast(err?.message || 'Failed to rename list', 'error');
      throw err;
    }
  }, [addToast]);

  const deleteList = useCallback(async (listId) => {
    const currentBoardId = stateRef.current.selectedBoardId;
    if (!currentBoardId) return;
    try {
      await api.deleteCardList(currentBoardId, listId);
      const nextLists = stateRef.current.lists
        .filter((l) => l.id !== listId)
        .map((l, idx) => ({ ...l, position: idx }));
      dispatch({ type: 'OPTIMISTIC_UPDATE_LISTS', payload: nextLists });
      addToast('List deleted', 'info');
    } catch (err) {
      addToast(err?.message || 'Failed to delete list', 'error');
      throw err;
    }
  }, [addToast]);

  // ── Card CRUD ─────────────────────────────────────────────────────────────

  const createCard = useCallback(async (listId, { name, description }) => {
    try {
      const created = await api.createCard(listId, {
        name: name.trim(),
        description: description?.trim() || '',
      });
      const nextLists = stateRef.current.lists.map((l) => {
        if (l.id === listId) {
          return {
            ...l,
            cards: [...l.cards, created],
          };
        }
        return l;
      });
      dispatch({ type: 'OPTIMISTIC_UPDATE_LISTS', payload: nextLists });
      addToast('Card added', 'success');
      return created;
    } catch (err) {
      addToast(err?.message || 'Failed to create card', 'error');
      throw err;
    }
  }, [addToast]);

  const updateCard = useCallback(async (listId, cardId, fields) => {
    try {
      const updated = await api.updateCard(listId, cardId, fields);
      const nextLists = stateRef.current.lists.map((l) => {
        if (l.id === listId) {
          return {
            ...l,
            cards: l.cards.map((c) => (c.id === cardId ? { ...c, ...updated } : c)),
          };
        }
        return l;
      });
      dispatch({ type: 'OPTIMISTIC_UPDATE_LISTS', payload: nextLists });
      return updated;
    } catch (err) {
      addToast(err?.message || 'Failed to update card', 'error');
      throw err;
    }
  }, [addToast]);

  const toggleCardCompletion = useCallback(async (listId, card) => {
    const newCompleted = !card.completed;
    const nextLists = stateRef.current.lists.map((l) => {
      if (l.id === listId) {
        return {
          ...l,
          cards: l.cards.map((c) => (c.id === card.id ? { ...c, completed: newCompleted } : c)),
        };
      }
      return l;
    });
    dispatch({ type: 'OPTIMISTIC_UPDATE_LISTS', payload: nextLists });

    try {
      await api.updateCard(listId, card.id, {
        name: card.name,
        description: card.description || '',
        completed: newCompleted,
      });
    } catch {
      const revertLists = stateRef.current.lists.map((l) => {
        if (l.id === listId) {
          return {
            ...l,
            cards: l.cards.map((c) => (c.id === card.id ? { ...c, completed: card.completed } : c)),
          };
        }
        return l;
      });
      dispatch({ type: 'OPTIMISTIC_UPDATE_LISTS', payload: revertLists });
      addToast('Failed to toggle card status', 'error');
    }
  }, [addToast]);

  const deleteCard = useCallback(async (listId, cardId) => {
    try {
      await api.deleteCard(listId, cardId);
      const nextLists = stateRef.current.lists.map((l) => {
        if (l.id === listId) {
          const remainingCards = l.cards
            .filter((c) => c.id !== cardId)
            .map((c, idx) => ({ ...c, position: idx }));
          return { ...l, cards: remainingCards };
        }
        return l;
      });
      dispatch({ type: 'OPTIMISTIC_UPDATE_LISTS', payload: nextLists });
      addToast('Card deleted', 'info');
    } catch (err) {
      addToast(err?.message || 'Failed to delete card', 'error');
      throw err;
    }
  }, [addToast]);

  // ── Drag & Drop Optimistic Movement ───────────────────────────────────────

  const reorderList = useCallback(async (sourceIndex, targetIndex) => {
    const currentBoardId = stateRef.current.selectedBoardId;
    if (sourceIndex === targetIndex || !currentBoardId) return;

    const snapshot = stateRef.current.lists;
    const reordered = [...stateRef.current.lists];
    const [movedList] = reordered.splice(sourceIndex, 1);
    reordered.splice(targetIndex, 0, movedList);

    const normalized = reordered.map((l, idx) => ({ ...l, position: idx }));
    dispatch({ type: 'OPTIMISTIC_UPDATE_LISTS', payload: normalized });

    const opId = ++lastMoveOpRef.current;

    try {
      const updated = await api.moveCardList(currentBoardId, movedList.id, {
        targetBoardId: currentBoardId,
        position: targetIndex,
      });

      if (opId !== lastMoveOpRef.current) return;

      const reconciled = normalized.map((l) => (l.id === movedList.id ? { ...l, position: updated.position } : l));
      dispatch({ type: 'OPTIMISTIC_UPDATE_LISTS', payload: reconciled });
    } catch (err) {
      if (opId === lastMoveOpRef.current) {
        dispatch({ type: 'OPTIMISTIC_UPDATE_LISTS', payload: snapshot });
        addToast(err?.message || 'Failed to reorder list', 'error');
      }
    }
  }, [addToast]);

  const moveCardItem = useCallback(async ({ cardId, sourceListId, targetListId, sourceIndex, targetIndex }) => {
    if (sourceListId === targetListId && sourceIndex === targetIndex) return;

    const snapshot = stateRef.current.lists;
    let movedCard = null;
    let nextLists;

    if (sourceListId === targetListId) {
      nextLists = stateRef.current.lists.map((l) => {
        if (l.id === sourceListId) {
          const cards = [...l.cards];
          [movedCard] = cards.splice(sourceIndex, 1);
          cards.splice(targetIndex, 0, movedCard);
          const normalizedCards = cards.map((c, idx) => ({ ...c, position: idx }));
          return { ...l, cards: normalizedCards };
        }
        return l;
      });
    } else {
      const sourceList = stateRef.current.lists.find((l) => l.id === sourceListId);
      if (!sourceList) return;
      movedCard = sourceList.cards[sourceIndex] || sourceList.cards.find((c) => c.id === cardId);
      if (!movedCard) return;

      nextLists = stateRef.current.lists.map((l) => {
        if (l.id === sourceListId) {
          const cards = l.cards.filter((c) => c.id !== cardId).map((c, idx) => ({ ...c, position: idx }));
          return { ...l, cards };
        }
        if (l.id === targetListId) {
          const cards = [...l.cards];
          const newCard = { ...movedCard, cardList: { id: targetListId } };
          const safeTargetIndex = Math.min(targetIndex, cards.length);
          cards.splice(safeTargetIndex, 0, newCard);
          const normalizedCards = cards.map((c, idx) => ({ ...c, position: idx }));
          return { ...l, cards: normalizedCards };
        }
        return l;
      });
    }

    dispatch({ type: 'OPTIMISTIC_UPDATE_LISTS', payload: nextLists });

    const opId = ++lastMoveOpRef.current;

    try {
      const updated = await api.moveCard(sourceListId, cardId, {
        targetListId,
        position: targetIndex,
      });

      if (opId !== lastMoveOpRef.current) return;

      const reconciled = stateRef.current.lists.map((l) => {
        if (l.id === targetListId) {
          return {
            ...l,
            cards: l.cards.map((c) => (c.id === cardId ? { ...c, position: updated.position } : c)),
          };
        }
        return l;
      });
      dispatch({ type: 'OPTIMISTIC_UPDATE_LISTS', payload: reconciled });
    } catch (err) {
      if (opId === lastMoveOpRef.current) {
        dispatch({ type: 'OPTIMISTIC_UPDATE_LISTS', payload: snapshot });
        addToast(err?.message || 'Failed to move card', 'error');
      }
    }
  }, [addToast]);

  // ── Lifecycle Effects ─────────────────────────────────────────────────────

  useEffect(() => {
    refreshBoards().catch((err) => {
      dispatch({ type: 'SET_ERROR', payload: err?.message || 'Failed to initialize session' });
    });
  }, [refreshBoards]);

  useEffect(() => {
    if (state.selectedBoardId != null) {
      loadBoardDetails(state.selectedBoardId);
    }
  }, [state.selectedBoardId, loadBoardDetails]);

  const value = {
    ...state,
    selectedBoard: state.boards.find((b) => b.id === state.selectedBoardId) || null,
    selectBoard,
    refreshBoards,
    loadBoardDetails,
    createBoard,
    updateBoard,
    deleteBoard,
    createList,
    updateList,
    deleteList,
    createCard,
    updateCard,
    toggleCardCompletion,
    deleteCard,
    reorderList,
    moveCardItem,
    addToast,
    removeToast,
    toggleSidebar,
    setActiveListId,
    setDraggedItem,
  };

  return <BoardContext.Provider value={value}>{children}</BoardContext.Provider>;
}
