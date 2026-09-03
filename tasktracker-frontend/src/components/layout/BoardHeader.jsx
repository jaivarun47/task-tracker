import Icon from '../common/Icon';
import ThemeToggle from '../common/ThemeToggle';
import { useBoard } from '../../hooks/useBoard';

export default function BoardHeader({ onOpenEditBoard }) {
  const { selectedBoard, lists, boardLoading, toggleSidebar } = useBoard();

  const totalCards = lists.reduce((acc, l) => acc + (l.cards?.length || 0), 0);
  const completedCards = lists.reduce(
    (acc, l) => acc + (l.cards?.filter((c) => c.completed)?.length || 0),
    0
  );

  return (
    <header className="tt-main-header">
      <div className="header-left-cluster">
        <button
          type="button"
          className="mobile-sidebar-toggle-btn"
          onClick={toggleSidebar}
          title="Open boards menu"
          aria-label="Open boards menu"
        >
          <Icon name="menu" size={19} />
        </button>

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
      </div>

      <div className="header-right-actions">
        <ThemeToggle />
      </div>
    </header>
  );
}
