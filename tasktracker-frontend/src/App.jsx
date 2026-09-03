import { useState } from 'react';
import './App.css';
import { BoardProvider } from './context/BoardContext';
import { ThemeProvider } from './context/ThemeContext';
import { useBoard } from './hooks/useBoard';
import Sidebar from './components/layout/Sidebar';
import BoardHeader from './components/layout/BoardHeader';
import BoardCanvas from './components/kanban/BoardCanvas';
import { CreateBoardModal, EditBoardModal, DeleteBoardModal } from './components/modals/BoardModal';
import { CreateListModal, EditListModal } from './components/modals/ListModal';
import { CardModal } from './components/modals/CardModal';
import { ToastContainer } from './components/common/Toast';
import { useKeyboardShortcuts } from './hooks/useKeyboardShortcuts';

function MainApp() {
  const {
    selectedBoard,
    selectedBoardId,
    lists,
    activeListId,
    createBoard,
    updateBoard,
    deleteBoard,
    createList,
    updateList,
    deleteList,
    createCard,
    updateCard,
    deleteCard,
    toasts,
    removeToast,
    setActiveListId,
  } = useBoard();

  // ── Modal State ───────────────────────────────────────────────────────────
  const [boardModal, setBoardModal] = useState({ type: null, data: null }); // 'create' | 'edit' | 'delete'
  const [listModal, setListModal] = useState({ type: null, data: null });   // 'create' | 'edit' | 'delete'
  const [cardModal, setCardModal] = useState({ show: false, listId: null, card: null });

  function closeAllModals() {
    setBoardModal({ type: null, data: null });
    setListModal({ type: null, data: null });
    setCardModal({ show: false, listId: null, card: null });
    setActiveListId(null);
  }

  // ── Keyboard Shortcuts ────────────────────────────────────────────────────
  useKeyboardShortcuts({
    onEscape: closeAllModals,
    onCreateBoard: () => setBoardModal({ type: 'create', data: null }),
    onCreateList: () => {
      if (selectedBoardId) {
        setListModal({ type: 'create', data: null });
      }
    },
    onCreateCard: () => {
      if (selectedBoardId && lists.length > 0) {
        const targetList = activeListId && lists.some((l) => l.id === activeListId)
          ? lists.find((l) => l.id === activeListId)
          : lists[0];
        if (targetList) {
          // Open card creation dialog
          setCardModal({ show: true, listId: targetList.id, card: { name: '', description: '', completed: false } });
        }
      }
    },
    onDeleteBoard: () => {
      if (selectedBoard) {
        setBoardModal({ type: 'delete', data: selectedBoard });
      }
    },
  });

  return (
    <div className="tt-app-root">
      <Sidebar
        onOpenCreateBoard={() => setBoardModal({ type: 'create', data: null })}
        onOpenEditBoard={(board) => setBoardModal({ type: 'edit', data: board })}
      />

      <div className="tt-content-wrapper">
        <BoardHeader
          onOpenEditBoard={(board) => setBoardModal({ type: 'edit', data: board })}
        />

        <main className="tt-canvas-container">
          <BoardCanvas
            onOpenCreateList={() => setListModal({ type: 'create', data: null })}
            onOpenEditList={(list) => setListModal({ type: 'edit', data: list })}
            onOpenDeleteList={(list) => deleteList(list.id)}
            onOpenEditCard={(listId, card) => setCardModal({ show: true, listId, card })}
          />
        </main>
      </div>

      {/* Board Modals */}
      <CreateBoardModal
        show={boardModal.type === 'create'}
        onClose={() => setBoardModal({ type: null, data: null })}
        onSubmit={createBoard}
      />

      <EditBoardModal
        show={boardModal.type === 'edit'}
        board={boardModal.data}
        onClose={() => setBoardModal({ type: null, data: null })}
        onUpdate={updateBoard}
        onDeleteClick={() => setBoardModal({ type: 'delete', data: boardModal.data })}
      />

      <DeleteBoardModal
        show={boardModal.type === 'delete'}
        board={boardModal.data}
        onClose={() => setBoardModal({ type: null, data: null })}
        onConfirm={deleteBoard}
      />

      {/* List Modals */}
      <CreateListModal
        show={listModal.type === 'create'}
        onClose={() => setListModal({ type: null, data: null })}
        onSubmit={createList}
      />

      <EditListModal
        show={listModal.type === 'edit'}
        list={listModal.data}
        onClose={() => setListModal({ type: null, data: null })}
        onUpdate={updateList}
        onDeleteClick={() => {
          if (listModal.data?.id) {
            deleteList(listModal.data.id);
            setListModal({ type: null, data: null });
          }
        }}
      />

      {/* Card Detail Modal */}
      <CardModal
        show={cardModal.show}
        card={cardModal.card}
        onClose={() => setCardModal({ show: false, listId: null, card: null })}
        onSave={(cardId, fields) => {
          if (cardModal.card?.id) {
            return updateCard(cardModal.listId, cardId, fields);
          } else {
            return createCard(cardModal.listId, fields);
          }
        }}
        onDelete={cardModal.card?.id ? (cardId) => deleteCard(cardModal.listId, cardId) : null}
      />

      {/* Toast Feedback */}
      <ToastContainer toasts={toasts} onClose={removeToast} />
    </div>
  );
}

export default function App() {
  return (
    <ThemeProvider>
      <BoardProvider>
        <MainApp />
      </BoardProvider>
    </ThemeProvider>
  );
}
