import React from 'react';
import './Sidebar.css';

const Sidebar = ({ selectedNode, selectedEdge, onDeleteNode, onDeleteEdge, previewedSubtype }) => {
  // If a preview is active, prefer it — this ensures heuristics from the palette
  // are immediately visible even if a canvas node was previously selected.
  const isPreviewActive = !!previewedSubtype;
  const previewNode = previewedSubtype ? {
    id: `preview-${previewedSubtype.componentType}-${previewedSubtype.subtype.id || previewedSubtype.subtype.name}`,
    data: {
      label: previewedSubtype.subtype.label || previewedSubtype.subtype.name,
      heuristics: previewedSubtype.subtype.heuristics || '',
      componentType: previewedSubtype.componentType,
      properties: { subtype: previewedSubtype.subtype.id || previewedSubtype.subtype.name },
    }
  } : null;

  const effectiveNode = isPreviewActive ? previewNode : selectedNode || null;

  if (!effectiveNode && !selectedEdge) {
    return (
      <div className="sidebar">
        <div className="sidebar-empty">
          <p>👈 Select a component or connection to view details</p>
        </div>
      </div>
    );
  }

  if (effectiveNode) {
    const node = effectiveNode;
    const heuristics = node.data?.heuristics;
    const props = node.data?.properties || {};

    return (
      <div className="sidebar">
        <div className="sidebar-header">
          <h3>Component Details</h3>
        </div>
        <div className="sidebar-content">
          <div className="detail-section">
            <label>Name:</label>
            <div className="detail-value">{node.data.label}</div>
          </div>
          <div className="detail-section">
            <label>Type:</label>
            <div className="detail-value badge">{node.data.componentType}</div>
          </div>
          <div className="detail-section">
            <label>ID:</label>
            <div className="detail-value code">{node.data.componentId || 'preview'}</div>
          </div>

          {heuristics && typeof heuristics === 'object' && heuristics.scores && (
            <div className="detail-section">
              <label>Heuristics:</label>
              <div className="heuristics-grid">
                {Object.entries(heuristics.scores || {}).map(([key, value]) => (
                  <div key={key} className="heuristic-item">
                    <span className="heuristic-name">{key.replace('_', ' ')}</span>
                    <div className="heuristic-bar">
                      <div
                        className="heuristic-fill"
                        style={{ width: `${(value / 10) * 100}%` }}
                      />
                    </div>
                    <span className="heuristic-value">{Number(value).toFixed(1)}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {heuristics && typeof heuristics === 'string' && (
            <div className="detail-section">
              <label>Heuristics:</label>
              <div className="heuristics-text">{heuristics}</div>
            </div>
          )}

          {props && Object.keys(props).length > 0 && (
            <div className="detail-section">
              <label>Properties:</label>
              <div className="properties-list">
                {Object.entries(props).map(([key, value]) => (
                  <div key={key} className="property-item">
                    <span className="property-key">{key}:</span>
                    <span className="property-value">{JSON.stringify(value)}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Always show heuristics for preview nodes (even if empty) with a helpful fallback message */}
          {isPreviewActive && (
            <div className="detail-section">
              <label>Heuristics:</label>
              {heuristics && typeof heuristics === 'object' && heuristics.scores ? (
                <div className="heuristics-grid">
                  {Object.entries(heuristics.scores || {}).map(([key, value]) => (
                    <div key={key} className="heuristic-item">
                      <span className="heuristic-name">{key.replace('_', ' ')}</span>
                      <div className="heuristic-bar">
                        <div
                          className="heuristic-fill"
                          style={{ width: `${(value / 10) * 100}%` }}
                        />
                      </div>
                      <span className="heuristic-value">{Number(value).toFixed(1)}</span>
                    </div>
                  ))}
                </div>
              ) : heuristics && typeof heuristics === 'string' ? (
                <div className="heuristics-text">{heuristics}</div>
              ) : (
                <div className="heuristics-text">No heuristics available for this subtype.</div>
              )}
            </div>
          )}
        </div>
        <div className="sidebar-actions">
          {!isPreviewActive && (
            <button
              className="btn btn-danger btn-block"
              onClick={() => onDeleteNode(node.id)}
            >
              🗑️ Delete Component
            </button>
          )}
        </div>
      </div>
    );
  }

  if (selectedEdge) {
    const edge = selectedEdge;
    const eHeur = edge.data?.heuristics;
    return (
      <div className="sidebar">
        <div className="sidebar-header">
          <h3>Connection Details</h3>
        </div>
        <div className="sidebar-content">
          <div className="detail-section">
            <label>Link Type:</label>
            <div className="detail-value badge">{edge.data?.linkType || 'Unknown'}</div>
          </div>
          <div className="detail-section">
            <label>ID:</label>
            <div className="detail-value code">{edge.data?.linkId || edge.id}</div>
          </div>

          {eHeur && typeof eHeur === 'object' && eHeur.scores && (
            <div className="detail-section">
              <label>Link Heuristics:</label>
              <div className="heuristics-grid">
                {Object.entries(eHeur.scores || {}).map(([key, value]) => (
                  <div key={key} className="heuristic-item">
                    <span className="heuristic-name">{key.replace('_', ' ')}</span>
                    <div className="heuristic-bar">
                      <div
                        className="heuristic-fill"
                        style={{ width: `${(value / 10) * 100}%` }}
                      />
                    </div>
                    <span className="heuristic-value">{Number(value).toFixed(1)}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {eHeur && typeof eHeur === 'string' && (
            <div className="detail-section">
              <label>Link Heuristics:</label>
              <div className="heuristics-text">{eHeur}</div>
            </div>
          )}
        </div>
        <div className="sidebar-actions">
          <button
            className="btn btn-danger btn-block"
            onClick={() => onDeleteEdge(edge.id)}
          >
            🗑️ Delete Connection
          </button>
        </div>
      </div>
    );
  }

  return null;
};

export default Sidebar;
