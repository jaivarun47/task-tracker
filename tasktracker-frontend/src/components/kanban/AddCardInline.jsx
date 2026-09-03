import { useState, useRef, useEffect, useCallback } from 'react';
import Icon from '../common/Icon';
import Button from '../common/Button';
import { useBoard } from '../../hooks/useBoard';

export default function AddCardInline({ listId }) {
  const { createCard } = useBoard();
  const [isOpen, setIsOpen] = useState(false);
  const [name, setName] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [shouldFocus, setShouldFocus] = useState(false);
  const inputRef = useRef(null);

  // Initial focus when opened
  useEffect(() => {
    if (isOpen) {
      inputRef.current?.focus();
    } else {
      setName('');
      setShouldFocus(false);
    }
  }, [isOpen]);

  // Safe auto-focus after card creation without stealing focus from modals
  useEffect(() => {
    if (shouldFocus && isOpen) {
      if (!document.querySelector('.modal-overlay')) {
        inputRef.current?.focus();
      }
      setShouldFocus(false);
    }
  }, [shouldFocus, isOpen]);

  const handleSubmit = useCallback(async (e) => {
    e?.preventDefault();
    const trimmed = name.trim();
    if (!trimmed || submitting) return;

    setSubmitting(true);
    try {
      await createCard(listId, { name: trimmed });
      setName('');
      setShouldFocus(true);
    } finally {
      setSubmitting(false);
    }
  }, [name, submitting, createCard, listId]);

  function handleKeyDown(e) {
    if (e.key === 'Escape') {
      e.preventDefault();
      setIsOpen(false);
    } else if (e.key === 'Enter') {
      e.preventDefault();
      handleSubmit();
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

