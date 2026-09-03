import { apiRequest } from './apiClient';
import { hasToken, setToken } from './sessionManager';

export { hasToken, clearToken } from './sessionManager';

export async function createSession() {
  const res = await apiRequest('/api/sessions', { method: 'POST' });
  if (res && res.token) {
    setToken(res.token);
  }
  return res;
}

export async function ensureSession() {
  if (!hasToken()) {
    await createSession();
  }
}

export async function getBoards() {
  if (!hasToken()) {
    return [];
  }
  return apiRequest('/api/boards', { method: 'GET' });
}

export async function createBoard(name) {
  await ensureSession();
  return apiRequest('/api/boards', { method: 'POST', body: JSON.stringify({ name }) });
}

export async function updateBoard(boardId, name) {
  return apiRequest(`/api/boards/${boardId}`, {
    method: 'PUT',
    body: JSON.stringify({ name }),
  });
}

export async function deleteBoard(boardId) {
  return apiRequest(`/api/boards/${boardId}`, { method: 'DELETE' });
}

export async function getListsByBoard(boardId) {
  return apiRequest(`/api/boards/${boardId}/lists`, { method: 'GET' });
}

export async function createCardList(boardId, name) {
  return apiRequest(`/api/boards/${boardId}/lists`, {
    method: 'POST',
    body: JSON.stringify({ name }),
  });
}

export async function updateCardList(boardId, listId, name) {
  return apiRequest(`/api/boards/${boardId}/lists/${listId}`, {
    method: 'PUT',
    body: JSON.stringify({ name }),
  });
}

export async function deleteCardList(boardId, listId) {
  return apiRequest(`/api/boards/${boardId}/lists/${listId}`, { method: 'DELETE' });
}

/**
 * Move a CardList within a board (reorder) or across boards.
 *
 * @param {number} boardId - Current (source) board ID
 * @param {number} listId - ID of the CardList to move
 * @param {Object} options
 * @param {number} options.targetBoardId - Destination board ID (same for reorder)
 * @param {number} options.position - Zero-based target position index
 * @returns {Promise<Object>} Updated CardListDto from backend
 */
export async function moveCardList(boardId, listId, { targetBoardId, position }) {
  return apiRequest(`/api/boards/${boardId}/lists/${listId}/move`, {
    method: 'PATCH',
    body: JSON.stringify({ targetBoardId, position }),
  });
}

export async function getCardsByList(listId) {
  return apiRequest(`/api/lists/${listId}/cards`, { method: 'GET' });
}

export async function createCard(listId, { name, description }) {
  return apiRequest(`/api/lists/${listId}/cards`, {
    method: 'POST',
    body: JSON.stringify({ name, description }),
  });
}

export async function updateCard(listId, cardId, { name, description, completed }) {
  return apiRequest(`/api/lists/${listId}/cards/${cardId}`, {
    method: 'PUT',
    body: JSON.stringify({ name, description, completed }),
  });
}

export async function deleteCard(listId, cardId) {
  return apiRequest(`/api/lists/${listId}/cards/${cardId}`, { method: 'DELETE' });
}

/**
 * Move a Card within a list (reorder) or across lists (and boards).
 *
 * @param {number} listId - Current (source) list ID
 * @param {number} cardId - ID of the Card to move
 * @param {Object} options
 * @param {number} options.targetListId - Destination list ID (same for reorder)
 * @param {number} options.position - Zero-based target position index
 * @returns {Promise<Object>} Updated CardDto from backend
 */
export async function moveCard(listId, cardId, { targetListId, position }) {
  return apiRequest(`/api/lists/${listId}/cards/${cardId}/move`, {
    method: 'PATCH',
    body: JSON.stringify({ targetListId, position }),
  });
}

export async function getCardById(listId, cardId) {
  return apiRequest(`/api/lists/${listId}/cards/${cardId}`, { method: 'GET' });
}
