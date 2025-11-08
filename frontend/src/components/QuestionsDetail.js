// src/QuestionDetail.js
import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";
import App from "../App"; // Import the System Design Simulator
import "./QuestionsDetail.css";

const API_BASE = "http://localhost:3000";

function QuestionDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [question, setQuestion] = useState(null);
  const [loading, setLoading] = useState(true);
  const token = localStorage.getItem("token");

  useEffect(() => {
    const fetchQuestion = async () => {
      try {
        const response = await axios.get(`${API_BASE}/questions/${id}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setQuestion(response.data.question);
        setLoading(false);
      } catch (err) {
        console.error(err);
        setLoading(false);
      }
    };

    fetchQuestion();
  }, [id, token]);

  const handleSubmitDesign = async () => {
    // TODO: Implement API endpoint to submit the architecture design
    try {
      console.log("Submit design for question:", id);
      
      // Example API call structure:
      // const response = await axios.post(
      //   `${API_BASE}/architecture/submit`,
      //   {
      //     questionId: id,
      //     architectureData: {
      //       nodes: [], // Get from App.js state
      //       edges: [], // Get from App.js state
      //     }
      //   },
      //   {
      //     headers: { Authorization: `Bearer ${token}` }
      //   }
      // );
      
      alert("Design submitted successfully! (Feature to be implemented)");
    } catch (err) {
      console.error("Error submitting design:", err);
      alert("Failed to submit design. Please try again.");
    }
  };

  const scrollToCanvas = () => {
    const canvasSection = document.getElementById('design-canvas');
    if (canvasSection) {
      canvasSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  };

  if (loading) {
    return (
      <div className="detail-container">
        <div className="loading-container">
          <div className="loader"></div>
          <p>Loading question...</p>
        </div>
      </div>
    );
  }

  if (!question) {
    return (
      <div className="detail-container">
        <div className="error-container">
          <h2>Question not found</h2>
          <button className="btn-back" onClick={() => navigate("/home")}>
            Back to Home
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="detail-container">
      <button className="btn-back" onClick={() => navigate("/home")}>
        ← Back
      </button>

      {/* Question Details Section */}
      <div className="detail-card">
        <div className="detail-header">
          <h1>{question.qtitle}</h1>
          <div className="author-section">
            <span className="author-avatar-large">
              {question.uid?.name?.charAt(0).toUpperCase() || "U"}
            </span>
            <div>
              <p className="author-name-large">{question.uid?.name || "Unknown"}</p>
              <small className="post-date-large">
                Posted on {new Date(question.createdAt).toLocaleString()}
              </small>
            </div>
          </div>
        </div>

        {question.qimg && (
          <div className="detail-image">
            <img
              src={`${API_BASE}/uploads/${question.qimg}`}
              alt={question.qtitle}
            />
          </div>
        )}

        <div className="detail-description">
          <h3>Description</h3>
          <p>{question.qdes}</p>
        </div>

        <div className="action-prompt">
          <p>Ready to design your solution?</p>
          <button 
            className="btn-scroll-to-canvas"
            onClick={scrollToCanvas}
          >
            Start Designing Below ↓
          </button>
        </div>
      </div>

      {/* Design Canvas Section */}
      <div className="canvas-section" id="design-canvas">
        <div className="canvas-header">
          <div className="canvas-title">
            <h2>🏗️ Design Your Solution</h2>
            <p>Create your system architecture by dragging components onto the canvas</p>
          </div>
          <button 
            className="btn-submit-design"
            onClick={handleSubmitDesign}
          >
            Submit Design
          </button>
        </div>
        
        <div className="simulator-wrapper">
          <App />
        </div>
      </div>
    </div>
  );
}

export default QuestionDetail;