import { useEffect, useRef } from 'react';
import Icon from './Icon';

export default function Modal({
  show,
  title,
  onClose,
  children,
  maxWidth = '460px',
}) {
  const modalRef = useRef(null);

  useEffect(() => {
    if (!show) return;

    function handleKeyDown(e) {
      if (e.key === 'Escape') {
        e.stopPropagation();
        onClose?.();
      } else if (e.key === 'Enter') {
        const target = e.target;
        // If focused on a button, let the button's native click handler execute
        if (target && target.tagName === 'BUTTON') {
          return;
        }

        // In a multiline textarea, regular Enter must insert a newline.
        // Ctrl+Enter or Cmd+Enter submits the form.
        if (target && target.tagName === 'TEXTAREA') {
          if (e.ctrlKey || e.metaKey) {
            e.preventDefault();
            const form = target.closest('form') || modalRef.current?.querySelector('form');
            if (form) {
              if (typeof form.requestSubmit === 'function') {
                form.requestSubmit();
              } else {
                form.dispatchEvent(new Event('submit', { cancelable: true, bubbles: true }));
              }
            }
          }
          return;
        }

        // For text inputs, checkboxes, or modal background, submit the primary form
        const form = modalRef.current?.querySelector('form');
        if (form) {
          e.preventDefault();
          if (typeof form.requestSubmit === 'function') {
            form.requestSubmit();
          } else {
            form.dispatchEvent(new Event('submit', { cancelable: true, bubbles: true }));
          }
        }
      }
    }

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [show, onClose]);

  if (!show) return null;

  return (
    <div className="modal-overlay anim-fade-in" onClick={onClose} role="presentation">
      <div
        className="modal-container anim-modal-in"
        style={{ maxWidth }}
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-label={title}
        ref={modalRef}
      >
        <div className="modal-header">
          <h2 className="modal-title">{title}</h2>
          <button
            type="button"
            className="modal-close-btn"
            onClick={onClose}
            aria-label="Close dialog"
          >
            <Icon name="x" size={18} />
          </button>
        </div>
        <div className="modal-body">{children}</div>
      </div>
    </div>
  );
}
