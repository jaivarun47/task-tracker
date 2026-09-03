import Icon from './Icon';

export default function Toast({ toast, onClose }) {
  const isError = toast.type === 'error';
  const isSuccess = toast.type === 'success';

  return (
    <div className={`toast-item toast-${toast.type} anim-toast-in`} role="alert">
      <div className="toast-icon">
        <Icon name={isError ? 'alert' : isSuccess ? 'check' : 'board'} size={16} />
      </div>
      <div className="toast-content">{toast.message}</div>
      {toast.action && (
        <button
          type="button"
          className="toast-action-btn"
          onClick={() => {
            toast.action.onClick?.();
            onClose(toast.id);
          }}
          aria-label={toast.action.label}
        >
          {toast.action.label}
        </button>
      )}
      <button
        type="button"
        className="toast-close"
        onClick={() => {
          toast.onDismiss?.();
          onClose(toast.id);
        }}
        aria-label="Dismiss notification"
      >
        <Icon name="x" size={14} />
      </button>
      {toast.duration && (
        <div
          className="toast-progress-bar"
          style={{ animationDuration: `${toast.duration}ms` }}
        />
      )}
    </div>
  );
}

export function ToastContainer({ toasts, onClose }) {
  if (!toasts || toasts.length === 0) return null;

  return (
    <div className="toast-container" aria-live="polite">
      {toasts.map((toast) => (
        <Toast key={toast.id} toast={toast} onClose={onClose} />
      ))}
    </div>
  );
}
