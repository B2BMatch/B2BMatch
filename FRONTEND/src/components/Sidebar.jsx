import React from 'react';

export const Sidebar = ({ children }) => {
  return (
    <aside style={{ width: '280px', padding: '20px', background: 'var(--bg-card)', borderRadius: '12px' }}>
      {children}
    </aside>
  );
};

export default Sidebar;