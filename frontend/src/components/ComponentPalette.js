import React, { useState, useEffect, useRef } from 'react';
import './ComponentPalette.css';
import { componentAPI } from '../api';
import {
  FaDatabase,
  FaMemory,
  FaNetworkWired,
  FaStream,
  FaHdd,
  FaBalanceScale,
  FaBolt,
  FaLayerGroup,
  FaGlobe,
  FaLaptop,
  FaCheck,
} from 'react-icons/fa';

const COMPONENT_TYPES = [
  { type: 'DATABASE', icon: <FaDatabase />, label: 'Database', color: '#10b981' },
  { type: 'CACHE', icon: <FaMemory />, label: 'Cache', color: '#f59e0b' },
  { type: 'API_SERVICE', icon: <FaNetworkWired />, label: 'API Service', color: '#3b82f6' },
  { type: 'QUEUE', icon: <FaStream />, label: 'Queue', color: '#8b5cf6' },
  { type: 'STORAGE', icon: <FaHdd />, label: 'Storage', color: '#ec4899' },
  { type: 'LOAD_BALANCER', icon: <FaBalanceScale />, label: 'Load Balancer', color: '#06b6d4' },
  { type: 'STREAM_PROCESSOR', icon: <FaBolt />, label: 'Stream Processor', color: '#6366f1' },
  { type: 'BATCH_PROCESSOR', icon: <FaLayerGroup />, label: 'Batch Processor', color: '#84cc16' },
  { type: 'EXTERNAL_SERVICE', icon: <FaGlobe />, label: 'External Service', color: '#64748b' },
  { type: 'CLIENT', icon: <FaLaptop />, label: 'Client', color: '#0ea5e9' },
];

const ComponentPalette = ({ onPreviewSubtype }) => {
  const [subtypesByType, setSubtypesByType] = useState({});
  const [loadingByType, setLoadingByType] = useState({});
  const [selectedByType, setSelectedByType] = useState({});
  const [openDropdown, setOpenDropdown] = useState(null);
  const [pinnedType, setPinnedType] = useState(null);

  const panelRef = useRef(null);
  const dragImageCache = useRef({}); // { _holder, [type]: { canvas, dataUrl, node, previewNode } }
  const dragPreviewRef = useRef(null);
  const dragMoveHandlerRef = useRef(null);

  // tiny transparent PNG to hide native ghost when using our custom preview
  const TRANSPARENT_PNG = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8Xw8AAn8B9l2k7wAAAABJRU5ErkJggg==';

  // Build and cache per-type drag-image DOM nodes on mount — keeps them present in DOM for setDragImage
  useEffect(() => {
    if (typeof document === 'undefined') return;
    if (!dragImageCache.current._holder) {
      const holder = document.createElement('div');
      holder.style.position = 'fixed';
      holder.style.left = '-9999px';
      holder.style.top = '-9999px';
      holder.style.width = '0';
      holder.style.height = '0';
      holder.style.overflow = 'hidden';
      holder.setAttribute('aria-hidden', 'true');
      document.body.appendChild(holder);
      dragImageCache.current._holder = holder;
    }
    const holder = dragImageCache.current._holder;

    COMPONENT_TYPES.forEach((comp) => {
      const type = comp.type;
      if (dragImageCache.current[type] && dragImageCache.current[type].node) return;
      try {
        const size = 64;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');

        // draw rounded rect + border + plus (same visuals used at runtime)
        ctx.clearRect(0, 0, size, size);
        ctx.save();
        ctx.shadowColor = 'rgba(0,0,0,0.7)';
        ctx.shadowBlur = 10;
        ctx.shadowOffsetX = 0;
        ctx.shadowOffsetY = 2;
        const radius = 12;
        const pad = 6;
        const x = pad;
        const y = pad;
        const w = size - pad * 2;
        const h = size - pad * 2;
        ctx.fillStyle = comp.color || '#999';
        ctx.beginPath();
        ctx.moveTo(x + radius, y);
        ctx.arcTo(x + w, y, x + w, y + h, radius);
        ctx.arcTo(x + w, y + h, x, y + h, radius);
        ctx.arcTo(x, y + h, x, y, radius);
        ctx.arcTo(x, y, x + w, y, radius);
        ctx.closePath();
        ctx.fill();
        ctx.restore();

        ctx.strokeStyle = 'rgba(255,255,255,0.95)';
        ctx.lineWidth = 3;
        ctx.beginPath();
        ctx.moveTo(x + radius, y);
        ctx.arcTo(x + w, y, x + w, y + h, radius);
        ctx.arcTo(x + w, y + h, x, y + h, radius);
        ctx.arcTo(x, y + h, x, y, radius);
        ctx.arcTo(x, y, x + w, y, radius);
        ctx.closePath();
        ctx.stroke();

        ctx.save();
        ctx.lineCap = 'round';
        ctx.lineWidth = 6;
        ctx.shadowColor = 'rgba(255,255,255,0.12)';
        ctx.shadowBlur = 8;
        ctx.strokeStyle = 'rgba(255,255,255,0.98)';
        const cx = size / 2;
        const cy = size / 2;
        const len = size * 0.28;
        ctx.beginPath();
        ctx.moveTo(cx - len, cy);
        ctx.lineTo(cx + len, cy);
        ctx.moveTo(cx, cy - len);
        ctx.lineTo(cx, cy + len);
        ctx.stroke();
        ctx.restore();

        // optional small label char
        const label = (comp.label || comp.type || '').toString();
        if (label) {
          ctx.fillStyle = 'rgba(0,0,0,0.6)';
          ctx.font = '700 12px Inter, Roboto, Arial, sans-serif';
          ctx.textAlign = 'center';
          ctx.textBaseline = 'middle';
          ctx.fillText(label.charAt(0), size * 0.8, size * 0.8);
        }

        // prefer an <img> in DOM (more compatible) but fall back to canvas
        let dataUrl;
        try { dataUrl = canvas.toDataURL('image/png'); } catch (e) { dataUrl = null; }
        if (dataUrl) {
          const img = new Image();
          img.src = dataUrl;
          img.width = size;
          img.height = size;
          img.setAttribute('width', String(size));
          img.setAttribute('height', String(size));
          img.style.width = size + 'px';
          img.style.height = size + 'px';
          holder.appendChild(img);
          dragImageCache.current[type] = { canvas, dataUrl, node: img };
        } else {
          canvas.style.width = size + 'px';
          canvas.style.height = size + 'px';
          holder.appendChild(canvas);
          dragImageCache.current[type] = { canvas, dataUrl: null, node: canvas };
        }
      } catch (e) {
        // ignore
      }
    });

    return () => {
      try {
        if (dragImageCache.current && dragImageCache.current._holder) {
          const holder = dragImageCache.current._holder;
          try { document.body.removeChild(holder); } catch (e) { }
          dragImageCache.current._holder = null;
        }
      } catch (e) { }
    };
  }, []);

  const formatSubtypeName = (subtype) =>
    String(subtype || '')
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, (c) => c.toUpperCase());

  const normalizeSubtypes = (list) =>
    (list || []).map((item) => {
      if (!item) return null;
      if (typeof item === 'string') {
        return { id: item, name: item, label: formatSubtypeName(item), heuristics: null };
      }
      const id = item.name || item.id || item.type || JSON.stringify(item);
      const name = item.name || item.id || item.type || id;
      const heuristics = item.heuristics || item.description || item.note || null;
      return { id, name, label: formatSubtypeName(name), heuristics };
    }).filter(Boolean);

  // prefetch subtypes
  useEffect(() => {
    COMPONENT_TYPES.forEach((c) => {
      const t = c.type;
      (async () => {
        setLoadingByType((s) => ({ ...s, [t]: true }));
        try {
          const resp = await componentAPI.getSubtypes(t);
          const normalized = normalizeSubtypes(resp?.data?.subtypes || resp?.data || []);
          const withHeuristics = await Promise.all(
            (normalized.length ? normalized : [{ id: 'DEFAULT', name: 'DEFAULT', label: 'Default', heuristics: null }]).map(async (st) => {
              try {
                const hResp = await componentAPI.getHeuristicsForSubtype(t, st.id);
                return { ...st, heuristics: hResp.data };
              } catch (e) { return st; }
            })
          );
          const final = withHeuristics.length ? withHeuristics : [{ id: 'DEFAULT', name: 'DEFAULT', label: 'Default', heuristics: null }];
          setSubtypesByType((s) => ({ ...s, [t]: final }));
          setSelectedByType((s) => ({ ...s, [t]: final[0].id }));
        } catch (err) {
          const fallback = [{ id: 'DEFAULT', name: 'DEFAULT', label: 'Default', heuristics: null }];
          setSubtypesByType((s) => ({ ...s, [t]: fallback }));
          setSelectedByType((s) => ({ ...s, [t]: 'DEFAULT' }));
        } finally {
          setLoadingByType((s) => ({ ...s, [t]: false }));
        }
      })();
    });
  }, []);

  const loadSubtypes = async (type) => {
    if (subtypesByType[type]) return subtypesByType[type];
    setLoadingByType((s) => ({ ...s, [type]: true }));
    try {
      const resp = await componentAPI.getSubtypes(type);
      const normalized = normalizeSubtypes(resp?.data?.subtypes || resp?.data || []);
      const withHeuristics = await Promise.all(
        (normalized.length ? normalized : [{ id: 'DEFAULT', name: 'DEFAULT', label: 'Default', heuristics: null }]).map(async (st) => {
          try {
            const hResp = await componentAPI.getHeuristicsForSubtype(type, st.id);
            return { ...st, heuristics: hResp.data };
          } catch (e) { return st; }
        })
      );
      const final = withHeuristics.length ? withHeuristics : [{ id: 'DEFAULT', name: 'DEFAULT', label: 'Default', heuristics: null }];
      setSubtypesByType((s) => ({ ...s, [type]: final }));
      setSelectedByType((s) => ({ ...s, [type]: (s[type] ?? final[0].id) }));
      return final;
    } catch (err) {
      const fallback = [{ id: 'DEFAULT', name: 'DEFAULT', label: 'Default', heuristics: null }];
      setSubtypesByType((s) => ({ ...s, [type]: fallback }));
      setSelectedByType((s) => ({ ...s, [type]: 'DEFAULT' }));
      return fallback;
    } finally {
      setLoadingByType((s) => ({ ...s, [type]: false }));
    }
  };

  const handleHover = async (type) => {
    if (pinnedType && pinnedType !== type) return;
    setOpenDropdown(type);
    const list = await loadSubtypes(type);
    const chosen = (list || []).find((s) => s.id === selectedByType[type]) || (list || [])[0] || null;
    if (onPreviewSubtype) onPreviewSubtype(type, chosen);
  };

  const handleLeave = (type) => {
    if (pinnedType === type) return;
    setOpenDropdown(null);
    if (!pinnedType && onPreviewSubtype) onPreviewSubtype(null, null);
  };

  const togglePin = (type) => {
    setPinnedType((p) => (p === type ? null : type));
    if (pinnedType !== type) {
      (async () => {
        const list = await loadSubtypes(type);
        setOpenDropdown(type);
        const chosen = (list || []).find((s) => s.id === selectedByType[type]) || (list || [])[0] || null;
        if (onPreviewSubtype) onPreviewSubtype(type, chosen);
      })();
    } else {
      setOpenDropdown(null);
      if (onPreviewSubtype) onPreviewSubtype(null, null);
    }
  };

  const handleSelectSubtype = (type, subtypeId, e) => {
    e && e.stopPropagation();
    setSelectedByType((s) => ({ ...s, [type]: subtypeId }));
    setPinnedType(type);
    setOpenDropdown(type);
    const chosen = (subtypesByType[type] || []).find((s) => s.id === subtypeId) || null;
    if (onPreviewSubtype) onPreviewSubtype(type, chosen);
  };

  const createDragPreviewNode = (componentType) => {
    const cached = dragImageCache.current[componentType] || {};
    if (cached.previewNode) return cached.previewNode;
    const compDef = COMPONENT_TYPES.find((c) => c.type === componentType) || {};
    const color = compDef.color || '#999999';
    const size = 56;
    const node = document.createElement('div');
    node.style.position = 'fixed';
    node.style.pointerEvents = 'none';
    node.style.left = '0px';
    node.style.top = '0px';
    node.style.transform = 'translate(-9999px, -9999px)';
    node.style.width = size + 'px';
    node.style.height = size + 'px';
    node.style.zIndex = '99999';
    node.style.display = 'flex';
    node.style.alignItems = 'center';
    node.style.justifyContent = 'center';
    node.style.background = color;
    node.style.borderRadius = '10px';
    node.style.boxShadow = '0 6px 18px rgba(0,0,0,0.6)';
    node.style.border = '3px solid rgba(255,255,255,0.95)';
    const svgNS = 'http://www.w3.org/2000/svg';
    const svg = document.createElementNS(svgNS, 'svg');
    svg.setAttribute('width', String(size * 0.6));
    svg.setAttribute('height', String(size * 0.6));
    svg.setAttribute('viewBox', '0 0 24 24');
    svg.style.display = 'block';
    const path = document.createElementNS(svgNS, 'path');
    path.setAttribute('d', 'M11 6h2v5h5v2h-5v5h-2v-5H6v-2h5z');
    path.setAttribute('fill', 'white');
    path.setAttribute('opacity', '0.98');
    svg.appendChild(path);
    node.appendChild(svg);
    const labelChar = (compDef.label || componentType || '').toString().charAt(0);
    if (labelChar) {
      const badge = document.createElement('div');
      badge.style.position = 'absolute';
      badge.style.right = '4px';
      badge.style.bottom = '4px';
      badge.style.width = '16px';
      badge.style.height = '16px';
      badge.style.borderRadius = '50%';
      badge.style.background = 'rgba(0,0,0,0.6)';
      badge.style.color = 'white';
      badge.style.fontSize = '11px';
      badge.style.display = 'flex';
      badge.style.alignItems = 'center';
      badge.style.justifyContent = 'center';
      badge.style.fontWeight = '700';
      badge.textContent = labelChar;
      node.appendChild(badge);
    }
    try { document.body.appendChild(node); } catch (e) { }
    dragImageCache.current[componentType] = { ...(dragImageCache.current[componentType] || {}), previewNode: node };
    return node;
  };

  const moveCustomPreview = (evt) => {
    try {
      const node = dragPreviewRef.current;
      if (!node) return;
      const x = ('clientX' in evt) ? evt.clientX : (evt.pageX || 0);
      const y = ('clientY' in evt) ? evt.clientY : (evt.pageY || 0);
      const offsetX = 18;
      const offsetY = 18;
      node.style.transform = `translate(${x + offsetX}px, ${y + offsetY}px)`;
    } catch (e) { }
  };

  const removeCustomPreview = () => {
    try {
      if (dragMoveHandlerRef.current) {
        document.removeEventListener('dragover', dragMoveHandlerRef.current);
        dragMoveHandlerRef.current = null;
      }
      if (dragPreviewRef.current) {
        const n = dragPreviewRef.current;
        try { n.parentNode && n.parentNode.removeChild(n); } catch (e) { }
        dragPreviewRef.current = null;
      }
    } catch (e) { }
  };

  const onDragStart = (event, componentType) => {
    const sublist = subtypesByType[componentType] || [];
    const chosen = sublist.find((s) => s.id === selectedByType[componentType]) || sublist[0] || null;
    const chosenId = chosen ? (chosen.id || chosen.name) : null;
    const payload = JSON.stringify({ type: componentType, subtype: chosenId });
    event.dataTransfer.setData('application/reactflow', payload);
    event.dataTransfer.setData('text/plain', componentType);

    // Hide native ghost then show custom following preview (prefer this reliably across browsers)
    try {
      const img = new Image(); img.src = TRANSPARENT_PNG;
      event.dataTransfer.setDragImage(img, 0, 0);
    } catch (e) { }

    try {
      const node = createDragPreviewNode(componentType);
      if (node.parentNode !== document.body) try { document.body.appendChild(node); } catch (e) {}
      dragPreviewRef.current = node;
      moveCustomPreview(event);
      dragMoveHandlerRef.current = (ev) => moveCustomPreview(ev);
      document.addEventListener('dragover', dragMoveHandlerRef.current);
      const onEnded = () => { removeCustomPreview(); try { event.target && event.target.removeEventListener && event.target.removeEventListener('dragend', onEnded); } catch (e) {} };
      try { event.target && event.target.addEventListener && event.target.addEventListener('dragend', onEnded); } catch (e) {}
    } catch (err) { }
  };

  const onDragEnd = () => { removeCustomPreview(); };

  return (
    <div className="component-palette">
      <div className="palette-header">
        <h3>Components</h3>
        <p className="palette-subtitle">Hover a component to preview subtypes — click a subtype to pin</p>
      </div>

      <div className="palette-items">
        {COMPONENT_TYPES.map((component) => {
          const { type, icon, label, color } = component;
          const subtypes = subtypesByType[type] || [];
          const loading = !!loadingByType[type];
          const selectedId = selectedByType[type];
          const isOpen = openDropdown === type || pinnedType === type;

          return (
            <div
              key={type}
              className={`palette-item ${isOpen ? 'open' : ''}`}
              draggable
              onDragStart={(e) => onDragStart(e, type)}
              onDragEnd={onDragEnd}
              style={{ borderLeftColor: color, zIndex: isOpen ? 50 : 1 }}
              onMouseEnter={() => handleHover(type)}
              onMouseLeave={() => handleLeave(type)}
              onClick={() => togglePin(type)}
            >
              <div className="palette-item-left">
                <span className="palette-item-icon">{icon}</span>
                <div className="palette-item-info">
                  <div className="palette-item-label">{label}</div>
                  <div className="palette-item-type">{type}</div>
                </div>
              </div>

              <div className="palette-item-right">
                <div className="subtype-selected-inline">
                  {subtypes.length ? (subtypes.find((s) => s.id === selectedId)?.label || subtypes[0].label) : 'Loading'}
                </div>

                {isOpen && (
                  <div
                    className="subtype-panel below"
                    onMouseDown={(e) => e.stopPropagation()}
                    ref={panelRef}
                  >
                    {loading ? (
                      <div className="subtype-loading">Loading…</div>
                    ) : (
                      <ul className="subtype-list-panel">
                        {subtypes.map((st) => (
                          <li
                            key={st.id}
                            className={`subtype-panel-item ${selectedId === st.id ? 'selected' : ''}`}
                            onClick={(e) => handleSelectSubtype(type, st.id, e)}
                            draggable
                            onDragStart={(e) => onDragStart(e, type)}
                            onDragEnd={onDragEnd}
                          >
                            <div className="sp-left">
                              <div className="sp-title">{st.label}</div>
                            </div>
                            <div className="sp-right" style={{ minWidth: 24 }}>
                              {selectedId === st.id && <FaCheck className="subtype-check" />}
                            </div>
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      <div className="palette-footer">
        <div className="palette-help">
          <strong>Quick Tips:</strong>
          <ul>
            <li>Hover a component to preview subtypes</li>
            <li>Click a subtype to select & pin details in the right panel</li>
            <li>Drag the component to the canvas — selected subtype is included</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default ComponentPalette;

