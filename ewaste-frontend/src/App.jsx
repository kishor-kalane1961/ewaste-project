import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Home from "./components/Home";
import Register from "./components/Register";
import Login from "./components/Login";
import UserDashboard from "./components/UserDashboard";
import AdminDashboard from "./components/AdminDashboard";
import PickupPersonDashboard from "./components/PickupPersonDashboard";

// Helper function to get user role from token
const getUserRole = () => {
  const token = localStorage.getItem("token");
  if (!token) return null;
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.role; // Ensure role is stored in JWT as "role"
  } catch (err) {
    return null;
  }
};

// Protected route component
const ProtectedRoute = ({ element, allowedRoles }) => {
  const role = getUserRole();
  if (!role || !allowedRoles.includes(role)) {
    return <Navigate to="/login" replace />;
  }
  return element;
};

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />

        {/* User Dashboard */}
        <Route
          path="/user-dashboard"
          element={<ProtectedRoute element={<UserDashboard />} allowedRoles={["USER"]} />}
        />

        {/* Admin Dashboard */}
        <Route
          path="/admin-dashboard"
          element={<ProtectedRoute element={<AdminDashboard />} allowedRoles={["ADMIN"]} />}
        />

        {/* PickupPerson Dashboard */}
        <Route
          path="/pickup-dashboard"
          element={<ProtectedRoute element={<PickupPersonDashboard />} allowedRoles={["PICKUP_PERSON"]} />}
        />

        {/* Fallback for unmatched routes */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
