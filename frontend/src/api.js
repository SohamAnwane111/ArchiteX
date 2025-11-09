import axios from 'axios';

const API_BASE_URL = 'https://synchack-production.up.railway.app/api';
// const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: false, // Set to false if backend doesn't use credentials
});

// Add request interceptor to handle CORS
api.interceptors.request.use(
    (config) => {
      // Add any additional headers if needed
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
);

// Add response interceptor to handle errors
api.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 0 || error.code === 'ERR_NETWORK') {
        console.error('CORS or Network Error:', error);
      }
      return Promise.reject(error);
    }
);

// Component APIs
export const componentAPI = {
  getAll: () => api.get('/components'),
  getById: (id) => api.get(`/components/${id}`),
  create: (data) => api.post('/components', data),
  update: (id, data) => api.put(`/components/${id}`, data),
  delete: (id) => api.delete(`/components/${id}`),
  getTypes: () => api.get('/components/types'),
  getByType: (type) => api.get(`/components/type/${type}`),
  getSubtypes: (type) => api.get(`/components/subtypes/${type}`),
  getHeuristicsForSubtype: (type, subtype) => api.get(`/components/heuristics/${type}/${subtype}`),
};

// Link APIs
export const linkAPI = {
  getAll: () => api.get('/links'),
  getById: (id) => api.get(`/links/${id}`),
  create: (data) => api.post('/links', data),
  delete: (id) => api.delete(`/links/${id}`),
  validate: (data) => api.post('/links/validate', data),
  suggest: (data) => api.post('/links/suggest', data),
  getTypes: () => api.get('/links/types'),
  getForComponent: (componentId) => api.get(`/links/component/${componentId}`),
  getHeuristics: (id) => api.get(`/links/${id}/heuristics`),
  getDefaultHeuristics: (linkType) => api.get(`/links/heuristics/default/${linkType}`),
  updateHeuristics: (id, data) => api.put(`/links/${id}/heuristics`, data),
};

// Architecture APIs
export const architectureAPI = {
  getAll: () => api.get('/architecture'),
  getById: (id) => api.get(`/architecture/${id}`),
  create: (data) => api.post('/architecture', data),
  update: (id, data) => api.put(`/architecture/${id}`, data),
  delete: (id) => api.delete(`/architecture/${id}`),
  addComponent: (id, component) => api.post(`/architecture/${id}/components`, component),
  addLink: (id, link) => api.post(`/architecture/${id}/links`, link),
  evaluate: (data) => api.post('/architecture/evaluate', data),
  getScore: (id) => api.get(`/architecture/${id}/score`),
  visualize: (id) => api.get(`/architecture/visualize/${id}`),
  compare: (data) => api.post('/architecture/compare', data),
  validate: (id) => api.post(`/architecture/${id}/validate`),
  submit: (id, data) => api.post(`/architecture/${id}/submit`, data),
  copy: (id, data) => api.post(`/architecture/${id}/copy`, data),
  getByUser: (userId) => api.get(`/architecture/user/${userId}`),
  getByQuestion: (questionId) => api.get(`/architecture/question/${questionId}`),
  getSubmitted: () => api.get('/architecture/submitted'),
  getRules: () => api.get('/architecture/rules'),
  getRulesByLinkType: (linkType) => api.get(`/architecture/rules/${linkType}`),
};

export default api;
