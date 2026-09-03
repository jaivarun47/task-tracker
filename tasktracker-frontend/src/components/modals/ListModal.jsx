import { useState, useEffect } from 'react';
import Modal from '../common/Modal';
import Button from '../common/Button';

export function CreateListModal({ show, onClose, onSubmit }) {
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
    <Modal show={show} title="Create List" onClose={onClose}>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label" htmlFor="new-list-name">List Title</label>
          <input
            id="new-list-name"
            className="form-input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. In Progress"
            autoFocus
            required
          />
        </div>
        <div className="form-actions">
          <Button variant="secondary" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button variant="primary" type="submit" disabled={!name.trim() || submitting}>
            {submitting ? 'Creating…' : 'Create List'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}

export function EditListModal({ show, list, onClose, onUpdate, onDeleteClick }) {
  const [name, setName] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (list && show) {
      setName(list.name || '');
    }
  }, [list, show]);

  async function handleSubmit(e) {
    e?.preventDefault();
    if (!name.trim() || !list || submitting) return;
    setSubmitting(true);
    try {
      await onUpdate(list.id, name);
      onClose();
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Modal show={show} title="Edit List" onClose={onClose}>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label" htmlFor="edit-list-name">List Title</label>
          <input
            id="edit-list-name"
            className="form-input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="List title"
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
            Delete List
          </Button>
          <div className="form-actions-group">
            <Button variant="secondary" onClick={onClose} disabled={submitting}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" disabled={!name.trim() || submitting}>
              {submitting ? 'Saving…' : 'Save'}
            </Button>
          </div>
        </div>
      </form>
    </Modal>
  );
}

export function DeleteListModal({ show, list, onClose, onConfirm }) {
  const [deleting, setDeleting] = useState(false);

  async function handleConfirm() {
    if (!list || deleting) return;
    setDeleting(true);
    try {
      await onConfirm(list.id);
      onClose();
    } finally {
      setDeleting(false);
    }
  }

  return (
    <Modal show={show} title="Delete List?" onClose={onClose}>
      <p className="modal-description">
        Are you sure you want to delete <strong>{list?.name}</strong> and its {list?.cards?.length || 0} cards?
      </p>
      <div className="form-actions">
        <Button variant="secondary" onClick={onClose} disabled={deleting}>
          Cancel
        </Button>
        <Button variant="danger" onClick={handleConfirm} disabled={deleting}>
          {deleting ? 'Deleting…' : 'Delete List'}
        </Button>
      </div>
    </Modal>
  );
}
