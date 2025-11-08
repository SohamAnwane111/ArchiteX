# System Design Simulator - Frontend

A visual, drag-and-drop interface for designing and evaluating distributed system architectures.

## Features

### 🎨 Visual Design Canvas
- **Drag & Drop Components**: Add databases, caches, APIs, queues, load balancers, and more
- **Visual Connections**: Connect components with validated links
- **Real-time Validation**: Automatic connection rule validation
- **Interactive Canvas**: Pan, zoom, and organize your architecture

### 🔗 Smart Connection System
- **Rule-Based Validation**: Backend validates all connections based on system design rules
- **Link Types**: API calls, streams, replication, ETL pipelines, batch transfers, event flows, etc.
- **Connection Suggestions**: System suggests valid link types between components
- **Heuristics Display**: View performance metrics for each connection

### 📊 Architecture Evaluation
- **Overall Scoring**: Get a comprehensive score for your architecture
- **Component Analysis**: Individual heuristic scores for each component
- **Link Analysis**: Performance metrics for each connection
- **Validation Results**: Check for rule violations and best practices
- **Recommendations**: Get suggestions to improve your design

### 📱 Interactive Components
- **10 Component Types**: Database, Cache, API Service, Queue, Storage, Load Balancer, Stream Processor, Batch Processor, External Service, Client
- **Heuristic Visualization**: See latency, cost, scalability, consistency, availability, durability, maintainability, energy efficiency, throughput, and security scores
- **Properties Panel**: View and manage component properties

## Getting Started

### Prerequisites
- Node.js 16+ and npm
- Java 17+ (for backend)
- Maven (for backend)

### Installation

1. **Install Frontend Dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Start Backend Server** (in a separate terminal)
   ```bash
   # From project root
   mvn spring-boot:run
   ```
   Backend will run on http://localhost:8080

3. **Start Frontend Development Server**
   ```bash
   cd frontend
   npm start
   ```
   Frontend will run on http://localhost:3000

## Usage Guide

### Creating an Architecture

1. **Add Components**
   - Drag component types from the left palette onto the canvas
   - Each component is automatically created in the backend with default heuristics

2. **Create Connections**
   - Click and drag from the bottom handle of one component to the top handle of another
   - The system will validate the connection and suggest appropriate link types
   - Invalid connections will be rejected with an explanation

3. **View Details**
   - Click on any component or connection to view details in the right sidebar
   - See heuristic scores, properties, and IDs

4. **Evaluate Architecture**
   - Click "Validate" to check for rule violations
   - Click "Evaluate" to get a comprehensive score and analysis
   - Review scores for latency, cost, scalability, and more

5. **Manage Architecture**
   - Delete components or connections using the sidebar
   - Clear the canvas to start fresh
   - Name your architecture in the header

## Architecture

### Frontend Stack
- **React 18**: UI framework
- **ReactFlow**: Canvas and graph visualization
- **Axios**: API communication
- **Lucide React**: Icons

### Key Components

#### `App.js`
Main application component that manages:
- Canvas state (nodes and edges)
- Backend communication
- Architecture lifecycle
- User interactions

#### `ComponentPalette.js`
Left sidebar displaying all available component types for drag-and-drop.

#### `ComponentNode.js`
Custom ReactFlow node representing system components with icons and styling.

#### `Sidebar.js`
Right panel showing details of selected components or connections, including heuristic scores.

#### `EvaluationPanel.js`
Modal overlay displaying comprehensive architecture evaluation results.

#### `api.js`
API client with organized endpoints for:
- Components (CRUD operations)
- Links (validation, creation, heuristics)
- Architecture (evaluation, validation, comparison)

## API Integration

The frontend integrates with all backend capabilities:

### Component APIs
- Create, read, update, delete components
- Get component types
- Automatic heuristic initialization

### Link APIs
- Create connections with validation
- Get connection suggestions
- View and update link heuristics
- Get default heuristics by link type

### Architecture APIs
- Create and manage architectures
- Evaluate architectures with detailed scores
- Validate against connection rules
- Compare multiple architectures
- Get visualization data

## Customization

### Adding New Component Types
Edit `ComponentPalette.js` to add new component types:
```javascript
const COMPONENT_TYPES = [
  { type: 'NEW_TYPE', icon: '🆕', label: 'New Type', color: '#hexcolor' },
  // ...
];
```

### Styling
- `App.css`: Main application styles
- `ComponentNode.css`: Node appearance
- `ComponentPalette.css`: Palette styling
- `Sidebar.css`: Details panel
- `EvaluationPanel.css`: Evaluation modal

## Features in Detail

### Heuristic Visualization
Each component and link displays heuristic scores (0-10) for:
- **Latency**: Response time
- **Cost**: Resource expense
- **Scalability**: Growth capacity
- **Consistency**: Data accuracy
- **Availability**: Uptime
- **Durability**: Data persistence
- **Maintainability**: Ease of updates
- **Energy Efficiency**: Power usage
- **Throughput**: Data processing rate
- **Security**: Protection level

### Validation System
- All connections are validated against backend rules
- Invalid connections are rejected with explanations
- Architecture-wide validation checks for best practices
- Violation reporting with specific issues

### Evaluation Metrics
- Overall architecture score (0-10)
- Component-level scores
- Link-level scores
- Architecture metrics (component count, link count, averages)
- Recommendations for improvement

## Troubleshooting

### Connection Failed
- Ensure backend is running on port 8080
- Check browser console for CORS errors
- Verify API endpoints match backend routes

### Drag & Drop Not Working
- Check that ReactFlow is properly initialized
- Ensure `onDragOver` prevents default behavior
- Verify component types match backend enums

### Evaluation Not Showing
- Confirm architecture ID is set
- Check that components and links are added to architecture
- Review backend logs for evaluation errors

## Future Enhancements

Potential features for future development:
- [ ] User authentication and saved architectures
- [ ] Architecture templates and examples
- [ ] Export/import architecture JSON
- [ ] Collaborative editing
- [ ] Version history
- [ ] Advanced filtering and search
- [ ] Custom heuristic weights
- [ ] Architecture comparison view
- [ ] Performance simulation
- [ ] Cost estimation tools

## Development

### Project Structure
```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── components/
│   │   ├── ComponentNode.js/css
│   │   ├── ComponentPalette.js/css
│   │   ├── Sidebar.js/css
│   │   └── EvaluationPanel.js/css
│   ├── App.js
│   ├── App.css
│   ├── api.js
│   ├── index.js
│   └── index.css
└── package.json
```

### Building for Production
```bash
npm run build
```

This creates an optimized production build in the `build/` directory.

## License

MIT License - feel free to use and modify for your needs.

