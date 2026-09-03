import Icon from './Icon';
import { useTheme } from '../../hooks/useTheme';

export default function ThemeToggle({ className = '' }) {
  const { theme, toggleTheme } = useTheme();
  const isDark = theme === 'dark';

  return (
    <button
      type="button"
      className={`theme-toggle-btn ${className}`}
      onClick={toggleTheme}
      aria-label={`Switch to ${isDark ? 'light' : 'dark'} mode`}
      title={`Switch to ${isDark ? 'light' : 'dark'} mode`}
    >
      <div className={`theme-toggle-icon-wrap ${isDark ? 'is-dark' : 'is-light'}`}>
        <Icon name={isDark ? 'sun' : 'moon'} size={15} />
      </div>
      <span className="theme-toggle-text">{isDark ? 'Light' : 'Dark'}</span>
    </button>
  );
}
