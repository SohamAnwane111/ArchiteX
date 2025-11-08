import React, { useState, useCallback, useRef, useEffect } from 'react';
import ReactFlow, {
  ReactFlowProvider,
  addEdge,
  useNodesState,
  useEdgesState,
  Controls,
  Background,
  MiniMap,
  Panel,
} from 'reactflow';
import 'reactflow/dist/style.css';
import './App.css';
import ComponentPalette from './components/ComponentPalette';
import ComponentNode from './components/ComponentNode';
import Sidebar from './components/Sidebar';
import EvaluationPanel from './components/EvaluationPanel';
import SubtypeModal from './components/SubtypeModal';
import LinkTypeModal from './components/LinkTypeModal';
import ComponentNameModal from './components/ComponentNameModal';
import { componentAPI, linkAPI, architectureAPI } from './api';

const nodeTypes = {
  component: ComponentNode,
};

const COMPONENTS_WITH_SUBTYPES = [
  'DATABASE',
  'CACHE',
  'API_SERVICE',
  'QUEUE',
  'STORAGE',
  'LOAD_BALANCER',
];

function App() {
  const reactFlowWrapper = useRef(null);
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [reactFlowInstance, setReactFlowInstance] = useState(null);
  const [selectedNode, setSelectedNode] = useState(null);
  const [selectedEdge, setSelectedEdge] = useState(null);

  // NEW: preview state exposed by palette (clicking/pinning a subtype
  const [previewedSubtype, setPreviewedSubtype] = useState(null);
  // previewedSubtype shape: { componentType: 'DATABASE', subtype: { id, label, heuristics, ... } }

  const [architectureId, setArchitectureId] = useState(null);
  const [architectureName, setArchitectureName] = useState('My Architecture');
  const [linkTypes, setLinkTypes] = useState([]);
  const [showEvaluation, setShowEvaluation] = useState(false);
  const [evaluation, setEvaluation] = useState(null);
  const [notification, setNotification] = useState(null);

  const [showSubtypeModal, setShowSubtypeModal] = useState(false);
  const [pendingComponent, setPendingComponent] = useState(null);

  const [showLinkTypeModal, setShowLinkTypeModal] = useState(false);
  const [pendingConnection, setPendingConnection] = useState(null);
  const [availableLinkTypes, setAvailableLinkTypes] = useState([]);

  const [showNameModal, setShowNameModal] = useState(false);
  const [pendingComponentWithSubtype, setPendingComponentWithSubtype] = useState(null);

  const [showSubmitModal, setShowSubmitModal] = useState(false);
  const [submitUserId, setSubmitUserId] = useState('');
  const [submitQuestionId, setSubmitQuestionId] = useState('');

  useEffect(() => {
    loadLinkTypes();
    createNewArchitecture();
  }, []);

  // Handler passed to ComponentPalette so it can notify App about a preview/selection
  const handlePreviewSubtype = useCallback((componentType, subtype) => {
    // When the palette previews a subtype, clear any real canvas selection
    // so the Sidebar will show the previewed heuristics immediately.
    if (!componentType || !subtype) {
      setPreviewedSubtype(null);
      return;
    }
    // clear canvas selection to avoid selectedNode overriding preview
    setSelectedNode(null);
    setSelectedEdge(null);
    setPreviewedSubtype({ componentType, subtype });
  }, [setSelectedNode, setSelectedEdge]);

  // If user selects a real node/edge on canvas, clear palette preview to avoid ambiguity
  useEffect(() => {
    if (selectedNode || selectedEdge) {
      setPreviewedSubtype(null);
    }
  }, [selectedNode, selectedEdge]);

  const loadLinkTypes = async () => {
    try {
      const response = await linkAPI.getTypes();
      setLinkTypes(response.data);
    } catch (error) {
      console.error('Failed to load link types:', error);
    }
  };

  const createNewArchitecture = async () => {
    try {
      const response = await architectureAPI.create({ name: architectureName });
      setArchitectureId(response.data.id);
      setArchitectureName(response.data.name);
      showNotification('Architecture created successfully', 'success');
    } catch (error) {
      showNotification('Failed to create architecture', 'error');
      console.error('Failed to create architecture:', error);
    }
  };

  const showNotification = (message, type = 'info') => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 3000);
  };

  const onConnect = useCallback(
      async (params) => {
        // create optimistic temporary edge so user sees an immediate connection
        const tempId = `edge-temp-${Date.now()}`;
        const optimisticEdge = {
          id: tempId,
          source: params.source,
          target: params.target,
          sourceHandle: params.sourceHandle,
          targetHandle: params.targetHandle,
          type: 'smoothstep',
          animated: true,
          label: 'Connecting...',
          data: { temp: true },
        };

        setEdges((eds) => addEdge(optimisticEdge, eds));

        const sourceNode = nodes.find((n) => n.id === params.source);
        const targetNode = nodes.find((n) => n.id === params.target);

        if (!sourceNode || !targetNode) {
          // invalid - remove temp
          setEdges((eds) => eds.filter((e) => e.id !== tempId));
          return;
        }

        try {
          const response = await linkAPI.suggest({
            sourceId: sourceNode.data.componentId,
            targetId: targetNode.data.componentId,
          });

          const suggestions = response?.data?.validLinkTypes || linkTypes || [];

          if (suggestions.length === 0) {
            showNotification('No valid link types for this connection', 'error');
            setEdges((eds) => eds.filter((e) => e.id !== tempId));
            return;
          }

          if (suggestions.length > 1) {
            // ask user; keep temp edge until user chooses or cancels
            setPendingConnection({
              params,
              sourceNode,
              targetNode,
              tempEdgeId: tempId,
            });
            setAvailableLinkTypes(suggestions);
            setShowLinkTypeModal(true);
            return;
          }

          // single suggestion -> create connection and replace temp
          const linkType = suggestions[0];
          await createConnection(params, sourceNode, targetNode, linkType, tempId);
        } catch (error) {
          showNotification('Failed to get link type suggestions', 'error');
          console.error('Failed to get link type suggestions:', error);
          setEdges((eds) => eds.filter((e) => e.id !== tempId));
        }
      },
      [nodes, edges, architectureId, linkTypes, setEdges]
  );

  // createConnection now accepts optional tempEdgeId to replace the optimistic edge
  const createConnection = async (params, sourceNode, targetNode, linkType, tempEdgeId = null) => {
    try {
      const validationResponse = await linkAPI.validate({
        sourceId: sourceNode.data.componentId,
        targetId: targetNode.data.componentId,
        linkType: linkType,
      });

      if (!validationResponse.data.valid) {
        showNotification(validationResponse.data.message || 'Invalid connection', 'error');
        if (tempEdgeId) setEdges((eds) => eds.filter((e) => e.id !== tempEdgeId));
        return;
      }

      const linkResponse = await linkAPI.create({
        sourceId: sourceNode.data.componentId,
        targetId: targetNode.data.componentId,
        linkType: linkType,
      });

      const finalEdge = {
        ...params,
        id: linkResponse.data.id,
        type: 'smoothstep',
        animated: true,
        label: (linkType || '').replace(/_/g, ' '),
        data: {
          linkId: linkResponse.data.id,
          linkType: linkType,
          heuristics: linkResponse.data.heuristics,
        },
      };

      setEdges((eds) => {
        const withoutTemp = tempEdgeId ? eds.filter((e) => e.id !== tempEdgeId) : eds;
        return addEdge(finalEdge, withoutTemp);
      });

      if (architectureId) {
        await architectureAPI.addLink(architectureId, { linkId: linkResponse.data.id });
      }

      showNotification('Connection created successfully', 'success');
    } catch (error) {
      showNotification(error.response?.data?.error || 'Failed to create connection', 'error');
      console.error('Failed to create connection:', error);
      if (tempEdgeId) setEdges((eds) => eds.filter((e) => e.id !== tempEdgeId));
    }
  };

  const onDragOver = useCallback((event) => {
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
  }, []);

  const createComponentOnCanvas = useCallback(
      async (type, position, subtype, customName) => {
        try {
          const properties = subtype ? { subtype } : {};

          const response = await componentAPI.create({
            type: type,
            name: customName || `${type}-${Date.now()}`,
            properties: properties,
          });

          const newNode = {
            id: `node-${response.data.id}`,
            type: 'component',
            position,
            data: {
              label: response.data.name,
              customName: customName,
              componentType: type,
              componentId: response.data.id,
              heuristics: response.data.heuristics,
              properties: response.data.properties,
              subtype: subtype,
            },
          };

          setNodes((nds) => nds.concat(newNode));

          if (architectureId) {
            await architectureAPI.addComponent(architectureId, { componentId: response.data.id });
          }

          const subtypeLabel = subtype ? ` (${subtype.replace(/_/g, ' ')})` : '';
          showNotification(`Component added successfully${subtypeLabel}`, 'success');
          // return the created node so callers can select it if needed
          return newNode;
        } catch (error) {
          const errorMessage = error.response?.data?.error
              || error.response?.data?.message
              || error.message
              || 'Failed to add component';
          showNotification(errorMessage, 'error');
          console.error('Failed to create component:', error);
          throw error;
        }
      },
      [architectureId, setNodes, showNotification]
  );

  const onDrop = useCallback(
      async (event) => {
        event.preventDefault();

        if (!reactFlowWrapper.current || !reactFlowInstance) {
          return;
        }

        const reactFlowBounds = reactFlowWrapper.current.getBoundingClientRect();

        // support payload that may be JSON (from palette) or plain text
        let payload = event.dataTransfer.getData('application/reactflow') || event.dataTransfer.getData('text/plain') || '';
        let type = payload;
        let subtype = null;

        // try to parse JSON payload { type, subtype }
        try {
          const parsed = JSON.parse(payload);
          if (parsed && typeof parsed === 'object') {
            type = parsed.type || type;
            subtype = parsed.subtype || null;
          }
        } catch (err) {
          // not JSON - payload remains as plain type
        }

        if (!type) return;

        const position = reactFlowInstance.project({
          x: event.clientX - reactFlowBounds.left,
          y: event.clientY - reactFlowBounds.top,
        });

        // store pending component (may already include subtype)
        setPendingComponent({ type, position, subtype });

        if (COMPONENTS_WITH_SUBTYPES.includes(type)) {
          // if subtype already provided by palette selection, skip modal and go to name modal
          if (subtype) {
            setPendingComponentWithSubtype({ type, position, subtype });
            setShowNameModal(true);
          } else {
            setShowSubtypeModal(true);
          }
        } else {
          setShowNameModal(true);
        }
      },
      [reactFlowInstance]
  );

  const onNodeClick = useCallback((event, node) => {
    setSelectedNode(node);
    setSelectedEdge(null);
  }, []);

  const onEdgeClick = useCallback((event, edge) => {
    setSelectedEdge(edge);
    setSelectedNode(null);
  }, []);

  const onDeleteNode = async (nodeId) => {
    const node = nodes.find((n) => n.id === nodeId);
    if (!node) return;

    try {
      await componentAPI.delete(node.data.componentId);
      setNodes((nds) => nds.filter((n) => n.id !== nodeId));
      setSelectedNode(null);
      showNotification('Component deleted', 'success');
    } catch (error) {
      showNotification('Failed to delete component', 'error');
      console.error('Failed to delete component:', error);
    }
  };

  const onDeleteEdge = async (edgeId) => {
    const edge = edges.find((e) => e.id === edgeId);
    if (!edge) return;

    try {
      await linkAPI.delete(edge.data.linkId);
      setEdges((eds) => eds.filter((e) => e.id !== edgeId));
      setSelectedEdge(null);
      showNotification('Connection deleted', 'success');
    } catch (error) {
      showNotification('Failed to delete connection', 'error');
      console.error('Failed to delete connection:', error);
    }
  };

  const evaluateArchitecture = async () => {
    if (!architectureId) {
      showNotification('No architecture to evaluate', 'error');
      return;
    }

    try {
      const response = await architectureAPI.evaluate({
        architectureId: architectureId,
      });
      setEvaluation(response.data);
      setShowEvaluation(true);
      showNotification('Architecture evaluated successfully', 'success');
    } catch (error) {
      showNotification('Failed to evaluate architecture', 'error');
      console.error('Failed to evaluate architecture:', error);
    }
  };

  const validateArchitecture = async () => {
    if (!architectureId) {
      showNotification('No architecture to validate', 'error');
      return;
    }

    try {
      const response = await architectureAPI.validate(architectureId);
      const validation = response.data;

      if (validation.valid) {
        showNotification('Architecture is valid!', 'success');
      } else {
        showNotification(`Architecture has ${validation.violations.length} violations`, 'warning');
      }

      setEvaluation({ ...evaluation, validation });
      setShowEvaluation(true);
    } catch (error) {
      showNotification('Failed to validate architecture', 'error');
      console.error('Failed to validate architecture:', error);
    }
  };

  const submitArchitecture = async () => {
    if (!architectureId) {
      showNotification('No architecture to submit', 'error');
      return;
    }

    // Open modal to get userId and questionId
    setShowSubmitModal(true);
  };

  const handleSubmitConfirm = async () => {
    if (!submitUserId || !submitQuestionId) {
      showNotification('Please enter both User ID and Question ID', 'warning');
      return;
    }

    try {
      const response = await architectureAPI.submit(architectureId, {
        userId: parseInt(submitUserId),
        questionId: parseInt(submitQuestionId),
      });

      showNotification('Architecture submitted successfully!', 'success');
      setShowSubmitModal(false);
      setSubmitUserId('');
      setSubmitQuestionId('');
    } catch (error) {
      showNotification('Failed to submit architecture', 'error');
      console.error('Failed to submit architecture:', error);
    }
  };

  const handleSubmitCancel = () => {
    setShowSubmitModal(false);
    setSubmitUserId('');
    setSubmitQuestionId('');
  };

  const clearCanvas = () => {
    if (window.confirm('Are you sure you want to clear the canvas?')) {
      setNodes([]);
      setEdges([]);
      createNewArchitecture();
    }
  };

  const handleSubtypeSelect = async (subtype) => {
    // If a pendingComponent already exists, we use it.
    // Otherwise ignore.
    if (!pendingComponent) return;

    const { type, position } = pendingComponent;
    setPendingComponentWithSubtype({ type, position, subtype });
    setShowSubtypeModal(false);
    setShowNameModal(true);
  };

  const handleSubtypeCancel = () => {
    setPendingComponent(null);
    setShowSubtypeModal(false);
  };

  const handleNameConfirm = async (customName) => {
    // Priority: pendingComponentWithSubtype (explicit), else pendingComponent (may already include subtype)
    if (pendingComponentWithSubtype) {
      const { type, position, subtype } = pendingComponentWithSubtype;
      await createComponentOnCanvas(type, position, subtype, customName);
      setPendingComponentWithSubtype(null);
    } else if (pendingComponent) {
      const { type, position, subtype } = pendingComponent;
      await createComponentOnCanvas(type, position, subtype || null, customName);
      setPendingComponent(null);
    }
    setShowNameModal(false);
  };

  const handleNameCancel = () => {
    setPendingComponent(null);
    setPendingComponentWithSubtype(null);
    setShowNameModal(false);
  };

  const handleLinkTypeSelect = async (linkType) => {
    if (!pendingConnection) return;

    const { params, sourceNode, targetNode, tempEdgeId } = pendingConnection;
    await createConnection(params, sourceNode, targetNode, linkType, tempEdgeId);

    setPendingConnection(null);
    setShowLinkTypeModal(false);
  };

  const handleLinkTypeCancel = () => {
    // remove any optimistic edge if present
    if (pendingConnection?.tempEdgeId) {
      setEdges((eds) => eds.filter((e) => e.id !== pendingConnection.tempEdgeId));
    }
    setPendingConnection(null);
    setShowLinkTypeModal(false);
  };

  return (
      <div className="app">
        {notification && (
            <div className={`notification notification-${notification.type}`}>
              {notification.message}
            </div>
        )}

        <div className="app-header">
          <h1>🏗️ System Design Simulator</h1>
          <div className="header-actions">
            <input
                type="text"
                value={architectureName}
                onChange={(e) => setArchitectureName(e.target.value)}
                className="architecture-name-input"
                placeholder="Architecture name"
            />
            <button onClick={validateArchitecture} className="btn btn-secondary">
              Validate
            </button>
            <button onClick={submitArchitecture} className="btn btn-success">
              Submit
            </button>
            <button onClick={evaluateArchitecture} className="btn btn-primary">
              Evaluate
            </button>
            <button onClick={clearCanvas} className="btn btn-danger">
              Clear
            </button>
          </div>
        </div>

        <div className="app-content">
          {/* pass preview handler to palette */}
          <ComponentPalette onPreviewSubtype={handlePreviewSubtype} />

          <div className="canvas-container" ref={reactFlowWrapper}>
            <ReactFlowProvider>
              <ReactFlow
                  nodes={nodes}
                  edges={edges}
                  onNodesChange={onNodesChange}
                  onEdgesChange={onEdgesChange}
                  onConnect={onConnect}
                  onInit={setReactFlowInstance}
                  onDrop={onDrop}
                  onDragOver={onDragOver}
                  onNodeClick={onNodeClick}
                  onEdgeClick={onEdgeClick}
                  nodeTypes={nodeTypes}
                  fitView
                  nodesConnectable={true}
                  nodesDraggable={true}
                  connectionMode="loose"
                  connectionLineType="smoothstep"
                  defaultEdgeOptions={{ type: 'smoothstep', animated: true }}
              >
                <Background />
                <Controls />
                <MiniMap />
                <Panel position="top-left" className="canvas-info">
                  <div className="info-item">
                    <strong>Components:</strong> {nodes.length}
                  </div>
                  <div className="info-item">
                    <strong>Connections:</strong> {edges.length}
                  </div>
                </Panel>
              </ReactFlow>
            </ReactFlowProvider>
          </div>

          <Sidebar
              // pass the real selectedNode state and preview separately; Sidebar will render preview if no real selection
              selectedNode={selectedNode}
              selectedEdge={selectedEdge}
              onDeleteNode={onDeleteNode}
              onDeleteEdge={onDeleteEdge}
              previewedSubtype={previewedSubtype} // pass preview data so Sidebar can show heuristics
          />
        </div>

        {showEvaluation && (
            <EvaluationPanel
                evaluation={evaluation}
                onClose={() => setShowEvaluation(false)}
            />
        )}

        {showSubtypeModal && pendingComponent && (
            <SubtypeModal
                componentType={pendingComponent.type}
                onSelect={handleSubtypeSelect}
                onCancel={handleSubtypeCancel}
            />
        )}

        {showLinkTypeModal && pendingConnection && (
            <LinkTypeModal
                linkTypes={availableLinkTypes}
                sourceNode={pendingConnection.sourceNode}
                targetNode={pendingConnection.targetNode}
                onSelect={handleLinkTypeSelect}
                onCancel={handleLinkTypeCancel}
            />
        )}

        {showNameModal && (
            <ComponentNameModal
                componentType={pendingComponentWithSubtype?.type || pendingComponent?.type}
                subtype={pendingComponentWithSubtype?.subtype || pendingComponent?.subtype}
                onConfirm={handleNameConfirm}
                onCancel={handleNameCancel}
            />
        )}

        {showSubmitModal && (
            <div className="modal-overlay">
              <div className="modal-content">
                <div className="modal-header">
                  <h2>Submit Architecture</h2>
                  <button className="close-btn" onClick={handleSubmitCancel}>×</button>
                </div>
                <div className="modal-body">
                  <div className="form-group">
                    <label htmlFor="userId">User ID:</label>
                    <input
                        type="number"
                        id="userId"
                        value={submitUserId}
                        onChange={(e) => setSubmitUserId(e.target.value)}
                        placeholder="Enter your User ID"
                        className="form-input"
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="questionId">Question ID:</label>
                    <input
                        type="number"
                        id="questionId"
                        value={submitQuestionId}
                        onChange={(e) => setSubmitQuestionId(e.target.value)}
                        placeholder="Enter Question ID"
                        className="form-input"
                    />
                  </div>
                </div>
                <div className="modal-footer">
                  <button onClick={handleSubmitCancel} className="btn btn-secondary">
                    Cancel
                  </button>
                  <button onClick={handleSubmitConfirm} className="btn btn-success">
                    Submit
                  </button>
                </div>
              </div>
            </div>
        )}
      </div>
  );
}

export default App;
