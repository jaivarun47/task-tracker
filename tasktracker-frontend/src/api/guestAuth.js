/**
 * @deprecated This module has been retired. Identity and authentication are now
 * managed server-side via server-issued session tokens in `sessionManager.js`.
 */
export function getGuestId() {
  throw new Error('guestAuth.js is deprecated. Use sessionManager.js instead.');
}
