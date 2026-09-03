import Icon from '../common/Icon';
import { useBoard } from '../../hooks/useBoard';

export default function Sidebar({ onOpenCreateBoard, onOpenEditBoard }) {
  const {
    boards,
    selectedBoardId,
    selectBoard,
    isSidebarCollapsed,
    toggleSidebar,
    selectedBoard,
  } = useBoard();

  function getInitials(name) {
    if (!name) return 'B';
    const parts = name.trim().split(/\s+/);
    if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
    return (parts[0][0] + parts[1][0]).toUpperCase();
  }

  return (
    <aside className={`tt-sidebar ${isSidebarCollapsed ? 'collapsed' : 'expanded'}`}>
      {/* Sidebar Header */}
      <div className="sidebar-header">
        {!isSidebarCollapsed ? (
          <div className="sidebar-brand">
            <div className="brand-icon">
              <Icon name="board" size={18} />
            </div>
            <span className="brand-text">TaskTracker</span>
          </div>
        ) : (
          <div className="brand-icon collapsed-brand-icon" title="TaskTracker">
            <Icon name="board" size={20} />
          </div>
        )}

        <button
          type="button"
          className="sidebar-toggle-btn"
          onClick={toggleSidebar}
          aria-label={isSidebarCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          title={isSidebarCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          <Icon name={isSidebarCollapsed ? 'chevronRight' : 'chevronLeft'} size={16} />
        </button>
      </div>

      {/* Boards Section */}
      <div className="sidebar-section dark-scroll">
        {!isSidebarCollapsed && <div className="sidebar-section-title">Boards</div>}

        <div className="sidebar-boards-list" role="list">
          {boards.map((b) => {
            const isActive = b.id === selectedBoardId;
            const initials = getInitials(b.name);

            return (
              <div key={b.id} className="board-item-wrapper">
                <button
                  type="button"
                  className={`sidebar-board-item ${isActive ? 'active' : ''}`}
                  onClick={() => selectBoard(b.id)}
                  title={b.name}
                >
                  <span className="board-avatar">{initials}</span>
                  {!isSidebarCollapsed && (
                    <span className="board-name-label">{b.name}</span>
                  )}
                </button>

                {!isSidebarCollapsed && isActive && (
                  <button
                    type="button"
                    className="board-settings-trigger"
                    onClick={(e) => {
                      e.stopPropagation();
                      onOpenEditBoard(b);
                    }}
                    title="Board Settings"
                    aria-label={`Settings for ${b.name}`}
                  >
                    <Icon name="edit" size={14} />
                  </button>
                )}
              </div>
            );
          })}

          {boards.length === 0 && !isSidebarCollapsed && (
            <div className="sidebar-empty-hint">No boards yet</div>
          )}
        </div>
      </div>

      {/* Bottom Pinned Footer */}
      <div className="sidebar-footer">
        {selectedBoard && !isSidebarCollapsed && (
          <div className="sidebar-active-summary">
            <span className="sidebar-active-label">Active</span>
            <span className="sidebar-active-name" title={selectedBoard.name}>
              {selectedBoard.name}
            </span>
          </div>
        )}

        <button
          type="button"
          className={`sidebar-add-board-btn ${isSidebarCollapsed ? 'collapsed-add-btn' : ''}`}
          onClick={onOpenCreateBoard}
          title="Create New Board"
          aria-label="Create New Board"
        >
          <Icon name="plus" size={16} />
          {!isSidebarCollapsed && <span>Add Board</span>}
        </button>
      </div>
    </aside>
  );
}
