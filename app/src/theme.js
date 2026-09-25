// Cedar light/dark theme: data-theme on <html> is the source of truth (set before
// paint in index.html). Canvas and WebGL can't use CSS, so they read tokens here
// and re-render on the 'themechange' event.
const STORAGE_KEY = 'deskforge_theme';
const THEME_COLORS = { light: '#ebe7df', dark: '#161614' };

export const getTheme = () =>
  document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';

export function setTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme);
  const themeMeta = document.querySelector('meta[name="theme-color"]');
  if (themeMeta) themeMeta.setAttribute('content', THEME_COLORS[theme]);
  try {
    localStorage.setItem(STORAGE_KEY, theme);
  } catch {
    // Storage can be unavailable (private mode); the toggle still works for this visit.
  }
  window.dispatchEvent(new CustomEvent('themechange', { detail: { theme } }));
}

/** Read a CSS custom property from :root, e.g. cssVar('--scene-bg'). */
export const cssVar = (name) =>
  getComputedStyle(document.documentElement).getPropertyValue(name).trim();

export function initThemeToggle(button) {
  const render = () => {
    const dark = getTheme() === 'dark';
    const label = dark ? 'Switch to light theme' : 'Switch to dark theme';
    button.setAttribute('aria-label', label);
    button.title = label;
    button.innerHTML = dark
      ? '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41"/></svg>'
      : '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/></svg>';
  };
  button.addEventListener('click', () => {
    setTheme(getTheme() === 'dark' ? 'light' : 'dark');
    render();
  });
  render();
}
