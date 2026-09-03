import { useState, useCallback } from 'react';
import Icon from '../common/Icon';
import ListColumn from './ListColumn';
import Button from '../common/Button';
import { useBoard } from '../../hooks/useBoard';
import { useMasonryLayout } from '../../hooks/useMasonryLayout';

export default function BoardCanvas({
  onOpenCreateList,
  onOpenEditList,
  onOpenDeleteList,
  onOpenEditCard,
}) {
  const { lists, selectedBoard, boardLoading, draggedItem, reorderList } = useBoard();
  const [isAddSlotDropTarget, setIsAddSlotDropTarget] = useState(false);

  const [collapsedListIds, setCollapsedListIds] = useState(() => new Set());

  const toggleCollapseList = useCallback((listId) => {
    setCollapsedListIds((prev) => {
      const next = new Set(prev);
      if (next.has(listId)) {
        next.delete(listId);
      } else {
        next.add(listId);
      }
      return next;
    });
  }, []);

  const {
    containerRef,
    registerItemRef,
    positions,
    addSlotPos,
    totalHeight,
    columnWidth,
  } = useMasonryLayout(lists, {
    targetColumnWidth: 300,
    minColumnWidth: 260,
    gap: 20,
    addSlotHeight: 140,
    hasAddSlot: true,
    collapsedListIds,
  });

  // ── Drag & Drop for Add List Slot (Move list to the end) ───────────────────
  function handleAddSlotDragOver(e) {
    if (draggedItem?.type !== 'LIST') return;
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    setIsAddSlotDropTarget(true);
  }

  function handleAddSlotDragLeave(e) {
    if (!e.currentTarget.contains(e.relatedTarget)) {
      setIsAddSlotDropTarget(false);
    }
  }

  function handleAddSlotDrop(e) {
    if (draggedItem?.type !== 'LIST') return;
    e.preventDefault();
    setIsAddSlotDropTarget(false);
    try {
      const rawData = e.dataTransfer.getData('application/json');
      if (!rawData) return;
      const data = JSON.parse(rawData);
      if (data.type === 'LIST') {
        const targetIndex = lists.length - 1;
        if (data.sourceIndex !== targetIndex) {
          reorderList(data.sourceIndex, targetIndex);
        }
      }
    } catch (err) {
      console.error('Failed to parse drop event on Add List slot', err);
    }
  }

  if (!selectedBoard) {
    return (
      <div className="board-canvas-empty-state">
        <div className="empty-state-card">
          <div className="empty-state-icon">
            <Icon name="board" size={32} />
          </div>
          <h2 className="empty-state-title">Select or create a board</h2>
          <p className="empty-state-description">
            Choose a board from the sidebar or create a new one to start tracking your tasks.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="tt-kanban-viewport">
      <div
        className="tt-kanban-track tt-masonry-track"
        ref={containerRef}
        style={{ height: `${totalHeight}px` }}
      >
        {lists.map((list, index) => {
          const pos = positions[list.id] || { x: 0, y: 0, width: columnWidth };
          const isCurrentlyDragging = draggedItem?.type === 'LIST' && draggedItem?.listId === list.id;

          return (
            <div
              key={list.id}
              ref={(el) => registerItemRef(list.id, el)}
              className={`tt-masonry-item ${isCurrentlyDragging ? 'is-dragging' : ''}`}
              style={{
                transform: `translate3d(${pos.x}px, ${pos.y}px, 0)`,
                width: `${pos.width || columnWidth}px`,
              }}
            >
              <ListColumn
                list={list}
                index={index}
                isCollapsed={collapsedListIds.has(list.id)}
                onToggleCollapse={() => toggleCollapseList(list.id)}
                onEditList={onOpenEditList}
                onDeleteList={onOpenDeleteList}
                onEditCard={onOpenEditCard}
              />
            </div>
          );
        })}

        {/* Trailing Dashed "+ Add List" Slot packed in masonry */}
        {addSlotPos && (
          <div
            className="tt-masonry-item tt-masonry-add-slot"
            style={{
              transform: `translate3d(${addSlotPos.x}px, ${addSlotPos.y}px, 0)`,
              width: `${addSlotPos.width || columnWidth}px`,
            }}
            onDragOver={handleAddSlotDragOver}
            onDragLeave={handleAddSlotDragLeave}
            onDrop={handleAddSlotDrop}
          >
            <div className={`tt-add-column-slot ${isAddSlotDropTarget ? 'column-drag-target' : ''}`}>
              <button
                type="button"
                className="add-column-dashed-btn"
                onClick={onOpenCreateList}
                aria-label="Add another list"
              >
                <div className="add-column-icon">
                  <Icon name="plus" size={18} />
                </div>
                <span className="add-column-label">Add List</span>
              </button>
            </div>
          </div>
        )}
      </div>

      {lists.length === 0 && !boardLoading && (
        <div className="empty-board-banner anim-fade-in">
          <div className="empty-board-content">
            <h3>This board has no lists</h3>
            <p>Create columns like "To Do", "In Progress", or "Done" to organize tasks.</p>
            <Button variant="primary" onClick={onOpenCreateList}>
              <Icon name="plus" size={15} />
              <span>Create First List</span>
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
