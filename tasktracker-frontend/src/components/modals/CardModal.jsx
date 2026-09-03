import { useState, useEffect } from 'react';
import Modal from '../common/Modal';
import Button from '../common/Button';
import { useBoard } from '../../hooks/useBoard';

export function CardModal({ show, card, listId, onClose, onSave, onDelete }) {
  const { lists, moveCardItem } = useBoard();
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [completed, setCompleted] = useState(false);
  const [targetListId, setTargetListId] = useState(listId);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (card && show) {
      setName(card.name || '');
      setDescription(card.description || '');
      setCompleted(Boolean(card.completed));
      setTargetListId(listId);
    }
  }, [card, listId, show]);

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

      // If target list was changed via dropdown, move the card seamlessly
      if (targetListId && targetListId !== listId && moveCardItem) {
        const sourceList = lists.find((l) => l.id === listId);
        const sourceIndex = sourceList ? sourceList.cards?.findIndex((c) => c.id === card.id) : 0;
        const targetList = lists.find((l) => l.id === targetListId);
        const targetIndex = targetList?.cards?.length || 0;
        moveCardItem({
          cardId: card.id,
          sourceListId: listId,
          targetListId,
          sourceIndex: sourceIndex >= 0 ? sourceIndex : 0,
          targetIndex,
        });
      }

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
            onKeyDown={(e) => {
              if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
                e.preventDefault();
                e.currentTarget.form?.requestSubmit();
              }
            }}
            placeholder="Add a more detailed description…"
          />
        </div>

        {lists && lists.length > 1 && (
          <div className="form-group">
            <label className="form-label" htmlFor="card-list-select">List</label>
            <select
              id="card-list-select"
              className="form-input"
              value={targetListId || listId}
              onChange={(e) => setTargetListId(Number(e.target.value))}
            >
              {lists.map((l) => (
                <option key={l.id} value={l.id}>
                  {l.name}
                </option>
              ))}
            </select>
          </div>
        )}

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
