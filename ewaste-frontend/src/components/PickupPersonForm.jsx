import React, { useState } from "react";
import api from "../services/api";
import "../styles/PickupPerson.css";

export default function PickupPersonForm({ onClose, onSaved }) {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [mobile, setMobile] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSave = async () => {
    // ✅ Validate all fields
    if (!name || !email || !mobile || !password) {
      return alert("Please fill all fields including password");
    }

    setLoading(true);

    try {
      const res = await api.post("/pickup-persons", {
        name,
        email,
        mobile,
        password, // Include password in the payload
        available: true,
      });

      if (res.status === 200 || res.data?.id) {
        alert(`Pickup Person "${res.data.name}" added successfully!`);

        if (onSaved) onSaved(res.data);
        onClose();
      } else {
        alert("Failed to add pickup person: Unexpected response from server");
        console.error(res);
      }
    } catch (err) {
      console.error(err);
      const message = err.response?.data?.message || err.message || "Unknown error";
      alert(`Failed to add pickup person: ${message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal">
      <div className="modal-content">
        <h3>Add Pickup Person</h3>
        <input
          placeholder="Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <input
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <input
          placeholder="Mobile"
          value={mobile}
          onChange={(e) => setMobile(e.target.value)}
        />
        <input
          placeholder="Password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <div className="actions">
          <button onClick={handleSave} disabled={loading}>
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
