import React from 'react';
import '../styles/searchbar.css';

export const SearchBar = ({ placeholder, value, onChange }) => {
  return (
    <div className="searchbar-container">
      <span>🔍</span>
      <input 
        type="text" 
        className="searchbar-input" 
        placeholder={placeholder || "Buscar..."} 
        value={value} 
        onChange={onChange} 
      />
    </div>
  );
};

export default SearchBar;