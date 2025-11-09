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
import ToastNotification from './components/ToastNotification';
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

// Utility function to decode user ID from JWT token
const getUserIdFromToken = () => {
  try {
    const token = localStorage.getItem('token');
    if (!token) return null;
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.id;
  } catch (err) {
    console.error('Error decoding token:', err);
    return null;
  }
};

// Note: objectIdToInt function removed - we now use ObjectId strings directly
// The Java backend has been updated to accept String instead of Integer

function App({ questionId: propQuestionId = null, userId: propUserId = null, onLoadSolution = null, solutionArchitectureId = null, aiMode = false, questionData = null }) {
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
  const [architectureName, setArchitectureName] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isCreatingArchitecture, setIsCreatingArchitecture] = useState(false);
  const [architectureCreated, setArchitectureCreated] = useState(false); // Track if architecture was created;
  const nameJustLoaded = useRef(false); // Track if name was just loaded from backend
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

  const loadLinkTypes = useCallback(async () => {
    try {
      const response = await linkAPI.getTypes();
      setLinkTypes(response.data);
    } catch (error) {
      console.error('Failed to load link types:', error);
    }
  }, []);

  const showNotification = useCallback((message, type = 'info', duration = 4000) => {
    setNotification({ message, type, duration });
  }, []);

  const createNewArchitecture = useCallback(async () => {
    // Prevent duplicate creation
    if (isCreatingArchitecture || architectureCreated || architectureId) {
      return;
    }

    setIsCreatingArchitecture(true);
    try {
      // Use user-provided name or default to 'Untitled Architecture'
      const nameToSend = architectureName && architectureName.trim() ? architectureName.trim() : 'Untitled Architecture';
      console.log('Creating architecture with name:', nameToSend);
      const response = await architectureAPI.create({ name: nameToSend });
      console.log('Architecture created, response:', response.data);
      setArchitectureId(response.data.id);
      setArchitectureName(response.data.name);
      setArchitectureCreated(true); // Mark as created
      nameJustLoaded.current = true; // Mark that name was just set from backend
      showNotification('Architecture created successfully', 'success');
    } catch (error) {
      showNotification('Failed to create architecture', 'error');
      console.error('Failed to create architecture:', error);
    } finally {
      setIsCreatingArchitecture(false);
    }
  }, [architectureName, showNotification, isCreatingArchitecture, architectureCreated, architectureId]);

  const loadArchitecture = useCallback(async (architectureId) => {
    try {
      const response = await architectureAPI.getById(architectureId);
      const architecture = response.data;

      // Clear existing canvas
      setNodes([]);
      setEdges([]);

      // Convert components to ReactFlow nodes
      const loadedNodes = architecture.components.map(comp => ({
        id: `node-${comp.id}`,
        type: 'component',
        position: comp.position || { x: Math.random() * 500, y: Math.random() * 500 }, // fallback for old data
        data: {
          label: comp.name,
          componentType: comp.type,
          componentId: comp.id,
          heuristics: comp.heuristics,
          properties: comp.properties,
          subtype: comp.properties?.subtype
        }
      }));

      // Convert links to ReactFlow edges
      const loadedEdges = architecture.links.map(link => {
        // Extract source and target IDs from various possible structures
        let sourceId, targetId;

        if (link.source) {
          // If source is an object with id property
          sourceId = typeof link.source === 'object' ? link.source.id : link.source;
        } else if (link.sourceId) {
          sourceId = link.sourceId;
        }

        if (link.target) {
          // If target is an object with id property
          targetId = typeof link.target === 'object' ? link.target.id : link.target;
        } else if (link.targetId) {
          targetId = link.targetId;
        }

        if (!sourceId || !targetId) {
          console.error('Link missing source or target:', link);
          return null;
        }

        return {
          id: link.id,
          source: `node-${sourceId}`,
          target: `node-${targetId}`,
          type: 'smoothstep',
          animated: true,
          label: (link.type || link.linkType || '').replace(/_/g, ' '),
          data: {
            linkId: link.id,
            linkType: link.type || link.linkType,
            heuristics: link.heuristics
          }
        };
      }).filter(edge => edge !== null); // Remove any null edges from invalid links

      setNodes(loadedNodes);
      setEdges(loadedEdges);
      setArchitectureId(architecture.id);
      setArchitectureName(architecture.name);
      nameJustLoaded.current = true; // Mark that name was just loaded from backend

      showNotification('Architecture loaded successfully', 'success');
    } catch (error) {
      showNotification('Failed to load architecture', 'error');
      console.error('Failed to load architecture:', error);
    }
  }, [showNotification, setNodes, setEdges]);

  const copyAndLoadArchitecture = useCallback(async (sourceArchitectureId, newName = null) => {
    try {
      const response = await architectureAPI.copy(sourceArchitectureId, newName ? { name: newName } : {});
      const copiedArchitecture = response.data;

      // Load the copied architecture onto canvas
      await loadArchitecture(copiedArchitecture.id);

      showNotification(`Architecture "${copiedArchitecture.name}" loaded for editing`, 'success');
    } catch (error) {
      showNotification('Failed to copy architecture', 'error');
      console.error('Failed to copy architecture:', error);
    }
  }, [loadArchitecture, showNotification]);

  // Initialize: load link types only once on mount
  useEffect(() => {
    loadLinkTypes();
  }, [loadLinkTypes]);

  // Create new architecture only if no solution is being loaded and no architecture exists
  // Use a ref to ensure this only runs once
  const architectureInitialized = useRef(false);
  useEffect(() => {
    if (!solutionArchitectureId && !architectureId && !isCreatingArchitecture && !architectureInitialized.current) {
      architectureInitialized.current = true;
      createNewArchitecture();
    }
  }, [solutionArchitectureId, architectureId, isCreatingArchitecture, createNewArchitecture]);

  // Load solution architecture when solutionArchitectureId changes
  useEffect(() => {
    if (solutionArchitectureId) {
      copyAndLoadArchitecture(solutionArchitectureId);
    }
  }, [solutionArchitectureId, copyAndLoadArchitecture]);

  // Update architecture name when it changes (debounced)
  useEffect(() => {
    if (!architectureId) {
      console.log('Skipping name update - no architecture ID');
      return;
    }

    if (!architectureName || architectureName.trim() === '') {
      console.log('Skipping name update - empty name');
      return;
    }

    const timeoutId = setTimeout(async () => {
      // Check flag at the time of actual update, not at effect setup
      if (nameJustLoaded.current) {
        console.log('Skipping name update - name was just loaded from backend');
        nameJustLoaded.current = false; // Reset the flag
        return;
      }

      try {
        const nameToUpdate = architectureName.trim();
        console.log('=== UPDATING ARCHITECTURE NAME ===');
        console.log('Architecture ID:', architectureId);
        console.log('Current name in state:', architectureName);
        console.log('Name to update:', nameToUpdate);

        const response = await architectureAPI.update(architectureId, { name: nameToUpdate });

        console.log('Update response:', response.data);
        console.log('Response name:', response.data.name);
        console.log('=================================');

        // Show success notification
        showNotification('Architecture name updated', 'success');
      } catch (error) {
        console.error('Failed to update architecture name:', error);
        showNotification('Failed to update architecture name', 'error');
      }
    }, 1000); // Wait 1 second after user stops typing

    return () => clearTimeout(timeoutId);
  }, [architectureName, architectureId, showNotification]);

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

  // createConnection now accepts optional tempEdgeId to replace the optimistic edge
  const createConnection = useCallback(async (params, sourceNode, targetNode, linkType, tempEdgeId = null) => {
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
  }, [architectureId, showNotification, setEdges]);

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
      [nodes, linkTypes, setEdges, createConnection, showNotification]
  );

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
            position: { x: position.x, y: position.y }, // Send position to backend
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
      if (aiMode && questionData) {
        // AI Mode: Call the AI evaluation endpoint
        showNotification('Evaluating with AI...', 'info', 6000); // Longer duration for AI evaluation

        // Fetch the architecture data
        const archResponse = await architectureAPI.getById(architectureId);
        const architecture = archResponse.data;

        // Build architecture object similar to MongoDB structure
        const architectureData = {
          id: architecture.id,
          name: architecture.name,
          components: architecture.components || [],
          links: architecture.links || [],
        };

        // Prepare question text
        const questionText = `${questionData.qtitle}\n\n${questionData.qdes || ''}`;

        // Call AI endpoint
        const aiResponse = await fetch('https://tusharsinghbaghel-synhack.hf.space/evaluate', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            question: questionText,
            architecture: architectureData,
          }),
        });

        if (!aiResponse.ok) {
          throw new Error(`AI evaluation failed: ${aiResponse.statusText}`);
        }

        const aiData = await aiResponse.json();

        // Transform AI response to match EvaluationPanel format
        const transformedEvaluation = {
          overallScore: Object.values(aiData.heuristic_scores || {}).reduce((sum, val) => sum + val, 0) / (Object.keys(aiData.heuristic_scores || {}).length || 1),
          parameterScores: aiData.heuristic_scores || {},
          insights: aiData.suggestion ? [aiData.suggestion] : [],
          componentCount: architecture.components?.length || 0,
          linkCount: architecture.links?.length || 0,
          valid: true,
          isAiMode: true, // Flag to indicate AI mode response
        };

        setEvaluation(transformedEvaluation);
        setShowEvaluation(true);
        showNotification('Architecture evaluated successfully with AI', 'success');
      } else {
        // Regular Mode: Use existing evaluation
        const response = await architectureAPI.evaluate({
          architectureId: architectureId,
        });
        setEvaluation(response.data);
        setShowEvaluation(true);
        showNotification('Architecture evaluated successfully', 'success');
      }
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
    // Prevent multiple submissions
    if (isSubmitting) {
      showNotification('Submission already in progress...', 'info');
      return;
    }

    if (!architectureId) {
      showNotification('No architecture to submit', 'error');
      return;
    }

    // Get userId and questionId from props or token
    const userId = propUserId || getUserIdFromToken();
    const questionId = propQuestionId;

    // If we have both userId and questionId, submit automatically
    if (userId && questionId) {
      setIsSubmitting(true);
      try {
        // First, check if architecture is already submitted
        try {
          const archResponse = await architectureAPI.getById(architectureId);
          if (archResponse.data.submitted) {
            showNotification('This architecture has already been submitted', 'warning');
            setIsSubmitting(false);
            return;
          }
        } catch (checkError) {
          console.warn('Failed to check architecture status:', checkError);
          // Continue with submit even if check fails
        }

        // First, update the architecture name to ensure it's current
        try {
          if (architectureName && architectureName.trim()) {
            const nameToSubmit = architectureName.trim();
            console.log('Updating architecture name before submit to:', nameToSubmit);
            const updateResponse = await architectureAPI.update(architectureId, { name: nameToSubmit });
            console.log('Name updated before submit. Response name:', updateResponse.data.name);
            // Update local state to match what was saved
            setArchitectureName(updateResponse.data.name);
          } else {
            console.warn('Architecture name is empty, using backend default');
          }
        } catch (updateError) {
          console.warn('Failed to update architecture name before submit:', updateError);
          // Continue with submit even if name update fails
        }

        // Send ObjectId strings directly (Java backend now accepts String instead of Integer)
        const response = await architectureAPI.submit(architectureId, {
          userId: userId,
          questionId: questionId,
        });

        showNotification('Architecture submitted successfully!', 'success');

        // Reload the page after a short delay to show the success message
        setTimeout(() => {
          window.location.reload();
        }, 1500);
      } catch (error) {
        showNotification('Failed to submit architecture', 'error');
        console.error('Failed to submit architecture:', error);
        setIsSubmitting(false);
      }
    } else {
      // Fallback: if props not provided, show error
      showNotification(
          userId
              ? 'Question ID is required to submit'
              : questionId
                  ? 'User ID is required to submit'
                  : 'User ID and Question ID are required to submit',
          'error'
      );
    }
  };

  const clearCanvas = () => {
    if (window.confirm('Are you sure you want to clear the canvas?')) {
      setNodes([]);
      setEdges([]);
      // Don't create a new architecture - just clear the canvas
      // The existing architecture will be reused
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
        <ToastNotification
          notification={notification}
          onClose={() => setNotification(null)}
        />

        <div className="app-header">
          <h1>System Design Simulator</h1>
          <div className="header-actions">
            <input
                type="text"
                value={architectureName}
                onChange={(e) => setArchitectureName(e.target.value)}
                onKeyDown={async (e) => {
                  // Save name immediately when user presses Enter
                  if (e.key === 'Enter') {
                    e.preventDefault(); // Prevent form submission if in a form

                    if (!architectureId) {
                      console.error('Cannot save name - no architecture ID');
                      showNotification('No architecture to save name for', 'error');
                      return;
                    }

                    if (!architectureName || !architectureName.trim()) {
                      console.error('Cannot save name - name is empty');
                      showNotification('Please enter a name', 'error');
                      return;
                    }

                    try {
                      const nameToUpdate = architectureName.trim();
                      console.log('Saving architecture name on Enter:', nameToUpdate);
                      console.log('Architecture ID:', architectureId);

                      const response = await architectureAPI.update(architectureId, { name: nameToUpdate });

                      console.log('Name saved successfully. Response:', response.data);
                      showNotification('Architecture name saved', 'success');
                      e.target.blur(); // Remove focus from input
                    } catch (error) {
                      console.error('Failed to save name on Enter:', error);
                      console.error('Error details:', error.response?.data || error.message);
                      const errorMsg = error.response?.data?.error || error.message || 'Unknown error';
                      showNotification(`Failed to save name: ${errorMsg}`, 'error');
                    }
                  }
                }}
                onBlur={async () => {
                  // Save name immediately when user clicks away from input
                  if (architectureId && architectureName && architectureName.trim()) {
                    try {
                      const nameToUpdate = architectureName.trim();
                      console.log('Saving architecture name on blur:', nameToUpdate);
                      const response = await architectureAPI.update(architectureId, { name: nameToUpdate });
                      console.log('Name saved successfully on blur. Response:', response.data);
                    } catch (error) {
                      console.error('Failed to save name on blur:', error);
                      console.error('Error details:', error.response?.data || error.message);
                    }
                  }
                }}
                className="architecture-name-input"
                placeholder="Enter architecture name"
            />
            <button onClick={validateArchitecture} className="btn btn-secondary">
              Validate
            </button>
            <button
                onClick={submitArchitecture}
                className="btn btn-success"
                disabled={isSubmitting}
            >
              {isSubmitting ? 'Submitting...' : 'Submit'}
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
                <MiniMap
                    nodeColor={(node) => {
                      // Use bright purple accent color for nodes in minimap for better visibility
                      return '#667eea';
                    }}
                    nodeStrokeColor={(node) => {
                      return '#ffffff';
                    }}
                    nodeStrokeWidth={2}
                    maskColor="rgba(102, 126, 234, 0.2)"
                    maskStrokeColor="rgba(102, 126, 234, 0.8)"
                    style={{
                      backgroundColor: 'rgba(11, 15, 20, 0.95)',
                    }}
                    pannable={true}
                    zoomable={true}
                />
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
      </div>
  );
}

export default App;
