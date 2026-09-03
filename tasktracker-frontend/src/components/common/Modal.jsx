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
