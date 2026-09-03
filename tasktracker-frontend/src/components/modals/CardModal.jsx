import { useState, useEffect } from 'react';
import Modal from '../common/Modal';
import Button from '../common/Button';

export function CardModal({ show, card, onClose, onSave, onDelete }) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [completed, setCompleted] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (card && show) {
      setName(card.name || '');
      setDescription(card.description || '');
      setCompleted(Boolean(card.completed));
    }
  }, [card, show]);

  async function handleSubmit(e) {
    e?.preventDefault();
    if (!name.trim() || !card || submitting) return;
    setSubmitting(true);
    try {
      await onSave(card.id, {
        name: name.trim(),
        description: description.trim(),
        completed,
      });
      onClose();
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Modal show={show} title="Edit Card" onClose={onClose} maxWidth="520px">
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label" htmlFor="card-title-input">Title</label>
          <input
            id="card-title-input"
            className="form-input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Card title"
            autoFocus
            required
          />
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="card-desc-input">Description</label>
          <textarea
            id="card-desc-input"
            className="form-textarea"
            rows={4}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Add a more detailed description…"
          />
        </div>

        <div className="form-group form-checkbox-group">
          <label className="checkbox-container">
            <input
              type="checkbox"
              checked={completed}
              onChange={(e) => setCompleted(e.target.checked)}
            />
            <span className="checkbox-label">Mark as completed</span>
          </label>
        </div>

        <div className="form-actions-between">
          {onDelete ? (
            <Button
              variant="danger"
              onClick={() => {
                onClose();
                onDelete(card.id);
              }}
              disabled={submitting}
            >
              Delete Card
            </Button>
          ) : <div />}
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
