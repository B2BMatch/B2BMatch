import React, { useEffect, useRef, useState } from 'react';
import '../styles/sidebarFilters.css';

export const SidebarFilters = ({ categories = [], value = '', onChange }) => {
  const [open, setOpen] = useState(false);
  const containerRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (containerRef.current && !containerRef.current.contains(event.target)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const selected = categories.find((category) => String(category.id) === String(value));
  const selectedLabel = value !== '' && selected ? selected.name : 'Todas';

  const handleSelect = (categoryId) => {
    onChange(categoryId);
    setOpen(false);
  };

  return (
    <aside className="sidebar-filters">
      <h3>Filtros</h3>
      <hr style={{ borderColor: 'var(--border-color)' }} />
      <div>
        <label className="filter-label" htmlFor="category-filter">Categoría</label>
        <div className="custom-select" ref={containerRef}>
          <button
            id="category-filter"
            type="button"
            className={`select-trigger ${open ? 'is-open' : ''}`}
            aria-haspopup="listbox"
            aria-expanded={open}
            onClick={() => setOpen((prev) => !prev)}
          >
            <span className="select-trigger__label">{selectedLabel}</span>
            <svg
              className="select-trigger__chevron"
              width="12"
              height="12"
              viewBox="0 0 12 12"
              aria-hidden="true"
            >
              <path d="M2 4l4 4 4-4" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </button>
          {open && (
            <ul className="select-options" role="listbox" aria-labelledby="category-filter">
              <li key="all" role="option" aria-selected={value === ''}>
                <button
                  type="button"
                  className={`select-option ${value === '' ? 'is-selected' : ''}`}
                  onClick={() => handleSelect('')}
                >
                  Todas
                </button>
              </li>
              {categories.map((category) => {
                const isSelected = String(category.id) === String(value);
                return (
                  <li key={category.id} role="option" aria-selected={isSelected}>
                    <button
                      type="button"
                      className={`select-option ${isSelected ? 'is-selected' : ''}`}
                      onClick={() => handleSelect(category.id)}
                    >
                      {category.name}
                    </button>
                  </li>
                );
              })}
            </ul>
          )}
        </div>
      </div>
    </aside>
  );
};

export default SidebarFilters;
