import Icon from '../common/Icon';
import { useBoard } from '../../hooks/useBoard';

export default function BoardHeader({ onOpenEditBoard }) {
  const { selectedBoard, lists, boardLoading } = useBoard();

  const totalCards = lists.reduce((acc, l) => acc + (l.cards?.length || 0), 0);
  const completedCards = lists.reduce(
    (acc, l) => acc + (l.cards?.filter((c) => c.completed)?.length || 0),
    0
  );

  return (
    <header className="tt-main-header">
      <div className="header-title-block">
        <div className="header-title-row">
          <h1 className="header-board-title">
            {selectedBoard ? selectedBoard.name : 'Select a board'}
          </h1>
          {selectedBoard && (
            <button
              type="button"
              className="header-edit-btn"
              onClick={() => onOpenEditBoard(selectedBoard)}
              title="Edit board name"
              aria-label="Edit board name"
            >
              <Icon name="edit" size={15} />
            </button>
          )}
        </div>

        {selectedBoard && (
          <div className="header-stats">
            <span className="stats-item">
              <strong>{lists.length}</strong> {lists.length === 1 ? 'list' : 'lists'}
            </span>
            <span className="stats-divider">•</span>
            <span className="stats-item">
              <strong>{totalCards}</strong> {totalCards === 1 ? 'task' : 'tasks'}
            </span>
            {totalCards > 0 && (
              <>
                <span className="stats-divider">•</span>
                <span className="stats-item stats-completed">
                  <strong>{completedCards}</strong> done ({Math.round((completedCards / totalCards) * 100)}%)
                </span>
              </>
            )}
            {boardLoading && (
              <>
                <span className="stats-divider">•</span>
                <span className="stats-loading">
                  <Icon name="spinner" size={13} /> Updating…
                </span>
              </>
            )}
          </div>
        )}
      </div>
    </header>
  );
}
