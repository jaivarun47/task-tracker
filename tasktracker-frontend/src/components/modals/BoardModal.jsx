import { useState, useEffect } from 'react';
import Modal from '../common/Modal';
import Button from '../common/Button';

export function CreateBoardModal({ show, onClose, onSubmit }) {
  const [name, setName] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (show) setName('');
  }, [show]);

  async function handleSubmit(e) {
    e?.preventDefault();
    if (!name.trim() || submitting) return;
    setSubmitting(true);
    try {
      await onSubmit(name);
      onClose();
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Modal show={show} title="Create Board" onClose={onClose}>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label" htmlFor="new-board-name">Board Name</label>
          <input
            id="new-board-name"
            className="form-input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. Product Roadmap"
            autoFocus
            required
          />
        </div>
        <div className="form-actions">
          <Button variant="secondary" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button variant="primary" type="submit" disabled={!name.trim() || submitting}>
            {submitting ? 'Creating…' : 'Create Board'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}

export function EditBoardModal({ show, board, onClose, onUpdate, onDeleteClick }) {
  const [name, setName] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (board && show) {
      setName(board.name || '');
    }
  }, [board, show]);

  async function handleSubmit(e) {
    e?.preventDefault();
    if (!name.trim() || !board || submitting) return;
    setSubmitting(true);
    try {
      await onUpdate(board.id, name);
      onClose();
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Modal show={show} title="Edit Board" onClose={onClose}>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label" htmlFor="edit-board-name">Board Name</label>
          <input
            id="edit-board-name"
            className="form-input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Board name"
            autoFocus
            required
          />
        </div>
        <div className="form-actions-between">
          <Button
            variant="danger"
            onClick={() => {
              onClose();
              onDeleteClick();
            }}
            disabled={submitting}
          >
            Delete Board
          </Button>
          <div className="form-actions-group">
            <Button variant="secondary" onClick={onClose} disabled={submitting}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" disabled={!name.trim() || submitting}>
              {submitting ? 'Saving…' : 'Save Changes'}
            </Button>
          </div>
        </div>
      </form>
    </Modal>
  );
}

export function DeleteBoardModal({ show, board, onClose, onConfirm }) {
  const [deleting, setDeleting] = useState(false);

  async function handleConfirm() {
    if (!board || deleting) return;
    setDeleting(true);
    try {
      await onConfirm(board.id);
      onClose();
    } finally {
      setDeleting(false);
    }
  }

  return (
    <Modal show={show} title="Delete Board?" onClose={onClose}>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          handleConfirm();
        }}
      >
        <p className="modal-description">
          Are you sure you want to delete <strong>{board?.name}</strong>? All lists and cards within this board will be permanently removed.
        </p>
        <div className="form-actions">
          <Button variant="secondary" onClick={onClose} disabled={deleting}>
            Cancel
          </Button>
          <Button variant="danger" type="submit" disabled={deleting}>
            {deleting ? 'Deleting…' : 'Yes, Delete Board'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
