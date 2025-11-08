import React, { useState, useEffect } from 'react';
import { componentAPI } from '../api';
import './SubtypeModal.css';

const SubtypeModal = ({ componentType, onSelect, onCancel }) => {
  const [subtypes, setSubtypes] = useState([]);
  const [selectedSubtype, setSelectedSubtype] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadSubtypes();
  }, [componentType]);

  const loadSubtypes = async () => {
    setLoading(true);
    try {
      const response = await componentAPI.getSubtypes(componentType);
      const availableSubtypes = response.data.subtypes || [];
      setSubtypes(availableSubtypes);
      if (availableSubtypes.length > 0) {
        setSelectedSubtype(availableSubtypes[0]);
      }
    } catch (error) {
      console.error('Failed to load subtypes:', error);
      setSubtypes(['default']);
      setSelectedSubtype('default');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirm = () => {
    if (selectedSubtype) {
      onSelect(selectedSubtype);
    }
  };

  const formatSubtypeName = (subtype) => {
    return subtype
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, (c) => c.toUpperCase());
  };

  const getSubtypeDescription = (type, subtype) => {
    const descriptions = {
      DATABASE: {
        SQL: 'Relational database with ACID properties',
        NOSQL: 'Non-relational database for flexible schemas',
        IN_MEMORY: 'Fast, memory-based database',
        COLUMN_STORE: 'Optimized for analytical queries',
        DOCUMENT_DB: 'Store and query JSON-like documents',
        GRAPH_DB: 'Optimized for graph relationships',
      },
      CACHE: {
        IN_MEMORY: 'Fast in-memory cache',
        DISTRIBUTED: 'Scalable distributed cache',
        LOCAL: 'Simple local cache',
      },
      API_SERVICE: {
        REST: 'RESTful API with HTTP methods',
        GRAPHQL: 'Flexible query language for APIs',
        GRPC: 'High-performance RPC framework',
        SOAP: 'XML-based web service protocol',
      },
      QUEUE: {
        MESSAGE_QUEUE: 'Async message queue',
        TASK_QUEUE: 'Background task processing',
        STREAM: 'Real-time event streaming',
        EVENT_BUS: 'Pub/sub event distribution',
      },
      STORAGE: {
        BLOCK_STORAGE: 'Raw block-level storage',
        OBJECT_STORAGE: 'Scalable object storage',
        FILE_STORAGE: 'Network file system',
      },
      LOAD_BALANCER: {
        ROUND_ROBIN: 'Distribute evenly across servers',
        LEAST_CONNECTIONS: 'Route to least busy server',
        IP_HASH: 'Consistent routing by client IP',
        WEIGHTED: 'Custom weight distribution',
      },
    };

    return descriptions[type]?.[subtype] || 'Component subtype';
  };

  return (
    <div className="subtype-modal-overlay" onClick={onCancel}>
      <div className="subtype-modal" onClick={(e) => e.stopPropagation()}>
        <div className="subtype-modal-header">
          <h3>Select {componentType.replace('_', ' ')} Type</h3>
          <button className="modal-close-btn" onClick={onCancel}>×</button>
        </div>

        <div className="subtype-modal-content">
          {loading ? (
            <div className="loading-spinner">Loading options...</div>
          ) : (
            <>
              <p className="subtype-hint">
                Choose the specific type for your {componentType.toLowerCase().replace('_', ' ')}:
              </p>

              <div className="subtype-options">
                {subtypes.map((subtype) => (
                  <div
                    key={subtype}
                    className={`subtype-option ${selectedSubtype === subtype ? 'selected' : ''}`}
                    onClick={() => setSelectedSubtype(subtype)}
                  >
                    <div className="subtype-radio">
                      {selectedSubtype === subtype && <div className="radio-dot" />}
                    </div>
                    <div className="subtype-info">
                      <div className="subtype-name">{formatSubtypeName(subtype)}</div>
                      <div className="subtype-description">
                        {getSubtypeDescription(componentType, subtype)}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>

        <div className="subtype-modal-footer">
          <button className="btn btn-secondary" onClick={onCancel}>
            Cancel
          </button>
          <button
            className="btn btn-primary"
            onClick={handleConfirm}
            disabled={!selectedSubtype || loading}
          >
            Add Component
          </button>
        </div>
      </div>
    </div>
  );
};

export default SubtypeModal;

