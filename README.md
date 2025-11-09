# ArchiteX - System Design Simulator

<div align="center">

![ArchiteX Logo](https://img.shields.io/badge/ArchiteX-System_Design_Simulator-667eea?style=for-the-badge)

**An intelligent platform for designing, evaluating, and learning system architecture patterns**

[![React](https://img.shields.io/badge/React-18.2.0-61DAFB?style=flat&logo=react)](https://reactjs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.5-6DB33F?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![Node.js](https://img.shields.io/badge/Node.js-Express-339933?style=flat&logo=node.js)](https://nodejs.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-47A248?style=flat&logo=mongodb)](https://www.mongodb.com/)
[![Python](https://img.shields.io/badge/Python-LLM_AI-3776AB?style=flat&logo=python)](https://www.python.org/)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Key Features](#-key-features)
- [Technology Stack](#-technology-stack)
- [System Components](#-system-components)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Heuristics Engine](#-heuristics-engine)
- [AI Evaluation](#-ai-evaluation)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Overview

**ArchiteX** is a comprehensive system design platform that combines the power of visual architecture modeling with intelligent evaluation mechanisms. It enables users to:

- 🎨 **Design** system architectures using an intuitive drag-and-drop interface
- 📊 **Evaluate** designs using 10+ industry-standard heuristics
- 🤖 **AI-Powered Analysis** leveraging LLM for context-aware suggestions
- 🏆 **Compare Solutions** through a community-driven Q&A platform
- 📚 **Learn** best practices from real-world architecture patterns

Perfect for students learning system design, engineers preparing for interviews, or architects prototyping solutions.

---

## 🏗️ Architecture

ArchiteX follows a microservices-inspired architecture with three main layers:

```
┌─────────────────────────────────────────────────────────────┐
│                     React Frontend (Port 3000)               │
│  • ReactFlow canvas  • Component palette  • Evaluation UI    │
└─────────────────┬───────────────────────────┬────────────────┘
                  │                           │
        ┌─────────▼─────────┐       ┌────────▼──────────┐
        │  Node.js Backend   │       │  Spring Boot      │
        │   (Port 5000)      │       │   Engine (8080)   │
        │  • Authentication  │       │  • Rule Engine    │
        │  • Q&A Platform    │       │  • Heuristics     │
        │  • User Management │       │  • Architecture   │
        └──────────┬─────────┘       └────────┬──────────┘
                   │                          │
                   └──────────┬───────────────┘
                              │
                    ┌─────────▼──────────┐
                    │   MongoDB Atlas    │
                    │  • Users           │
                    │  • Questions       │
                    │  • Architectures   │
                    │  • Components      │
                    └────────────────────┘

        ┌──────────────────────────────────────┐
        │  Python LLM Server (HuggingFace)     │
        │  https://tusharsinghbaghel-synhack   │
        │  .hf.space/evaluate                  │
        │  • AI-powered architecture analysis  │
        └──────────────────────────────────────┘
```

---

## ✨ Key Features

### 🎨 Visual Architecture Design
- **Drag-and-Drop Canvas**: Build system architectures using ReactFlow
- **Rich Component Library**: 15+ component types (Databases, Caches, Load Balancers, etc.)
- **Subtype Support**: Specialized variants (SQL/NoSQL, Redis/Memcached, etc.)
- **Smart Connections**: Auto-suggested link types with validation
- **Custom Naming**: Personalize components with meaningful names
- **Real-time Preview**: Instant visual feedback on canvas

### 📊 Intelligent Evaluation System
- **10 Heuristic Parameters**:
  - ⚡ Latency
  - 💰 Cost
  - 📈 Scalability
  - 🔄 Consistency
  - ✅ Availability
  - 💾 Durability
  - 🔧 Maintainability
  - 🌱 Energy Efficiency
  - 🚀 Throughput
  - 🔒 Security

- **Rule Engine**: Validates architectural patterns against best practices
- **Component-Level Scores**: Individual heuristic ratings per component
- **Link Analysis**: Connection validity and pattern recognition
- **Overall Architecture Score**: Weighted evaluation across all parameters

### 🤖 AI-Powered Analysis
- **Context-Aware Evaluation**: LLM analyzes architecture against question requirements
- **Intelligent Suggestions**: Get AI-generated improvement recommendations
- **Natural Language Insights**: Human-readable explanations of design decisions
- **Dual Mode**: Toggle between heuristic-based and AI-powered evaluation

### 👥 Community Q&A Platform
- **Post Questions**: Share system design challenges with images
- **Submit Solutions**: Create and submit architecture diagrams
- **View Solutions**: Explore community submissions for any question
- **Compare Designs**: Analyze different approaches to the same problem
- **User Profiles**: Track your questions and solutions
- **Authentication**: Secure JWT-based auth with Google OAuth support

### 🛠️ Advanced Capabilities
- **Architecture Versioning**: Copy and iterate on existing designs
- **Export/Import**: Save and load architecture configurations
- **Minimap Navigation**: Easy navigation for complex diagrams
- **Validation System**: Real-time connection and component validation
- **Toast Notifications**: User-friendly feedback system
- **Responsive Design**: Works seamlessly across devices

---

## 🔧 Technology Stack

### Frontend
```json
{
  "framework": "React 18.2.0",
  "canvas": "ReactFlow 11.10.0",
  "routing": "React Router DOM 7.9.5",
  "http": "Axios 1.6.0",
  "styling": "Custom CSS with modern UI/UX",
  "icons": "React Icons + Lucide React"
}
```

### Backend - Node.js (User Management)
```json
{
  "runtime": "Node.js with Express 5.1.0",
  "database": "MongoDB 8.19.3 (Mongoose ODM)",
  "authentication": "JWT + Passport.js",
  "oauth": "Google OAuth 2.0",
  "file_upload": "Multer 2.0.2",
  "security": "bcryptjs for password hashing",
  "session": "Express Session with MongoStore"
}
```

### Backend - Spring Boot (System Design Engine)
```json
{
  "framework": "Spring Boot 3.5.5",
  "java_version": "Java 21",
  "database": "Spring Data MongoDB",
  "architecture": "REST API with CORS",
  "features": [
    "Rule Engine for validation",
    "Heuristics calculation engine",
    "Architecture lifecycle management",
    "Component and Link management"
  ]
}
```

### AI/ML Layer
```json
{
  "platform": "HuggingFace Spaces",
  "language": "Python",
  "framework": "LLM-based evaluation",
  "endpoint": "POST /evaluate",
  "capabilities": [
    "Architecture analysis",
    "Heuristic scoring",
    "Intelligent suggestions"
  ]
}
```

### Database
```json
{
  "type": "MongoDB Atlas",
  "collections": [
    "users - User accounts and profiles",
    "questions - Q&A platform data",
    "architectures - System designs",
    "components - Individual components",
    "links - Component connections"
  ]
}
```

---

## 🧩 System Components

### 1️⃣ React Frontend (`/frontend`)

**Purpose**: Interactive user interface for system design

**Key Components**:
- `App.js` - Main canvas orchestrator with state management
- `ComponentPalette.js` - Draggable component library with subtype preview
- `ComponentNode.js` - Custom ReactFlow node renderer
- `Sidebar.js` - Property inspector and heuristics viewer
- `EvaluationPanel.js` - Results display with score visualization
- `QuestionsList.js` & `QuestionsDetail.js` - Q&A interface
- `Auth.js` - Login/Signup with Google OAuth
- `ToastNotification.js` - User feedback system

**Features**:
- Real-time canvas updates with optimistic UI
- Subtype selection modals for specialized components
- Link type suggestion and validation
- Architecture naming and management
- Dark mode theme support

---

### 2️⃣ Node.js Backend (`/backend`)

**Purpose**: User authentication and Q&A platform

**Endpoints**:

```javascript
// Authentication
POST   /signup              - Create new user account
POST   /signin              - Login with JWT token

// Questions (Q&A Platform)
POST   /questions           - Post new question (auth required, with image)
GET    /questions           - Get all questions
GET    /questions/my        - Get current user's questions (auth required)
GET    /questions/:id       - Get specific question details
DELETE /questions/:id       - Delete question (auth required)

// Solutions
GET    /questions/:id/solutions - Get all solutions for a question
POST   /solutions           - Submit solution (called by Spring Boot)

// User Management
GET    /users/:id           - Get user profile
PATCH  /users/:id           - Update user profile (auth required)
```

**Database Models**:
```javascript
User {
  name: String,
  email: String (unique),
  password: String (hashed),
  createdAt: Date
}

Question {
  uid: ObjectId (ref: User),
  qtitle: String,
  qdes: String,
  qimg: String (filename),
  solutions: [ObjectId] (ref: Architecture),
  createdAt: Date
}
```

**Security**:
- JWT tokens with 7-day expiration
- bcrypt password hashing (10 rounds)
- Protected routes with authentication middleware
- CORS configured for cross-origin requests

---

### 3️⃣ Spring Boot Engine (`/src`)

**Purpose**: Core system design evaluation and rule engine

**Controllers**:

1. **ArchitectureController** (`/api/architecture`)
```java
POST   /                      - Create new architecture
GET    /{id}                  - Get architecture by ID
PUT    /{id}                  - Update architecture name/metadata
DELETE /{id}                  - Delete architecture
POST   /{id}/components       - Add component to architecture
POST   /{id}/links            - Add link to architecture
POST   /evaluate              - Evaluate architecture heuristics
POST   /validate              - Validate architecture rules
POST   /submit                - Submit solution to question
POST   /copy/{id}             - Copy existing architecture
GET    /health/mongodb        - MongoDB health check
```

2. **ComponentController** (`/api/components`)
```java
POST   /                      - Create component with heuristics
GET    /{id}                  - Get component details
DELETE /{id}                  - Delete component
GET    /types                 - Get available component types
POST   /batch                 - Create multiple components
```

3. **LinkController** (`/api/links`)
```java
POST   /                      - Create link between components
DELETE /{id}                  - Delete link
POST   /validate              - Validate connection
POST   /suggest               - Get suggested link types
GET    /types                 - Get all link types
```

**Domain Models**:
- `Architecture` - Container for entire system design
- `Component` - Individual system component (15+ types)
- `Link` - Connection between components (10+ types)
- Component Types: `DATABASE`, `CACHE`, `API_SERVICE`, `LOAD_BALANCER`, `QUEUE`, `STORAGE`, `CLIENT`, `CDN`, `GATEWAY`, `MONITORING`, `AUTH_SERVICE`, `NOTIFICATION_SERVICE`, `SEARCH_ENGINE`, `ANALYTICS`, `BATCH_PROCESSOR`

**Services**:
- `ArchitectureService` - CRUD operations and lifecycle management
- `RuleEngineService` - Validation logic and pattern checking
- `HeuristicsCalculator` - Score computation engine

---

### 4️⃣ Python LLM Server (External)

**Purpose**: AI-powered architecture evaluation

**Endpoint**: `https://tusharsinghbaghel-synhack.hf.space/evaluate`

**Request Format**:
```json
{
  "question": "Question title and description",
  "architecture": {
    "id": "arch_123",
    "name": "My Design",
    "components": [...],
    "links": [...]
  }
}
```

**Response Format**:
```json
{
  "heuristic_scores": {
    "LATENCY": 8.5,
    "COST": 7.0,
    "SCALABILITY": 9.0,
    ...
  },
  "suggestion": "Your architecture demonstrates good scalability with the use of..."
}
```

**Capabilities**:
- Context-aware analysis based on question requirements
- Natural language explanation of design choices
- Intelligent scoring across all heuristic parameters
- Actionable improvement suggestions

---

## 🚀 Getting Started

### Prerequisites
```bash
- Node.js 16+ and npm
- Java 21+ and Maven
- MongoDB Atlas account (or local MongoDB)
- Git
```

### Installation

#### 1. Clone Repository
```bash
git clone https://github.com/yourusername/ArchiteX.git
cd ArchiteX
```

#### 2. Setup Environment Variables

**Backend (Node.js)** - Create `/backend/.env`:
```env
MONGO_URI=mongodb+srv://username:password@cluster.mongodb.net/architex
JWT_SECRET=your_super_secret_jwt_key_change_this
PORT=5000
GOOGLE_CLIENT_ID=your_google_oauth_client_id
GOOGLE_CLIENT_SECRET=your_google_oauth_secret
FRONTEND_URL=http://localhost:3000
```

**Spring Boot** - Create `/src/main/resources/application.properties`:
```properties
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/architex
spring.data.mongodb.database=architex
server.port=8080
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

#### 3. Install Dependencies

**Frontend**:
```bash
cd frontend
npm install
```

**Backend (Node.js)**:
```bash
cd ../backend
npm install
```

**Spring Boot**:
```bash
cd ..
./mvnw clean install
```

#### 4. Start Services

**Terminal 1 - Node.js Backend**:
```bash
cd backend
node server.js
# Server running on http://localhost:5000
```

**Terminal 2 - Spring Boot Engine**:
```bash
./mvnw spring-boot:run
# Server running on http://localhost:8080
```

**Terminal 3 - React Frontend**:
```bash
cd frontend
npm start
# App running on http://localhost:3000
```

### Quick Test
1. Navigate to `http://localhost:3000`
2. Sign up for an account
3. Start designing your first architecture!
4. Drag components from the palette
5. Connect them with smart links
6. Click "Evaluate" to see heuristic scores

---

## 📚 API Documentation

### Component Types

| Type | Description | Subtypes Available |
|------|-------------|-------------------|
| `DATABASE` | Data persistence layer | SQL, NoSQL, In-Memory, Time-Series, Graph |
| `CACHE` | Fast data retrieval | Redis, Memcached, In-Memory |
| `API_SERVICE` | Business logic layer | REST, GraphQL, gRPC |
| `LOAD_BALANCER` | Traffic distribution | Layer 4, Layer 7, DNS-based |
| `QUEUE` | Message broker | Kafka, RabbitMQ, SQS, PubSub |
| `STORAGE` | Object storage | S3, Blob, File System |
| `CLIENT` | User interface | Web, Mobile, Desktop |
| `CDN` | Content delivery | - |
| `GATEWAY` | API gateway | - |
| `MONITORING` | Observability | - |
| `AUTH_SERVICE` | Authentication | - |
| `NOTIFICATION_SERVICE` | Alerts/messaging | - |
| `SEARCH_ENGINE` | Full-text search | - |
| `ANALYTICS` | Data analysis | - |
| `BATCH_PROCESSOR` | Background jobs | - |

### Link Types

| Link Type | Description | Valid Between |
|-----------|-------------|---------------|
| `SYNCHRONOUS` | Direct request-response | Client ↔ API, API ↔ DB |
| `ASYNCHRONOUS` | Event-driven | Service → Queue |
| `DATA_FLOW` | Data pipeline | DB → Analytics |
| `CACHE_READ` | Cache lookup | API → Cache |
| `CACHE_WRITE` | Cache update | API → Cache |
| `LOAD_BALANCED` | Distributed traffic | LB → Service |
| `REPLICATION` | Data sync | DB → DB |
| `CDN_ORIGIN` | Content delivery | CDN → Storage |
| `AUTHENTICATION` | Auth flow | API → Auth Service |
| `NOTIFICATION` | Alert/message | Service → Notification |

---

## 🎯 Heuristics Engine

### How It Works

The heuristics engine evaluates architectures across 10 dimensions:

1. **Component-Level Heuristics**: Each component type has predefined scores for each parameter
2. **Subtype Variations**: Subtypes modify base scores (e.g., NoSQL has higher scalability than SQL)
3. **Link Impact**: Connections affect certain parameters (e.g., async links improve throughput)
4. **Aggregation**: Architecture score = weighted average of all component/link scores
5. **Validation Rules**: Patterns are checked against best practices

### Example Heuristic Scores (SQL Database)
```json
{
  "LATENCY": 6.0,           // Moderate read/write speed
  "COST": 7.0,              // Mid-range pricing
  "SCALABILITY": 7.0,       // Vertical scaling mainly
  "CONSISTENCY": 9.5,       // ACID guarantees
  "AVAILABILITY": 8.0,      // High uptime
  "DURABILITY": 9.5,        // Strong persistence
  "MAINTAINABILITY": 6.0,   // Schema management complexity
  "ENERGY_EFFICIENCY": 6.0, // Standard power usage
  "THROUGHPUT": 7.0,        // Good transaction rate
  "SECURITY": 8.5           // Mature security features
}
```

### Customization

Edit `/src/main/resources/heuristics-config.json` to modify scores for any component type or subtype.

---

## 🤖 AI Evaluation

### When to Use AI Mode

- **Requirements-Based Design**: When you have specific functional requirements
- **Learning**: To understand why certain patterns work better
- **Optimization**: Get suggestions for improving existing designs
- **Comparison**: Validate your heuristic-based evaluation with AI insights

### How It Works

1. User enables AI mode in the evaluation settings
2. Frontend sends architecture + question context to LLM server
3. LLM analyzes design against requirements
4. Returns heuristic scores + natural language explanation
5. Results displayed in evaluation panel with insights

### Example AI Response
```
"Your architecture demonstrates good scalability with the use of 
horizontal scaling via load balancers and NoSQL databases. However, 
consider adding a caching layer between the API and database to 
reduce latency for frequently accessed data. The async queue 
implementation is excellent for decoupling services, but ensure you 
have proper monitoring for message backlogs."
```

---

## 🧪 Testing

### Backend Tests
```bash
# Node.js
cd backend
npm test

# Spring Boot
./mvnw test
```

### Integration Test
Access the MongoDB test at:
```bash
GET http://localhost:8080/api/architecture/health/mongodb
```

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- **Frontend**: ESLint + Prettier
- **Backend (Node)**: Airbnb style guide
- **Backend (Java)**: Google Java Style Guide

---

## 📄 License

This project is licensed under the ISC License.

---

## 👥 Authors

- **Your Team Name** - System Design Platform
- Contact: [your-email@example.com](mailto:your-email@example.com)

---

## 🙏 Acknowledgments

- ReactFlow for the amazing canvas library
- Spring Boot community for robust framework
- MongoDB for flexible data modeling
- HuggingFace for AI capabilities
- All contributors and users of ArchiteX

---

<div align="center">

**Built with ❤️ for the system design community**

[Report Bug](https://github.com/yourusername/ArchiteX/issues) · [Request Feature](https://github.com/yourusername/ArchiteX/issues)

</div>

