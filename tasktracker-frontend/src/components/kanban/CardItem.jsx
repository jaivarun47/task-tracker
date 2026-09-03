import { useState } from 'react';
import Icon from '../common/Icon';
import { useBoard } from '../../hooks/useBoard';

export default function CardItem({ card, listId, index, onEdit }) {
  const { toggleCardCompletion, deleteCard, setDraggedItem, moveCardItem } = useBoard();
  const [isDragTarget, setIsDragTarget] = useState(false);
  const [dragPosition, setDragPosition] = useState('top'); // 'top' | 'bottom'

  function handleDragStart(e) {
    const dragData = {
      type: 'CARD',
      cardId: card.id,
      sourceListId: listId,
      sourceIndex: index,
    };
    e.dataTransfer.effectAllowed = 'move';
    e.dataTransfer.setData('application/json', JSON.stringify(dragData));
    setDraggedItem(dragData);

    // Give visual hint without instant collapse
    setTimeout(() => {
      e.target.classList.add('is-dragging');
    }, 0);
  }

  function handleDragEnd(e) {
    e.target.classList.remove('is-dragging');
    setDraggedItem(null);
    setIsDragTarget(false);
  }

  function handleDragOver(e) {
    e.preventDefault();
    e.stopPropagation();
    e.dataTransfer.dropEffect = 'move';

    // Calculate whether hover is in top or bottom half of the card
    const rect = e.currentTarget.getBoundingClientRect();
    const midY = rect.top + rect.height / 2;
    const pos = e.clientY < midY ? 'top' : 'bottom';
    setDragPosition(pos);
    setIsDragTarget(true);
  }

  function handleDragLeave(e) {
    e.stopPropagation();
    setIsDragTarget(false);
  }

  function handleDrop(e) {
    e.preventDefault();
    e.stopPropagation();
    setIsDragTarget(false);

    try {
      const rawData = e.dataTransfer.getData('application/json');
      if (!rawData) return;
      const data = JSON.parse(rawData);

      if (data.type !== 'CARD') return;

      let targetIndex = index;
      if (dragPosition === 'bottom') {
        targetIndex = index + 1;
      }

      // If moving within same list, adjust index if dragging down
      if (data.sourceListId === listId && data.sourceIndex < targetIndex) {
        targetIndex = Math.max(0, targetIndex - 1);
      }

      moveCardItem({
        cardId: data.cardId,
        sourceListId: data.sourceListId,
        targetListId: listId,
        sourceIndex: data.sourceIndex,
        targetIndex,
      });
    } catch (err) {
      console.error('Failed to parse drag data', err);
    }
  }

  return (
    <div
      className={`tt-card ${card.completed ? 'is-completed' : ''} ${
        isDragTarget ? `drag-over-${dragPosition}` : ''
      }`}
      draggable
      onDragStart={handleDragStart}
      onDragEnd={handleDragEnd}
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
      role="listitem"
    >
      <div className="card-drag-handle" title="Drag to reorder">
        <Icon name="grip" size={13} />
      </div>

      <div className="card-main-content">
        <div className="card-top-row">
          <label
            className="card-checkbox-label"
            onClick={(e) => e.stopPropagation()}
          >
            <input
              type="checkbox"
              className="card-checkbox"
              checked={Boolean(card.completed)}
              onChange={() => toggleCardCompletion(listId, card)}
              aria-label={`Mark "${card.name}" as completed`}
            />
          </label>

          <span
            className={`card-title ${card.completed ? 'completed' : ''}`}
            title={card.name}
            onClick={() => onEdit(card)}
          >
            {card.name}
          </span>

          <div
            className="card-actions"
            onClick={(e) => e.stopPropagation()}
          >
            <button
              type="button"
              className="card-action-btn"
              onClick={() => onEdit(card)}
              title="Edit card"
              aria-label={`Edit ${card.name}`}
            >
              <Icon name="edit" size={13} />
            </button>
            <button
              type="button"
              className="card-action-btn card-action-delete"
              onClick={() => deleteCard(listId, card.id)}
              title="Delete card"
              aria-label={`Delete ${card.name}`}
            >
              <Icon name="trash" size={13} />
            </button>
          </div>
        </div>

        {card.description ? (
          <p
            className="card-description-preview"
            title={card.description}
            onClick={() => onEdit(card)}
          >
            {card.description}
          </p>
        ) : null}
      </div>
    </div>
  );
}
