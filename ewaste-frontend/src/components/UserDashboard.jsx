import React, { useState, useEffect } from "react";
import "../styles/UserDashboard.css";
import PickupRequest from "./PickupRequest";

export default function UserDashboard() {
  const [activeTab, setActiveTab] = useState("pickup");
  const [user, setUser] = useState(null);
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    mobile: "",
    address: "",
    dob: "",
    gender: "",
    profilePic: null,
  });
  const [requests, setRequests] = useState([]);

  const token = localStorage.getItem("token");

  const handleLogout = () => {
      // Example: clear localStorage/session and redirect
      localStorage.clear();
      window.location.href = "/login";
    };
  useEffect(() => {
    fetch("", {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then((data) => {
        setUser(data);
        setFormData({
          name: data.name || "",
          email: data.email || "",
          mobile: data.mobile || "",
          address: data.address || "",
          dob: data.dob || "",
          gender: data.gender || "",
          profilePic: data.profilePic || null,
        });
      })
      .catch((err) => console.error("Error fetching user:", err));
  }, [token]);

  useEffect(() => {
    fetch("http://localhost:8084/api/pickup/my-requests", {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then((data) => {
        if (data.status === "success") {
          setRequests(data.data);
        } else {
          setRequests([]);
        }
      })
      .catch((err) => console.error("Error fetching requests:", err));
  }, [token]);

  const handleChange = (e) => {
    const { name, value, files } = e.target;
    if (files) {
      setFormData({ ...formData, [name]: files[0] });
    } else {
      setFormData({ ...formData, [name]: value });
    }
  };

  const handleSave = async (e) => {
    e.preventDefault();
    const data = new FormData();
    Object.keys(formData).forEach((key) => {
      if (formData[key]) data.append(key, formData[key]);
    });

    try {
      const response = await fetch("http://localhost:8084/api/users/update", {
        method: "PUT",
        headers: { Authorization: `Bearer ${token}` },
        body: data,
      });

      if (!response.ok) throw new Error("Failed to update profile");

      const updatedUser = await response.json();
      setUser(updatedUser);
      setActiveTab("profile");
    } catch (error) {
      console.error("Error updating profile:", error);
    }
  };

  return (
    <div className="dashboard">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="profile">
          <img
            src={user?.profilePic || "https://via.placeholder.com/120"}
            alt="Profile"
            className="profile-pic"
          />
          <h3 className="username">{user?.name || "New User"}</h3>
          <p className="user-email">{user?.email}</p>
        </div>

        <nav className="nav">
          <button
            className={activeTab === "editProfile" ? "active" : ""}
            onClick={() => setActiveTab("editProfile")}
          >
            Edit Profile
          </button>
          <button
            className={activeTab === "profile" ? "active" : ""}
            onClick={() => setActiveTab("profile")}
          >
            Profile
          </button>
          <button
            className={activeTab === "pickup" ? "active" : ""}
            onClick={() => setActiveTab("pickup")}
          >
            Pickup Request
          </button>
          <button
            className={activeTab === "status" ? "active" : ""}
            onClick={() => setActiveTab("status")}
          >
            My Requests
          </button>
            {/* ✅ Logout Button */}
            <button className="logout" onClick={handleLogout}>
              Logout
            </button>
        </nav>
      </aside>

      {/* Main Content */}
      <main className="content">
        {/* Edit Profile */}
        {activeTab === "editProfile" && (
          <div className="edit-profile">
            <h2>Fill Your Details</h2>
            <form onSubmit={handleSave} className="profile-form">
              <label>Name</label>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleChange}
                required
              />

              <label>Email</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                required
              />

              <label>Mobile</label>
              <input
                type="text"
                name="mobile"
                value={formData.mobile}
                onChange={handleChange}
                required
              />

              <label>Address</label>
              <input
                type="text"
                name="address"
                value={formData.address}
                onChange={handleChange}
              />

              <label>Date of Birth</label>
              <input
                type="date"
                name="dob"
                value={formData.dob}
                onChange={handleChange}
              />

              <label>Gender</label>
              <select
                name="gender"
                value={formData.gender}
                onChange={handleChange}
              >
                <option value="">Select</option>
                <option value="Male">Male</option>
                <option value="Female">Female</option>
              </select>

              <label>Profile Picture</label>
              <input
                type="file"
                name="profilePic"
                accept="image/*"
                onChange={handleChange}
              />

              <button type="submit">Save</button>
            </form>
          </div>
        )}

        {/* Profile View */}
        {activeTab === "profile" && user && (
          <div className="profile-view">
            <h2>My Profile</h2>
            <div className="profile-card">
              <img
                src={user.profilePic || "https://via.placeholder.com/150"}
                alt="User"
                className="profile-pic-large"
              />
              <div className="profile-details">
                <p><strong>Name:</strong> {user.name}</p>
                <p><strong>Email:</strong> {user.email}</p>
                <p><strong>Mobile:</strong> {user.mobile}</p>
                <p><strong>Address:</strong> {user.address}</p>
                <p><strong>Date of Birth:</strong> {user.dob}</p>
                <p><strong>Gender:</strong> {user.gender}</p>
              </div>
            </div>
          </div>
        )}

        {/* Pickup Request Form */}
        {activeTab === "pickup" && <PickupRequest />}

        {/* Requests History */}
        {activeTab === "status" && (
          <div className="requests">
            <h2>My Requests</h2>
            {requests.length === 0 ? (
              <p>No requests found.</p>
            ) : (
              <table className="requests-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Device</th>
                    <th>Model</th>
                    <th>Condition</th>
                    <th>Quantity</th>
                    <th>Pickup Date</th>
                    <th>Pickup Time</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {requests.map((req) => (
                    <tr key={req.id}>
                      <td>{req.id}</td>
                      <td>{req.deviceType}</td>
                      <td>{req.model}</td>
                      <td>{req.condition}</td>
                      <td>{req.quantity}</td>
                      <td>{req.pickupDate}</td>
                      <td>{req.pickupTime}</td>
                      <td>{req.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </main>
    </div>
  );
}
