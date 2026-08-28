const SESSION_TOKEN_KEY = 'tasktracker_session_token';

export function getToken() {
  return localStorage.getItem(SESSION_TOKEN_KEY);
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(SESSION_TOKEN_KEY, token);
  }
}

export function clearToken() {
  localStorage.removeItem(SESSION_TOKEN_KEY);
}

export function hasToken() {
  const token = getToken();
  return Boolean(token && token.trim().length > 0);
}
