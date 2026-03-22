import React, { useState, useEffect } from "react";
import api from "../services/api";
import ScheduleModal from "./ScheduleModal";
import "../styles/RequestDetails.css";

export default function RequestDetails({ request, onClose }) {
  const [showSchedule, setShowSchedule] = useState(false);
  const [reason, setReason] = useState("");
  const [currentSrc, setCurrentSrc] = useState("/placeholder.png");

  const BASE_URL = "http://localhost:8084/uploads/";

  useEffect(() => {
    if (request?.photoPath) {
      setCurrentSrc(BASE_URL + request.photoPath);
    } else {
      setCurrentSrc("/placeholder.png");
    }
  }, [request]);

  const handleApprove = async () => {
    try {
      await api.put(`/admin/requests/${request.id}/approve`);
      onClose();
    } catch (err) {
      console.error(err);
      alert("Failed to approve request");
    }
  };

  const handleReject = async () => {
    if (!reason.trim()) return alert("Please enter rejection reason");
    try {
      await api.put(`/admin/requests/${request.id}/reject`, { reason });
      onClose();
    } catch (err) {
      console.error(err);
      alert("Failed to reject request");
    }
  };

  if (!request) return <p>Loading...</p>;

  return (
    <div className="modal">
      <div className="modal-content">
        <h2 className="modal-title">Request Details</h2>

        <div className="details">
          <p><strong>ID:</strong> {request.id}</p>
          <p><strong>Device Type:</strong> {request.deviceType}</p>
          <p><strong>Model Name:</strong> {request.model}</p>
          <p><strong>Status:</strong> {request.status}</p>
        </div>

        <div className="device-photo">
          <img
            src={currentSrc}
            alt="Device"
            className="device-img"
            onError={(e) => (e.target.src = "/placeholder.png")}
          />
        </div>

        <div className="actions">
          <button className="approve-btn" onClick={handleApprove}>
            Approve
          </button>
          <button className="schedule-btn" onClick={() => setShowSchedule(true)}>
            Schedule Pickup
          </button>

          <input
            type="text"
            placeholder="Reason for rejection"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
          />
          <button className="reject-btn" onClick={handleReject}>
            Reject
          </button>

          <button className="close-btn" onClick={onClose}>
            Close
          </button>
        </div>
      </div>

      {showSchedule && (
        <ScheduleModal
          request={request}
          onClose={() => setShowSchedule(false)}
        />
      )}
    </div>
  );
}
