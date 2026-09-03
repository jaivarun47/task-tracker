export default function Icon({ name, size = 16, className = '', ...props }) {
  const icons = {
    menu: (
      <>
        <line x1="3" y1="6" x2="21" y2="6" />
        <line x1="3" y1="12" x2="21" y2="12" />
        <line x1="3" y1="18" x2="21" y2="18" />
      </>
    ),
    plus: (
      <path d="M12 5v14M5 12h14" />
    ),
    edit: (
      <path d="M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z" />
    ),
    trash: (
      <>
        <polyline points="3 6 5 6 21 6" />
        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
        <line x1="10" y1="11" x2="10" y2="17" />
        <line x1="14" y1="11" x2="14" y2="17" />
      </>
    ),
    x: (
      <path d="M18 6L6 18M6 6l12 12" />
    ),
    check: (
      <polyline points="20 6 9 17 4 12" />
    ),
    grip: (
      <>
        <circle cx="9" cy="5" r="1" fill="currentColor" />
        <circle cx="9" cy="12" r="1" fill="currentColor" />
        <circle cx="9" cy="19" r="1" fill="currentColor" />
        <circle cx="15" cy="5" r="1" fill="currentColor" />
        <circle cx="15" cy="12" r="1" fill="currentColor" />
        <circle cx="15" cy="19" r="1" fill="currentColor" />
      </>
    ),
    chevronLeft: (
      <polyline points="15 18 9 12 15 6" />
    ),
    chevronRight: (
      <polyline points="9 18 15 12 9 6" />
    ),
    chevronDown: (
      <polyline points="6 9 12 15 18 9" />
    ),
    chevronUp: (
      <polyline points="18 15 12 9 6 15" />
    ),
    board: (
      <>
        <rect x="3" y="3" width="18" height="18" rx="2" />
        <path d="M9 3v18M15 3v18" />
      </>
    ),
    columns: (
      <>
        <rect x="4" y="4" width="6" height="16" rx="1" />
        <rect x="14" y="4" width="6" height="16" rx="1" />
      </>
    ),
    alert: (
      <>
        <circle cx="12" cy="12" r="10" />
        <line x1="12" y1="8" x2="12" y2="12" />
        <line x1="12" y1="16" x2="12.01" y2="16" />
      </>
    ),
    spinner: (
      <path d="M21 12a9 9 0 1 1-6.219-8.56" />
    ),
    sun: (
      <>
        <circle cx="12" cy="12" r="5" />
        <line x1="12" y1="1" x2="12" y2="3" />
        <line x1="12" y1="21" x2="12" y2="23" />
        <line x1="4.22" y1="4.22" x2="5.64" y2="5.64" />
        <line x1="18.36" y1="18.36" x2="19.78" y2="19.78" />
        <line x1="1" y1="12" x2="3" y2="12" />
        <line x1="21" y1="12" x2="23" y2="12" />
        <line x1="4.22" y1="19.78" x2="5.64" y2="18.36" />
        <line x1="18.36" y1="5.64" x2="19.78" y2="4.22" />
      </>
    ),
    moon: (
      <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
    ),
  };

  const svgContent = icons[name] || null;

  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={`icon ${className} ${name === 'spinner' ? 'icon-spin' : ''}`}
      aria-hidden="true"
      {...props}
    >
      {svgContent}
    </svg>
  );
}
