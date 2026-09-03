import { useEffect } from 'react';

export function useKeyboardShortcuts({
  onEscape,
  onCreateBoard,
  onCreateList,
  onCreateCard,
  onDeleteBoard,
}) {
  useEffect(() => {
    function handleKeyDown(e) {
      if (e.key === 'Escape') {
        onEscape?.();
        return;
      }

      // Ignore input/textarea focus
      if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') {
        return;
      }

      // Alt + Shift + B -> Create Board
      if (e.altKey && e.shiftKey && e.key.toLowerCase() === 'b') {
        e.preventDefault();
        onCreateBoard?.();
      }
      // Alt + Shift + N -> Create List
      else if (e.altKey && e.shiftKey && e.key.toLowerCase() === 'n') {
        e.preventDefault();
        onCreateList?.();
      }
      // Alt + N -> Create Card
      else if (e.altKey && !e.shiftKey && e.key.toLowerCase() === 'n') {
        e.preventDefault();
        onCreateCard?.();
      }
      // Alt + Shift + Delete -> Delete Board
      else if (e.altKey && e.shiftKey && e.key === 'Delete') {
        e.preventDefault();
        onDeleteBoard?.();
      }
    }

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onEscape, onCreateBoard, onCreateList, onCreateCard, onDeleteBoard]);
}
