import { useEffect, useMemo, useState } from 'react';
import './App.css';
import Modal from './components/Modal.jsx';
import * as api from './api/taskApi';

const COLUMN_BACKGROUNDS = [
  'rgba(17, 24, 39, 0.16)',
  'rgba(59, 130, 246, 0.16)',
  'rgba(16, 185, 129, 0.14)',
  'rgba(245, 158, 11, 0.16)',
  'rgba(168, 85, 247, 0.16)',
];

function App() {
  const [boards, setBoards] = useState([]);
  const [selectedBoardId, setSelectedBoardId] = useState(null);

  const [lists, setLists] = useState([]);
  const [cardsByListId, setCardsByListId] = useState({});

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Modals
  const [showCreateBoard, setShowCreateBoard] = useState(false);
  const [newBoardName, setNewBoardName] = useState('');
  const [showEditBoard, setShowEditBoard] = useState(false);
  const [editBoardName, setEditBoardName] = useState('');

  const [showCreateList, setShowCreateList] = useState(false);
  const [newListName, setNewListName] = useState('');

  const [cardModalOpen, setCardModalOpen] = useState(false);
  const [cardModalMode, setCardModalMode] = useState('create'); // 'create' | 'edit'
  const [cardModalListId, setCardModalListId] = useState(null);
  const [cardModalCard, setCardModalCard] = useState(null);
  const [cardName, setCardName] = useState('');
  const [cardDescription, setCardDescription] = useState('');
  const [cardCompleted, setCardCompleted] = useState(false);

  const [showEditList, setShowEditList] = useState(false);
  const [editListId, setEditListId] = useState(null);
  const [editListName, setEditListName] = useState('');

  const selectedBoard = useMemo(
    () => boards.find((b) => b.id === selectedBoardId) || null,
    [boards, selectedBoardId],
  );

  async function refreshBoards() {
    const nextBoards = await api.getBoards();
    setBoards(nextBoards);
    if (nextBoards.length > 0) {
      setSelectedBoardId((prev) => prev ?? nextBoards[0].id);
    } else {
      setSelectedBoardId(null);
    }
  }

  async function refreshBoard(boardId) {
    setLoading(true);
    try {
      const nextLists = await api.getListsByBoard(boardId);
      setLists(nextLists);

      const pairs = await Promise.all(
        nextLists.map(async (l) => {
          const nextCards = await api.getCardsByList(l.id);
          return [l.id, nextCards];
        }),
      );
      setCardsByListId(Object.fromEntries(pairs));
      setError(null);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    refreshBoards().catch((e) => {
      setError(e?.message || 'Failed to load boards');
      setLoading(false);
    });
  }, []);

  useEffect(() => {
    if (selectedBoardId == null) return;
    refreshBoard(selectedBoardId).catch((e) => setError(e?.message || 'Failed to load board'));
  }, [selectedBoardId]);

  function openCreateCard(listId) {
    setCardModalMode('create');
    setCardModalListId(listId);
    setCardModalCard(null);
    setCardName('');
    setCardDescription('');
    setCardCompleted(false);
    setCardModalOpen(true);
  }

  function openEditCard(listId, card) {
    setCardModalMode('edit');
    setCardModalListId(listId);
    setCardModalCard(card);
    setCardName(card?.name || '');
    setCardDescription(card?.description || '');
    setCardCompleted(Boolean(card?.completed));
    setCardModalOpen(true);
  }

  function openEditList(list) {
    setShowEditList(true);
    setEditListId(list.id);
    setEditListName(list.name || '');
  }

  async function handleCreateBoard() {
    const name = newBoardName.trim();
    if (!name) return;
    await api.createBoard(name);
    setShowCreateBoard(false);
    setNewBoardName('');
    await refreshBoards();
  }

  async function handleUpdateBoard() {
    if (!selectedBoardId) return;
    const name = editBoardName.trim();
    if (!name) return;
    await api.updateBoard(selectedBoardId, name);
    setShowEditBoard(false);
    setEditBoardName('');
    await refreshBoards();
    await refreshBoard(selectedBoardId);
  }

  async function handleDeleteBoard() {
    if (!selectedBoardId) return;
    if (!window.confirm('Delete this board and all its lists/cards?')) return;
    await api.deleteBoard(selectedBoardId);
    setShowEditBoard(false);
    await refreshBoards();
  }

  async function handleCreateList() {
    if (!selectedBoardId) return;
    const name = newListName.trim();
    if (!name) return;
    await api.createCardList(selectedBoardId, name);
    setShowCreateList(false);
    setNewListName('');
    await refreshBoard(selectedBoardId);
  }

  async function handleUpdateList() {
    if (!selectedBoardId || !editListId) return;
    const name = editListName.trim();
    if (!name) return;
    await api.updateCardList(selectedBoardId, editListId, name);
    setShowEditList(false);
    setEditListId(null);
    setEditListName('');
    await refreshBoard(selectedBoardId);
  }

  async function handleDeleteList(listId) {
    if (!selectedBoardId) return;
    if (!window.confirm('Delete this list and all its cards?')) return;
    await api.deleteCardList(selectedBoardId, listId);
    await refreshBoard(selectedBoardId);
  }

  async function handleCreateOrUpdateCard() {
    if (!cardModalListId) return;
    const name = cardName.trim();
    if (!name) return;

    const payload = {
      name,
      // Use empty-string (not null) so backend update can treat it as "blank" and apply its default.
      description: cardDescription.trim(),
      completed: cardModalMode === 'edit' ? cardCompleted : false,
    };

    if (cardModalMode === 'create') {
      await api.createCard(cardModalListId, payload);
    } else {
      await api.updateCard(cardModalListId, cardModalCard.id, payload);
    }

    setCardModalOpen(false);
    setCardModalCard(null);
    setCardModalListId(null);
    await refreshBoard(selectedBoardId);
  }

  async function handleToggleCardCompleted(listId, card) {
    await api.updateCard(listId, card.id, {
      name: card.name,
      description: card.description,
      completed: !card.completed,
    });
    await refreshBoard(selectedBoardId);
  }

  async function handleDeleteCard(listId, cardId) {
    if (!window.confirm('Delete this card?')) return;
    await api.deleteCard(listId, cardId);
    await refreshBoard(selectedBoardId);
  }

  return (
    <div className="tt-root">
      <div className="tt-shell">
        <aside className="tt-sidebar">
          <div className="tt-sidebar-header">
            <div className="tt-brand">TaskTracker</div>
            <button
              type="button"
              className="btn btn-sm btn-primary"
              onClick={() => {
                setShowCreateBoard(true);
                setNewBoardName('');
              }}
            >
              Create
            </button>
          </div>

          <div className="tt-sidebar-section">
            <div className="tt-sidebar-title">Boards</div>
            <div className="tt-boards-list" role="list">
              {boards.map((b) => (
                <button
                  key={b.id}
                  type="button"
                  className={`tt-board-item ${b.id === selectedBoardId ? 'active' : ''}`}
                  onClick={() => setSelectedBoardId(b.id)}
                >
                  {b.name}
                </button>
              ))}
              {boards.length === 0 ? <div className="tt-muted">No boards yet. Create one.</div> : null}
            </div>
          </div>

          {selectedBoard ? (
            <div className="tt-sidebar-footer">
              <div className="tt-sidebar-footer-row">
                <span className="tt-muted">Selected</span>
                <span className="tt-sidebar-selected">{selectedBoard.name}</span>
              </div>
              <div className="tt-sidebar-footer-actions">
                <button
                  type="button"
                  className="btn btn-sm btn-outline-light w-100"
                  onClick={() => {
                    setShowEditBoard(true);
                    setEditBoardName(selectedBoard.name || '');
                  }}
                >
                  Edit board
                </button>
              </div>
            </div>
          ) : null}
        </aside>

        <main className="tt-main">
          <div className="tt-main-header">
            <div>
              <div className="tt-main-title">
                {selectedBoard ? selectedBoard.name : 'Select a board'}
              </div>
              <div className="tt-main-subtitle">{loading ? 'Loading…' : 'Manage lists and cards'}</div>
            </div>
            {selectedBoard ? (
              <button
                type="button"
                className="btn btn-sm btn-primary"
                onClick={() => {
                  setShowCreateList(true);
                  setNewListName('');
                }}
              >
                + Add another list
              </button>
            ) : null}
          </div>

          {error ? <div className="alert alert-danger tt-alert">{error}</div> : null}

          {!selectedBoardId ? (
            <div className="tt-empty">Create a board to get started.</div>
          ) : (
            <div className="tt-columns" aria-busy={loading}>
              {lists.map((list, idx) => (
                <section
                  key={list.id}
                  className="tt-column"
                  style={{ background: COLUMN_BACKGROUNDS[idx % COLUMN_BACKGROUNDS.length] }}
                >
                  <div className="tt-col-header">
                    <div className="tt-col-title">{list.name}</div>
                    <div className="tt-col-actions">
                      <button
                        type="button"
                        className="btn btn-sm btn-link tt-icon-btn"
                        aria-label="Edit list"
                        onClick={() => openEditList(list)}
                      >
                        ✎
                      </button>
                      <button
                        type="button"
                        className="btn btn-sm btn-link tt-icon-btn"
                        aria-label="Delete list"
                        onClick={() => handleDeleteList(list.id)}
                      >
                        ⓧ
                      </button>
                    </div>
                  </div>

                  <div className="tt-cards">
                    {(cardsByListId[list.id] || []).map((card) => (
                      <div key={card.id} className="tt-card">
                        <div className="tt-card-top">
                          <input
                            type="checkbox"
                            className="tt-card-check"
                            checked={Boolean(card.completed)}
                            onChange={() => handleToggleCardCompleted(list.id, card)}
                            aria-label="Mark completed"
                          />
                          <div
                            className={`tt-card-title ${card.completed ? 'completed' : ''}`}
                            title={card.name}
                          >
                            {card.name}
                          </div>
                          <div className="tt-card-actions">
                            <button
                              type="button"
                              className="btn btn-sm btn-link tt-icon-btn"
                              onClick={() => openEditCard(list.id, card)}
                              aria-label="Edit card"
                            >
                              ✎
                            </button>
                            <button
                              type="button"
                              className="btn btn-sm btn-link tt-icon-btn"
                              onClick={() => handleDeleteCard(list.id, card.id)}
                              aria-label="Delete card"
                            >
                              ⓧ
                            </button>
                          </div>
                        </div>
                        {card.description ? <div className="tt-card-desc">{card.description}</div> : null}
                      </div>
                    ))}
                  </div>

                  <button
                    type="button"
                    className="tt-add-card"
                    onClick={() => openCreateCard(list.id)}
                  >
                    + Add a card
                  </button>
                </section>
              ))}

              <section className="tt-column tt-column-dashed">
                <button type="button" className="tt-add-list-btn" onClick={() => setShowCreateList(true)}>
                  + Add another list
                </button>
              </section>
            </div>
          )}
        </main>
      </div>

      <Modal show={showCreateBoard} title="Create board" onClose={() => setShowCreateBoard(false)}>
        <div className="mb-3">
          <label className="form-label">Board name</label>
          <input
            className="form-control"
            value={newBoardName}
            onChange={(e) => setNewBoardName(e.target.value)}
            placeholder="e.g. My board"
          />
        </div>
        <div className="d-flex justify-content-end gap-2">
          <button type="button" className="btn btn-outline-secondary" onClick={() => setShowCreateBoard(false)}>
            Cancel
          </button>
          <button type="button" className="btn btn-primary" onClick={handleCreateBoard}>
            Create
          </button>
        </div>
      </Modal>

      <Modal show={showEditBoard} title="Edit board" onClose={() => setShowEditBoard(false)}>
        <div className="mb-3">
          <label className="form-label">Board name</label>
          <input
            className="form-control"
            value={editBoardName}
            onChange={(e) => setEditBoardName(e.target.value)}
            placeholder="Board name"
          />
        </div>
        <div className="d-flex justify-content-between gap-2">
          <button type="button" className="btn btn-outline-danger" onClick={handleDeleteBoard}>
            Delete board
          </button>
          <div className="d-flex gap-2">
            <button type="button" className="btn btn-outline-secondary" onClick={() => setShowEditBoard(false)}>
              Cancel
            </button>
            <button type="button" className="btn btn-primary" onClick={handleUpdateBoard}>
              Save
            </button>
          </div>
        </div>
      </Modal>

      <Modal show={showCreateList} title="Create list" onClose={() => setShowCreateList(false)}>
        <div className="mb-3">
          <label className="form-label">List name</label>
          <input
            className="form-control"
            value={newListName}
            onChange={(e) => setNewListName(e.target.value)}
            placeholder="e.g. In Progress"
          />
        </div>
        <div className="d-flex justify-content-end gap-2">
          <button type="button" className="btn btn-outline-secondary" onClick={() => setShowCreateList(false)}>
            Cancel
          </button>
          <button type="button" className="btn btn-primary" onClick={handleCreateList}>
            Create
          </button>
        </div>
      </Modal>

      <Modal show={showEditList} title="Edit list" onClose={() => setShowEditList(false)}>
        <div className="mb-3">
          <label className="form-label">List name</label>
          <input
            className="form-control"
            value={editListName}
            onChange={(e) => setEditListName(e.target.value)}
            placeholder="List name"
          />
        </div>
        <div className="d-flex justify-content-end gap-2">
          <button type="button" className="btn btn-outline-secondary" onClick={() => setShowEditList(false)}>
            Cancel
          </button>
          <button type="button" className="btn btn-primary" onClick={handleUpdateList}>
            Save
          </button>
        </div>
      </Modal>

      <Modal
        show={cardModalOpen}
        title={cardModalMode === 'create' ? 'Add card' : 'Edit card'}
        onClose={() => setCardModalOpen(false)}
      >
        <div className="mb-3">
          <label className="form-label">Name</label>
          <input
            className="form-control"
            value={cardName}
            onChange={(e) => setCardName(e.target.value)}
            placeholder="e.g. Write tests"
          />
        </div>
        <div className="mb-3">
          <label className="form-label">Description</label>
          <textarea
            className="form-control"
            rows={3}
            value={cardDescription}
            onChange={(e) => setCardDescription(e.target.value)}
            placeholder="Optional details"
          />
        </div>
        {cardModalMode === 'edit' ? (
          <div className="form-check mb-3">
            <input
              className="form-check-input"
              type="checkbox"
              checked={cardCompleted}
              onChange={(e) => setCardCompleted(e.target.checked)}
              id="cardCompleted"
            />
            <label className="form-check-label" htmlFor="cardCompleted">
              Completed
            </label>
          </div>
        ) : null}
        <div className="d-flex justify-content-end gap-2">
          <button type="button" className="btn btn-outline-secondary" onClick={() => setCardModalOpen(false)}>
            Cancel
          </button>
          <button type="button" className="btn btn-primary" onClick={handleCreateOrUpdateCard}>
            {cardModalMode === 'create' ? 'Add card' : 'Save changes'}
          </button>
        </div>
      </Modal>
    </div>
  );
}

export default App;
