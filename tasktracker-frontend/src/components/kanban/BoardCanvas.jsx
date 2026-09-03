import Icon from '../common/Icon';
import ListColumn from './ListColumn';
import Button from '../common/Button';
import { useBoard } from '../../hooks/useBoard';

export default function BoardCanvas({
  onOpenCreateList,
  onOpenEditList,
  onOpenDeleteList,
  onOpenEditCard,
}) {
  const { lists, selectedBoard, boardLoading } = useBoard();

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
      <div className="tt-kanban-track">
        {lists.map((list, index) => (
          <ListColumn
            key={list.id}
            list={list}
            index={index}
            onEditList={onOpenEditList}
            onDeleteList={onOpenDeleteList}
            onEditCard={onOpenEditCard}
          />
        ))}

        {/* Trailing Dashed "+ Add List" Slot */}
        <div className="tt-add-column-slot">
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
