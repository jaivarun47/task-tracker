import { useState, useRef, useEffect } from 'react';
import Icon from '../common/Icon';
import Button from '../common/Button';
import { useBoard } from '../../hooks/useBoard';

export default function AddCardInline({ listId }) {
  const { createCard } = useBoard();
  const [isOpen, setIsOpen] = useState(false);
  const [name, setName] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const inputRef = useRef(null);

  useEffect(() => {
    if (isOpen) {
      inputRef.current?.focus();
    } else {
      setName('');
    }
  }, [isOpen]);

  async function handleSubmit(e) {
    e?.preventDefault();
    if (!name.trim() || submitting) return;
    setSubmitting(true);
    try {
      await createCard(listId, { name });
      setName('');
      inputRef.current?.focus();
    } finally {
      setSubmitting(false);
    }
  }

  function handleKeyDown(e) {
    if (e.key === 'Escape') {
      setIsOpen(false);
    }
  }

  if (!isOpen) {
    return (
      <button
        type="button"
        className="add-card-trigger"
        onClick={() => setIsOpen(true)}
      >
        <Icon name="plus" size={14} />
        <span>Add a card</span>
      </button>
    );
  }

  return (
    <form className="add-card-inline-form anim-fade-in" onSubmit={handleSubmit}>
      <input
        ref={inputRef}
        className="add-card-inline-input"
        value={name}
        onChange={(e) => setName(e.target.value)}
        onKeyDown={handleKeyDown}
        placeholder="Enter a title for this card…"
        disabled={submitting}
        required
      />
      <div className="add-card-inline-actions">
        <Button variant="primary" size="sm" type="submit" disabled={!name.trim() || submitting}>
          {submitting ? 'Adding…' : 'Add Card'}
        </Button>
        <Button
          variant="ghost"
          size="sm"
          onClick={() => setIsOpen(false)}
          disabled={submitting}
        >
          Cancel
        </Button>
      </div>
    </form>
  );
}
