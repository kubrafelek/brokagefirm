import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

class ApiService {
  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Add request interceptor to add auth headers
    this.api.interceptors.request.use(
      (config) => {
        const user = this.getCurrentUser();
        if (user && user.username && user.password) {
          config.headers['Username'] = user.username;
          config.headers['Password'] = user.password;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Add response interceptor for error handling
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          this.logout();
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  // Authentication methods
  async login(username, password) {
    try {
      const response = await this.api.post('/auth/login', {
        username,
        password
      });

      if (response.data && response.data.userId) {
        const userData = {
          username,
          password, // Store password for subsequent API calls
          userId: response.data.userId,
          isAdmin: response.data.isAdmin,
          message: response.data.message
        };
        localStorage.setItem('user', JSON.stringify(userData));
        return userData;
      }
      throw new Error('Invalid response format');
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Login failed');
    }
  }

  logout() {
    localStorage.removeItem('user');
  }

  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  }

  isAuthenticated() {
    return !!this.getCurrentUser();
  }

  isAdmin() {
    const user = this.getCurrentUser();
    return user?.isAdmin || false;
  }

  // Orders API
  async getOrders(userId = null, startDate = null, endDate = null) {
    const params = {};
    if (userId) params.userId = userId;
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;

    const response = await this.api.get('/orders', { params });
    return response.data;
  }

  async createOrder(orderData) {
    const response = await this.api.post('/orders', orderData);
    return response.data;
  }

  async cancelOrder(orderId) {
    const response = await this.api.delete(`/orders/${orderId}`);
    return response.data;
  }

  async getPendingOrders() {
    const response = await this.api.get('/orders/pending');
    return response.data;
  }

  async matchOrder(orderId) {
    const response = await this.api.post('/orders/match', { orderId });
    return response.data;
  }

  // Assets API
  async getAssets(userId = null) {
    const user = this.getCurrentUser();
    const params = {};

    // Only add userId parameter if user is admin and userId is provided
    if (user && user.isAdmin && userId) {
      params.userId = userId;
    }
    // For regular customers, don't send userId parameter - backend will use authenticated user's ID

    const response = await this.api.get('/assets', { params });
    return response.data;
  }

  // Get available asset names for dropdown population
  async getAvailableAssets() {
    try {
      // Use the new backend endpoint for available assets
      const response = await this.api.get('/assets/available');
      return response.data;
    } catch (error) {
      console.error('Failed to fetch available assets:', error);
      // Fallback to hardcoded assets if API fails
      return ['AAPL', 'GOOGL', 'MSFT', 'NVDA', 'TSLA'];
    }
  }

  // Health check
  async getHealth() {
    const response = await this.api.get('/health', {
      baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080'
    });
    return response.data;
  }
}

export default new ApiService();
