import React, { useState } from 'react';
import './ComponentNameModal.css';

const ComponentNameModal = ({ componentType, subtype, onConfirm, onCancel }) => {
  const [name, setName] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (name.trim()) {
      onConfirm(name.trim());
    }
  };

  const getTypeDisplay = () => {
    if (subtype) {
      return `${componentType} (${subtype.replace(/_/g, ' ')})`;
    }
    return componentType.replace(/_/g, ' ');
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content component-name-modal">
        <h2>Name Your Component</h2>
        <p className="modal-subtitle">
          Type: <strong>{getTypeDisplay()}</strong>
        </p>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="componentName">Component Name</label>
            <input
              id="componentName"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder={`e.g., User ${componentType.toLowerCase()}, Orders DB, Main Cache...`}
              autoFocus
              className="component-name-input"
            />
          </div>

          <div className="modal-actions">
            <button type="button" onClick={onCancel} className="btn btn-secondary">
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={!name.trim()}>
              Create Component
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ComponentNameModal;

