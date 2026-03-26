const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080';

function unwrapApiResponse(payload) {
  // Backend wraps successful responses in:
  // { timestamp, status, data, error }
  if (
    payload &&
    typeof payload === 'object' &&
    Object.prototype.hasOwnProperty.call(payload, 'data') &&
    Object.prototype.hasOwnProperty.call(payload, 'status')
  ) {
    return payload.data;
  }
  return payload;
}

export async function apiRequest(path, options = {}) {
  const url = `${API_BASE}${path}`;
  const res = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
  });

  const isJson = (res.headers.get('content-type') || '').includes('application/json');
  const payload = isJson ? await res.json() : await res.text();

  if (!res.ok) {
    const message =
      payload && typeof payload === 'object' && payload.message
        ? payload.message
        : `Request failed with status ${res.status}`;
    const err = new Error(message);
    err.status = res.status;
    err.payload = payload;
    throw err;
  }

  return unwrapApiResponse(payload);
}

