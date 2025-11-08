import mongoose from "mongoose";

const userSchema = new mongoose.Schema(
  {
    _id: { type: mongoose.Schema.Types.ObjectId, auto: true },
    name: { type: String, required: false },
    email: { type: String, unique: true, required: true },
    password: { type: String, required: false },
    provider: { type: String, enum: ["local", "google"], default: "local" },

    // Counters for activity
    questionsPosted: { type: Number, default: 0 },
    questionsAttempted: { type: Number, default: 0 },
  },
  {
    timestamps: true,
  }
);

export const User = mongoose.model("User", userSchema);
