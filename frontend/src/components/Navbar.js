// src/components/Navbar.js
import React from "react";
import { useNavigate } from "react-router-dom";
import "./Navbar.css";

function Navbar({ onRandomQuestion, onPostQuestion }) {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/");
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <h1>Architex</h1>
        <span className="navbar-subtitle">Community</span>
      </div>

      <div className="navbar-actions">
        <button className="btn-nav btn-random" onClick={onRandomQuestion}>
          <span className="icon">🎲</span>
          Random Question
        </button>
        <button className="btn-nav btn-post" onClick={onPostQuestion}>
          <span className="icon">➕</span>
          Post Question
        </button>
        <button className="btn-nav btn-logout" onClick={handleLogout}>
          <span className="icon">🚪</span>
          Logout
        </button>
      </div>
    </nav>
  );
}

export default Navbar;