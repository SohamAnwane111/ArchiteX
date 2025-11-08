import React, { useState } from 'react';
import './LinkTypeModal.css';

const LinkTypeModal = ({ linkTypes, sourceNode, targetNode, onSelect, onCancel }) => {
  const [selectedType, setSelectedType] = useState(linkTypes[0]);

  const handleSubmit = () => {
    if (selectedType) {
      onSelect(selectedType);
    }
  };

  const getLinkTypeDescription = (type) => {
    const descriptions = {
      API_CALL: 'Synchronous request-response communication',
      STREAM: 'Real-time data streaming and processing',
      REPLICATION: 'Data replication for redundancy and availability',
      ETL_PIPELINE: 'Extract, Transform, Load data pipeline',
      BATCH_TRANSFER: 'Batch data transfer for large datasets',
      EVENT_FLOW: 'Asynchronous event-driven communication',
      CACHE_LOOKUP: 'Cache read/write operations',
      DATABASE_QUERY: 'Database read/write operations'
    };
    return descriptions[type] || 'Connection between components';
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content link-type-modal">
        <h2>Select Link Type</h2>
        <p className="modal-subtitle">
          Connecting: <strong>{sourceNode.data.label}</strong> → <strong>{targetNode.data.label}</strong>
        </p>

        <div className="link-types-list">
          {linkTypes.map((type) => (
            <div
              key={type}
              className={`link-type-option ${selectedType === type ? 'selected' : ''}`}
              onClick={() => setSelectedType(type)}
            >
              <div className="link-type-header">
                <input
                  type="radio"
                  name="linkType"
                  value={type}
                  checked={selectedType === type}
                  onChange={() => setSelectedType(type)}
                />
                <span className="link-type-name">{type.replace(/_/g, ' ')}</span>
              </div>
              <p className="link-type-description">{getLinkTypeDescription(type)}</p>
            </div>
          ))}
        </div>

        <div className="modal-actions">
          <button onClick={onCancel} className="btn btn-secondary">
            Cancel
          </button>
          <button onClick={handleSubmit} className="btn btn-primary" disabled={!selectedType}>
            Create Connection
          </button>
        </div>
      </div>
    </div>
  );
};

export default LinkTypeModal;

