import axios from "axios";

const API_URL = "http://localhost:8084/api"; // Spring Boot backend URL

const api = axios.create({
  baseURL: API_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 30000, // 30s timeout for requests
});

// ---------------- Request Interceptor ----------------
// Attach JWT token automatically if available
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ---------------- Response Interceptor ----------------
// Handle common errors globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      // Backend returned an error response
      if (error.response.status === 401) {
        console.warn("Unauthorized! Redirecting to login...");
        localStorage.removeItem("token");
        window.location.href = "/login"; // or handle via router
      }
      if (error.response.status === 403) {
        alert("Access denied! You do not have permission to perform this action.");
      }
    } else if (error.request) {
      console.error("No response received from server:", error.request);
    } else {
      console.error("Error setting up request:", error.message);
    }
    return Promise.reject(error);
  }
);

export default api;
