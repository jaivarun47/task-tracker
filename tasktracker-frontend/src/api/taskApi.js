import { apiRequest } from './apiClient';

export async function getBoards() {
  return apiRequest('/api/boards', { method: 'GET' });
}

export async function createBoard(name) {
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

export async function getCardById(listId, cardId) {
  return apiRequest(`/api/lists/${listId}/cards/${cardId}`, { method: 'GET' });
}

