import React, { useState, useEffect } from "react";
import api from "../services/api";
import "../styles/ScheduleModal.css";

export default function ScheduleModal({ request, onClose }) {
  const [date, setDate] = useState("");
  const [time, setTime] = useState("");
  const [pickupPersons, setPickupPersons] = useState([]);
  const [selectedPersonId, setSelectedPersonId] = useState("");
  const [errorMessage, setErrorMessage] = useState(""); // New: show error from backend
  const [loading, setLoading] = useState(false);

  // Fetch all pickup persons
  useEffect(() => {
    const fetchPersons = async () => {
      try {
        const res = await api.get("/pickup-persons");
        setPickupPersons(res.data);
      } catch (err) {
        console.error("Error fetching pickup persons:", err.response || err.message);
        setErrorMessage("Failed to load pickup persons.");
      }
    };
    fetchPersons();
  }, []);

  const handleSchedule = async () => {
    if (!date || !time || !selectedPersonId) {
      setErrorMessage("Please fill date, time, and select a pickup person");
      return;
    }

    setLoading(true);
    setErrorMessage(""); // clear previous errors

    try {
      // Schedule request
      await api.put(`/admin/requests/${request.id}/schedule`, {
        pickupDate: date,
        pickupTime: time,
      });

      // Assign pickup person
      await api.put(`/admin/requests/${request.id}/assign/${selectedPersonId}`);

      onClose();
    } catch (err) {
      console.error("Axios error response:", err.response);
      console.error("Axios error message:", err.message);

      // Show backend error message if available
      const backendMsg = err.response?.data?.message || err.response?.data || err.message;
      setErrorMessage(`Failed to schedule request: ${backendMsg}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal">
      <div className="modal-content">
        <h3>Schedule Pickup for Request {request.id}</h3>

        {errorMessage && <p className="error-message">{errorMessage}</p>}

        <input
          type="date"
          value={date}
          onChange={(e) => setDate(e.target.value)}
        />
        <input
          type="time"
          value={time}
          onChange={(e) => setTime(e.target.value)}
        />

        <select
          value={selectedPersonId}
          onChange={(e) => setSelectedPersonId(e.target.value)}
        >
          <option value="">Select Pickup Person</option>
          {pickupPersons.map((person) => (
            <option key={person.id} value={person.id}>
              {person.name}
            </option>
          ))}
        </select>

        <div className="actions">
          <button onClick={handleSchedule} disabled={loading}>
            {loading ? "Saving..." : "Save"}
          </button>
          <button onClick={onClose} disabled={loading}>
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
}
