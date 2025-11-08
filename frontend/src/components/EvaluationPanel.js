import React from 'react';
import './EvaluationPanel.css';

const EvaluationPanel = ({ evaluation, onClose }) => {
  if (!evaluation) return null;

  const renderParameterScores = (scores) => {
    if (!scores || Object.keys(scores).length === 0) return null;

    const parameterLabels = {
      LATENCY: 'Latency',
      COST: 'Cost',
      AVAILABILITY: 'Availability',
      CONSISTENCY: 'Consistency',
      SECURITY: 'Security',
      DURABILITY: 'Durability',
      SCALABILITY: 'Scalability',
      THROUGHPUT: 'Throughput',
      MAINTAINABILITY: 'Maintainability',
      ENERGY_EFFICIENCY: 'Energy Efficiency'
    };

    return (
      <div className="scores-grid">
        {Object.entries(scores).map(([key, value]) => (
          <div key={key} className="score-card">
            <div className="score-label">{parameterLabels[key] || key.replace('_', ' ')}</div>
            <div className="score-bar">
              <div
                className="score-fill"
                style={{
                  width: `${(value / 10) * 100}%`,
                  backgroundColor: value >= 7 ? '#10b981' : value >= 4 ? '#f59e0b' : '#ef4444'
                }}
              />
            </div>
            <div className="score-value">{value.toFixed(2)}/10</div>
          </div>
        ))}
      </div>
    );
  };

  const renderBottlenecks = (bottlenecks) => {
    if (!bottlenecks || bottlenecks.length === 0) return null;

    return (
      <div className="bottlenecks-section">
        <h3>⚠️ Bottleneck Analysis</h3>
        <div className="bottlenecks-list">
          {bottlenecks.map((bottleneck, index) => (
            <div key={index} className="bottleneck-card">
              <div className="bottleneck-header">
                <span className="bottleneck-name">{bottleneck.componentName}</span>
                <span className="bottleneck-type">{bottleneck.componentType}</span>
              </div>
              <div className="bottleneck-details">
                <div className="bottleneck-stat">
                  <span className="stat-label">Total Connections:</span>
                  <span className="stat-value">{bottleneck.totalConnections}</span>
                </div>
                <div className="bottleneck-stat">
                  <span className="stat-label">Incoming:</span>
                  <span className="stat-value">{bottleneck.incomingConnections}</span>
                </div>
                <div className="bottleneck-stat">
                  <span className="stat-label">Outgoing:</span>
                  <span className="stat-value">{bottleneck.outgoingConnections}</span>
                </div>
                <div className="bottleneck-stat">
                  <span className="stat-label">Risk Score:</span>
                  <span className={`stat-value ${bottleneck.bottleneckScore < 0.5 ? 'high-risk' : 'medium-risk'}`}>
                    {(bottleneck.bottleneckScore * 100).toFixed(0)}%
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  };

  const renderInsights = (insights) => {
    if (!insights || insights.length === 0) return null;

    return (
      <div className="insights-section">
        <h3>💡 Insights & Recommendations</h3>
        <ul className="insights-list">
          {insights.map((insight, index) => (
            <li key={index} className={
              insight.includes('✅') ? 'insight-success' :
              insight.includes('⚠') || insight.includes('❌') ? 'insight-warning' :
              insight.includes('💡') ? 'insight-info' :
              'insight-neutral'
            }>
              {insight}
            </li>
          ))}
        </ul>
      </div>
    );
  };

  return (
    <div className="evaluation-overlay">
      <div className="evaluation-panel">
        <div className="evaluation-header">
          <h2>📊 Architecture Evaluation</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>

        <div className="evaluation-content">
          {/* Overall Score */}
          {evaluation.overallScore !== undefined && (
            <div className="overall-score-section">
              <h3>Overall Score</h3>
              <div className="overall-score">
                <div className="score-circle">
                  <svg width="120" height="120">
                    <circle
                      cx="60"
                      cy="60"
                      r="50"
                      fill="none"
                      stroke="#e5e7eb"
                      strokeWidth="10"
                    />
                    <circle
                      cx="60"
                      cy="60"
                      r="50"
                      fill="none"
                      stroke="#3b82f6"
                      strokeWidth="10"
                      strokeDasharray={`${(evaluation.overallScore / 10) * 314} 314`}
                      strokeDashoffset="0"
                      transform="rotate(-90 60 60)"
                    />
                  </svg>
                  <div className="score-text">
                    {evaluation.overallScore.toFixed(2)}
                    <span>/10</span>
                  </div>
                </div>
                <div className="score-label-main">
                  {evaluation.overallScore >= 7 ? 'Excellent' :
                   evaluation.overallScore >= 5 ? 'Good' :
                   evaluation.overallScore >= 3 ? 'Fair' : 'Poor'}
                </div>
              </div>
            </div>
          )}

          {/* Architecture Metrics */}
          <div className="section">
            <h3>Architecture Metrics</h3>
            <div className="metrics-grid">
              <div className="metric-card">
                <div className="metric-label">Components</div>
                <div className="metric-value">{evaluation.componentCount || 0}</div>
              </div>
              <div className="metric-card">
                <div className="metric-label">Links</div>
                <div className="metric-value">{evaluation.linkCount || 0}</div>
              </div>
              <div className="metric-card">
                <div className="metric-label">Valid</div>
                <div className="metric-value">{evaluation.valid ? '✅ Yes' : '❌ No'}</div>
              </div>
            </div>
          </div>

          {/* Parameter Scores */}
          {evaluation.parameterScores && Object.keys(evaluation.parameterScores).length > 0 && (
            <div className="section">
              <h3>Parameter-Wise Scores</h3>
              {renderParameterScores(evaluation.parameterScores)}
            </div>
          )}

          {/* Bottlenecks */}
          {evaluation.bottlenecks && evaluation.bottlenecks.length > 0 &&
            renderBottlenecks(evaluation.bottlenecks)
          }

          {/* Insights */}
          {evaluation.insights && evaluation.insights.length > 0 &&
            renderInsights(evaluation.insights)
          }

          {/* Validation Violations */}
          {evaluation.violations && evaluation.violations.length > 0 && (
            <div className="section">
              <h3>⚠️ Validation Violations</h3>
              <ul className="violations-list">
                {evaluation.violations.map((violation, index) => (
                  <li key={index} className="violation-item">{violation}</li>
                ))}
              </ul>
            </div>
          )}

          {/* Warnings */}
          {evaluation.warnings && evaluation.warnings.length > 0 && (
            <div className="section">
              <h3>⚡ Warnings</h3>
              <ul className="warnings-list">
                {evaluation.warnings.map((warning, index) => (
                  <li key={index} className="warning-item">{warning}</li>
                ))}
              </ul>
            </div>
          )}
        </div>

        <div className="evaluation-footer">
          <button className="btn btn-primary" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

export default EvaluationPanel;
