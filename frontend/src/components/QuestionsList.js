// src/components/QuestionsList.js
import React from "react";
import "./QuestionsList.css";

function QuestionsList({ questions, loading, onQuestionClick }) {
  if (loading) {
    return (
      <main className="questions-main">
        <div className="loading-container">
          <div className="loader"></div>
          <p>Loading questions...</p>
        </div>
      </main>
    );
  }

  return (
    <main className="questions-main">
      <div className="questions-header">
        <h2>All Questions</h2>
        <span className="total-count">{questions.length} questions</span>
      </div>

      <div className="questions-grid">
        {questions.length === 0 ? (
          <div className="empty-state-large">
            <span className="empty-icon-large">💭</span>
            <h3>No questions yet</h3>
            <p>Be the first to post a question!</p>
          </div>
        ) : (
          questions.map((q) => (
            <div
              key={q._id}
              className="question-card"
              onClick={() => onQuestionClick(q._id)}
            >
              <div className="question-card-header">
                <h3>{q.qtitle}</h3>
              </div>
              <div className="question-card-footer">
                <div className="author-info">
                  <span className="author-avatar">
                    {q.uid?.name?.charAt(0).toUpperCase() || "U"}
                  </span>
                  <div>
                    <p className="author-name">{q.uid?.name || "Unknown"}</p>
                    <small className="post-date">
                      {new Date(q.createdAt).toLocaleDateString()}
                    </small>
                  </div>
                </div>
                <span className="view-arrow">→</span>
              </div>
            </div>
          ))
        )}
      </div>
    </main>
  );
}

export default QuestionsList;