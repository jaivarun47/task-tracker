import { useState, useRef } from 'react';
import Icon from '../common/Icon';
import CardItem from './CardItem';
import AddCardInline from './AddCardInline';
import { useBoard } from '../../hooks/useBoard';

export default function ListColumn({
  list,
  index,
  isCollapsed = false,
  onToggleCollapse,
  onEditList,
  onDeleteList,
  onEditCard,
}) {
  const {
    activeListId,
    setActiveListId,
    draggedItem,
    setDraggedItem,
    moveCardItem,
    reorderList,
  } = useBoard();

  const [isColumnDragTarget, setIsColumnDragTarget] = useState(false);
  const [isCardDropZoneActive, setIsCardDropZoneActive] = useState(false);
  const columnRef = useRef(null);

  const cards = list.cards || [];
  const isFocused = activeListId === list.id;

  // ── Column Drag Handlers (Reorder lists within board) ─────────────────────

  function handleColumnDragStart(e) {
    const dragData = {
      type: 'LIST',
      listId: list.id,
      sourceIndex: index,
    };
    e.dataTransfer.effectAllowed = 'move';
    e.dataTransfer.setData('application/json', JSON.stringify(dragData));
    setDraggedItem(dragData);

    setTimeout(() => {
      columnRef.current?.classList.add('is-dragging-column');
    }, 0);
  }

  function handleColumnDragEnd() {
    columnRef.current?.classList.remove('is-dragging-column');
    setDraggedItem(null);
    setIsColumnDragTarget(false);
  }

  // ── Container Drag Over & Drop Handlers ───────────────────────────────────

  function handleContainerDragOver(e) {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';

    // Highlight drop area based on what is being dragged
    setIsColumnDragTarget(true);
    setIsCardDropZoneActive(true);
  }

  function handleContainerDragLeave(e) {
    // Only clear if leaving the column element itself
    if (!e.currentTarget.contains(e.relatedTarget)) {
      setIsColumnDragTarget(false);
      setIsCardDropZoneActive(false);
    }
  }

  function handleContainerDrop(e) {
    e.preventDefault();
    setIsColumnDragTarget(false);
    setIsCardDropZoneActive(false);

    try {
      const rawData = e.dataTransfer.getData('application/json');
      if (!rawData) return;
      const data = JSON.parse(rawData);

      if (data.type === 'LIST') {
        // Reorder list within board
        if (data.sourceIndex !== index) {
          reorderList(data.sourceIndex, index);
        }
      } else if (data.type === 'CARD') {
        // Dropped directly onto list container (e.g. empty list or bottom of list)
        // Insert at the end of the cards in this list
        const targetIndex = cards.length;
        moveCardItem({
          cardId: data.cardId,
          sourceListId: data.sourceListId,
          targetListId: list.id,
          sourceIndex: data.sourceIndex,
          targetIndex,
        });
      }
    } catch (err) {
      console.error('Failed to parse drop event', err);
    }
  }

  const isListDropTarget =
    isColumnDragTarget && draggedItem?.type === 'LIST' && draggedItem?.listId !== list.id;

  return (
    <section
      ref={columnRef}
      className={`tt-column ${isCollapsed ? 'is-collapsed' : ''} ${isFocused ? 'is-focused' : ''} ${
        isListDropTarget ? 'column-drag-target' : ''
      }`}
      onClick={() => setActiveListId(list.id)}
      onDragOver={handleContainerDragOver}
      onDragLeave={handleContainerDragLeave}
      onDrop={handleContainerDrop}
      role="region"
      aria-label={`List: ${list.name}`}
    >
      {/* Draggable Column Header */}
      <div
        className="tt-col-header"
        draggable
        onDragStart={handleColumnDragStart}
        onDragEnd={handleColumnDragEnd}
        title={isCollapsed ? 'Click to expand list, or drag to reorder' : 'Drag list to reorder'}
        onClick={() => {
          if (isCollapsed) {
            onToggleCollapse?.();
          }
        }}
      >
        <div className="col-header-left">
          <div className="col-drag-grip">
            <Icon name="grip" size={13} />
          </div>
          <h2 className="tt-col-title">{list.name}</h2>
          <span className="col-card-counter" title={`${cards.length} cards`}>
            {cards.length}
          </span>
        </div>

        <div className="tt-col-actions">
          <button
            type="button"
            className="col-action-btn col-action-collapse"
            onClick={(e) => {
              e.stopPropagation();
              onToggleCollapse?.();
            }}
            title={isCollapsed ? 'Expand list' : 'Collapse list'}
            aria-label={isCollapsed ? `Expand ${list.name}` : `Collapse ${list.name}`}
            aria-expanded={!isCollapsed}
          >
            <Icon name={isCollapsed ? 'chevronDown' : 'chevronUp'} size={13} />
          </button>
          <button
            type="button"
            className="col-action-btn"
            onClick={(e) => {
              e.stopPropagation();
              onEditList(list);
            }}
            title="Edit list title"
            aria-label={`Edit ${list.name}`}
          >
            <Icon name="edit" size={13} />
          </button>
          <button
            type="button"
            className="col-action-btn col-action-delete"
            onClick={(e) => {
              e.stopPropagation();
              onDeleteList(list);
            }}
            title="Delete list"
            aria-label={`Delete ${list.name}`}
          >
            <Icon name="trash" size={13} />
          </button>
        </div>
      </div>

      {!isCollapsed && (
        <>
          {/* Cards Stack */}
          <div className={`tt-cards-scroll-area ${isCardDropZoneActive && cards.length === 0 ? 'empty-drop-active' : ''}`}>
            {cards.length > 0 ? (
              <div className="tt-cards-stack" role="list">
                {cards.map((card, cardIndex) => (
                  <CardItem
                    key={card.id}
                    card={card}
                    listId={list.id}
                    index={cardIndex}
                    onEdit={() => onEditCard(list.id, card)}
                  />
                ))}
              </div>
            ) : (
              <div className="tt-empty-list-slot">
                <div className="empty-list-dashed-box">
                  <span className="empty-list-text">No cards yet</span>
                  <span className="empty-list-subtext">Drop a card here</span>
                </div>
              </div>
            )}
          </div>

          {/* Inline Card Creator at Bottom of List */}
          <div className="tt-col-footer">
            <AddCardInline listId={list.id} />
          </div>
        </>
      )}
    </section>
  );
}
