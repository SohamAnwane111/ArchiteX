import express from "express";
import mongoose from "mongoose";
import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";
import cors from "cors";
import multer from "multer";
import dotenv from "dotenv";

import { User } from "./models/User.js";
import { Question } from "./models/Question.js";

dotenv.config();
const app = express();
app.use(express.json());
app.use(cors());
app.use("/uploads", express.static("uploads")); 

// ---------------- MongoDB Connection ----------------
mongoose
  .connect(process.env.MONGO_URI)
  .then(() => console.log("✅ Connected to MongoDB Atlas"))
  .catch((err) => console.error("❌ MongoDB connection error:", err));

// ---------------- Signup Route ----------------
app.post("/signup", async (req, res) => {
  try {
    const { name, email, password } = req.body;
    if (!email || !password)
      return res.status(400).json({ error: "Email and password are required" });

    const existingUser = await User.findOne({ email });
    if (existingUser)
      return res.status(400).json({ error: "User already exists" });

    const hashedPassword = await bcrypt.hash(password, 10);
    const newUser = new User({ name, email, password: hashedPassword });
    await newUser.save();

    res.status(201).json({ message: "User created successfully", user: newUser });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Server error" });
  }
});

// ---------------- Signin Route ----------------
app.post("/signin", async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password)
      return res.status(400).json({ error: "Email and password are required" });

    const user = await User.findOne({ email });
    if (!user) return res.status(400).json({ error: "Invalid email or password" });

    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) return res.status(400).json({ error: "Invalid email or password" });

    const token = jwt.sign({ id: user._id }, process.env.JWT_SECRET, { expiresIn: "7d" });

    res.status(200).json({
      message: "Login successful",
      token,
      user: { id: user._id, name: user.name, email: user.email },
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Server error" });
  }
});

// ---------------- Multer Setup ----------------
const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, "uploads/"),
  filename: (req, file, cb) => cb(null, Date.now() + "-" + file.originalname),
});
const upload = multer({ storage });

// ---------------- Auth Middleware ----------------
function isAuthenticated(req, res, next) {
  if (!req.headers.authorization)
    return res.status(401).json({ error: "Not logged in" });

  const token = req.headers.authorization.split(" ")[1];
  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.user = decoded; // Contains user id
    next();
  } catch {
    return res.status(401).json({ error: "Invalid token" });
  }
}

// ---------------- Post Question Endpoint (UPDATED) ----------------
app.post("/questions", isAuthenticated, upload.single("qimg"), async (req, res) => {
  try {
    const { qtitle, qdes } = req.body;
    if (!qtitle || !qdes || !req.file)
      return res.status(400).json({ error: "Title, description and image are required" });

    const newQuestion = await Question.create({
      uid: req.user.id,
      qtitle,
      qdes,
      qimg: req.file.filename,
    });

    res.status(201).json({ message: "Question posted successfully", question: newQuestion });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Server error" });
  }
});

// ---------------- Get All Questions ----------------
app.get("/questions", async (req, res) => {
  try {
    const questions = await Question.find()
      .populate("uid", "name email")
      .sort({ createdAt: -1 });
    res.status(200).json({ questions });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Server error" });
  }
});

// ---------------- Get Single Question by ID (NEW) ----------------
app.get("/questions/:id", isAuthenticated, async (req, res) => {
  try {
    const question = await Question.findById(req.params.id)
      .populate("uid", "name email");
    
    if (!question)
      return res.status(404).json({ error: "Question not found" });

    res.status(200).json({ question });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Server error" });
  }
});

// ---------------- Get Current User's Questions (NEW) ----------------
app.get("/questions/my", isAuthenticated, async (req, res) => {
  try {
    const questions = await Question.find({ uid: req.user.id })
      .populate("uid", "name email")
      .sort({ createdAt: -1 });
    
    res.status(200).json({ questions });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Server error" });
  }
});

// ---------------- Server ----------------
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`🚀 Server running on port ${PORT}`));