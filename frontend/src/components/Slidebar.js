// src/components/Sidebar.js
import React from "react";
import "./Sidebar.css";

function Sidebar({ myQuestions, onQuestionClick }) {
  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <h3>My Questions</h3>
        <span className="question-count">{myQuestions.length}</span>
      </div>

      <div className="sidebar-list">
        {myQuestions.length === 0 ? (
          <div className="empty-state">
            <span className="empty-icon">📝</span>
            <p>No questions posted yet</p>
          </div>
        ) : (
          myQuestions.map((q) => (
            <div
              key={q._id}
              className="sidebar-item"
              onClick={() => onQuestionClick(q._id)}
            >
              <div className="sidebar-item-content">
                <h4>{q.qtitle}</h4>
                <small>{new Date(q.createdAt).toLocaleDateString()}</small>
              </div>
              <span className="arrow">→</span>
            </div>
          ))
        )}
      </div>
    </aside>
  );
}

export default Sidebar;