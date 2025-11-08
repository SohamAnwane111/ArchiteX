import mongoose from "mongoose";

const questionSchema = new mongoose.Schema(
  {
    uid: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
    qtitle: { type: String, required: true },   // Added question title
    qdes: { type: String, required: true },
    qimg: { type: String, required: true },     // Base64 image string or filename
  },
  {
    timestamps: true,
  }
);

export const Question = mongoose.model("Question", questionSchema);
