import React, { useEffect, useState } from "react";
import api from "../services/api";
import "../styles/PickupPersonDashboard.css";

export default function PickupPersonDashboard() {
  const [requests, setRequests] = useState([]);
  const [selectedStatus, setSelectedStatus] = useState("ALL");
  const [loading, setLoading] = useState(false);
  const [otpSentRequestId, setOtpSentRequestId] = useState(null);
  const [enteredOtp, setEnteredOtp] = useState("");

  const handleLogout = () => {
    localStorage.clear();
    window.location.href = "/login";
  };

  const fetchRequests = async () => {
    try {
      const res = await api.get("/pickup-persons/dashboard/assigned-requests");
      setRequests(res.data);
    } catch (err) {
      console.error("Error fetching requests:", err);
    }
  };

  const updateStatus = async (id, status) => {
    setLoading(true);
    try {
      await api.put(`/pickup-persons/request/${id}/status`, { status });
      await fetchRequests();
      if (status !== "PICKED_UP") {
        setOtpSentRequestId(null);
        setEnteredOtp("");
      }
    } catch (err) {
      console.error("Error updating status:", err);
      alert("Failed to update status. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const sendOtp = async (id) => {
    try {
      await api.post(`/pickup-persons/pickup/${id}/generate-otp`);
      setOtpSentRequestId(id);
      alert("OTP sent to user's email!");
    } catch (err) {
      console.error("Error sending OTP:", err);
      alert("Failed to send OTP. Try again.");
    }
  };

  const verifyOtp = async (id) => {
    try {
      await api.post(`/pickup-persons/pickup/${id}/verify-otp`, { otp: enteredOtp });
      alert("OTP verified successfully! Pickup confirmed.");
      setEnteredOtp("");
      setOtpSentRequestId(null);
      fetchRequests();
    } catch (err) {
      console.error("Error verifying OTP:", err);
      alert(err.response?.data || "Invalid OTP. Please try again.");
    }
  };

  useEffect(() => {
    fetchRequests();
  }, []);

  const filteredRequests =
    selectedStatus === "ALL"
      ? requests
      : requests.filter((r) => r.status === selectedStatus);

  return (
    <div className="pickup-dashboard">
      {/* ---------------- Header ---------------- */}
      <div className="dashboard-header">
        <h1>Pickup Person Dashboard</h1>
        <button className="logout-btn" onClick={handleLogout}>Logout</button>
      </div>

      {/* Filter Buttons */}
      <div className="status-filters">
        {["ALL", "PENDING", "SCHEDULED", "PICKED_UP", "COMPLETED", "CANCELLED"].map(
          (status) => (
            <button
              key={status}
              className={selectedStatus === status ? "active" : ""}
              onClick={() => setSelectedStatus(status)}
            >
              {status.replace("_", " ")}
            </button>
          )
        )}
      </div>

      {/* Requests Table */}
      <div className="requests-container">
        {filteredRequests.length === 0 && <p>No requests available</p>}
        {filteredRequests.map((req) => (
          <div key={req.id} className="request-card">
            <div className="request-info">
              <p><strong>User:</strong> {req.user.username}</p>
              <p><strong>Device:</strong> {req.deviceType} ({req.model})</p>
              <p><strong>Quantity:</strong> {req.quantity}</p>
              <p><strong>Address:</strong> {req.pickupAddress}</p>
              <p><strong>Date/Time:</strong> {req.pickupDate} {req.pickupTime}</p>
              <p>
                <strong>Status:</strong>{" "}
                <span className={`status ${req.status.toLowerCase()}`}>
                  {req.status.replace("_", " ")}
                </span>
              </p>
            </div>

            <div className="request-actions">
              {req.status !== "COMPLETED" && req.status !== "CANCELLED" && (
                <>
                  {req.status === "SCHEDULED" && otpSentRequestId !== req.id && (
                    <button onClick={() => sendOtp(req.id)} disabled={loading}>
                      Send OTP
                    </button>
                  )}

                  {otpSentRequestId === req.id && (
                    <>
                      <input
                        type="text"
                        placeholder="Enter OTP"
                        value={enteredOtp}
                        onChange={(e) => setEnteredOtp(e.target.value)}
                      />
                      <button
                        onClick={() => verifyOtp(req.id)}
                        disabled={loading || !enteredOtp}
                      >
                        Verify OTP
                      </button>
                    </>
                  )}

                  <button onClick={() => updateStatus(req.id, "COMPLETED")} disabled={loading}>
                    Completed
                  </button>
                  <button onClick={() => updateStatus(req.id, "CANCELLED")} disabled={loading}>
                    Cancel
                  </button>
                </>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
