// src/Home.js
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Navbar from "./components/Navbar";
import Sidebar from "./components/Sidebar";
import QuestionsList from "./components/QuestionsList";
import PostQuestionModal from "./components/PostQuestionModal";
import App from "./App"; // Import your existing App.js directly
import "./Home.css";

const API_BASE = "http://localhost:3000";

function Home() {
  const [questions, setQuestions] = useState([]);
  const [myQuestions, setMyQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [showSimulator, setShowSimulator] = useState(false);
  const [selectedQuestion, setSelectedQuestion] = useState(null);
  const navigate = useNavigate();

  const token = localStorage.getItem("token");

  // Fetch all questions
  const fetchQuestions = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_BASE}/questions`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setQuestions(response.data.questions);
      setLoading(false);
    } catch (err) {
      console.error(err);
      setLoading(false);
    }
  };

  // Fetch user's own questions
  const fetchMyQuestions = async () => {
    try {
      const response = await axios.get(`${API_BASE}/questions/my`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setMyQuestions(response.data.questions);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    if (!token) {
      navigate("/");
      return;
    }
    fetchQuestions();
    fetchMyQuestions();
  }, [token, navigate]);

  const handleRandomQuestion = () => {
    if (questions.length > 0) {
      const randomIndex = Math.floor(Math.random() * questions.length);
      navigate(`/question/${questions[randomIndex]._id}`);
    }
  };

  const handleQuestionClick = (questionId) => {
    navigate(`/question/${questionId}`);
  };

  const handlePostSuccess = () => {
    setShowModal(false);
    fetchQuestions();
    fetchMyQuestions();
  };

  const handleSubmitDesign = async () => {
    // TODO: Implement API endpoint to submit the architecture design
    try {
      console.log("Submit design functionality - to be implemented");
      
      // Example of what the API call might look like:
      // const response = await axios.post(
      //   `${API_BASE}/architecture/submit`,
      //   {
      //     architectureData: simulatorState,
      //     questionId: selectedQuestion?._id
      //   },
      //   {
      //     headers: { Authorization: `Bearer ${token}` }
      //   }
      // );
      
      alert("Design submission will be implemented soon!");
    } catch (err) {
      console.error("Error submitting design:", err);
      alert("Failed to submit design. Please try again.");
    }
  };

  // If simulator is open, render it fullscreen
  if (showSimulator) {
    return (
      <div className="simulator-fullscreen">
        <div className="simulator-header">
          <button 
            className="btn-close-simulator"
            onClick={() => setShowSimulator(false)}
          >
            ← Back to Questions
          </button>
          <button 
            className="btn-submit-design"
            onClick={handleSubmitDesign}
          >
            Submit Design
          </button>
        </div>
        {/* Your existing App.js component renders here */}
        <App />
      </div>
    );
  }

  return (
    <div className="home-wrapper">
      <Navbar
        onRandomQuestion={handleRandomQuestion}
        onPostQuestion={() => setShowModal(true)}
        onOpenSimulator={() => setShowSimulator(true)}
      />
      
      <div className="home-layout">
        <Sidebar
          myQuestions={myQuestions}
          onQuestionClick={handleQuestionClick}
        />
        
        <QuestionsList
          questions={questions}
          loading={loading}
          onQuestionClick={handleQuestionClick}
        />
      </div>

      {showModal && (
        <PostQuestionModal
          onClose={() => setShowModal(false)}
          onSuccess={handlePostSuccess}
          token={token}
        />
      )}
    </div>
  );
}

export default Home;