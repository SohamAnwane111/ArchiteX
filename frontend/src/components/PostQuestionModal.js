// src/components/PostQuestionModal.js
import React, { useState } from "react";
import axios from "axios";
import "./PostQuestionModal.css";

const API_BASE = "http://localhost:3000";

function PostQuestionModal({ onClose, onSuccess, token }) {
  const [qtitle, setQtitle] = useState("");
  const [qdes, setQdes] = useState("");
  const [qimg, setQimg] = useState(null);
  const [preview, setPreview] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setQimg(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreview(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!qtitle || !qdes || !qimg) {
      setError("Please fill all fields and select an image");
      return;
    }

    try {
      setLoading(true);
      const formData = new FormData();
      formData.append("qtitle", qtitle);
      formData.append("qdes", qdes);
      formData.append("qimg", qimg);

      await axios.post(`${API_BASE}/questions`, formData, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "multipart/form-data",
        },
      });

      onSuccess();
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.error || "Failed to post question");
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Post New Question</h2>
          <button className="close-btn" onClick={onClose}>
            ✕
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          {error && <div className="modal-error">{error}</div>}

          <div className="form-group">
            <label>Question Title</label>
            <input
              type="text"
              value={qtitle}
              onChange={(e) => setQtitle(e.target.value)}
              placeholder="Enter a clear, concise title"
              required
            />
          </div>

          <div className="form-group">
            <label>Description</label>
            <textarea
              value={qdes}
              onChange={(e) => setQdes(e.target.value)}
              rows={4}
              placeholder="Provide detailed description..."
              required
            />
          </div>

          <div className="form-group">
            <label>Image</label>
            <div className="file-input-wrapper">
              <input
                type="file"
                accept="image/*"
                onChange={handleImageChange}
                required
                id="file-input"
              />
              <label htmlFor="file-input" className="file-input-label">
                {preview ? "Change Image" : "Choose Image"}
              </label>
            </div>
            {preview && (
              <div className="image-preview">
                <img src={preview} alt="Preview" />
              </div>
            )}
          </div>

          <div className="modal-actions">
            <button
              type="button"
              className="btn-cancel"
              onClick={onClose}
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn-submit"
              disabled={loading}
            >
              {loading ? "Posting..." : "Post Question"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default PostQuestionModal;