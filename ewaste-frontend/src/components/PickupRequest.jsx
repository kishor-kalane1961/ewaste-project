import React, { useState } from "react";
import "../styles/PickupRequest.css";

export default function PickupRequest() {
  const [formData, setFormData] = useState({
    mobileNo: "",
    pickupAddress: "",
    deviceType: "MOBILE", // default
    model: "",
    pickupDate: "",
    pickupTime: "",
    condition: "WORKING", // default
    quantity: 1,
    photo: null,
  });

  const handleChange = (e) => {
    const { name, value, files } = e.target;
    if (name === "photo") {
      setFormData({ ...formData, photo: files[0] });
    } else {
      setFormData({ ...formData, [name]: value });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const data = new FormData();
    Object.keys(formData).forEach((key) => {
      data.append(key, formData[key]);
    });

    try {
      const token = localStorage.getItem("token");

      const response = await fetch("http://localhost:8084/api/pickup/create", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,   // ✅ fixed
        },
        body: data,
      });


      if (response.ok) {
        alert("Pickup request submitted successfully!");
        setFormData({
          mobileNo: "",
          pickupAddress: "",
          deviceType: "MOBILE",
          model: "",
          pickupDate: "",
          pickupTime: "",
          condition: "WORKING",
          quantity: 1,
          photo: null,
        });
      } else if (response.status === 401) {
        alert("Unauthorized! Please login again.");
      } else if (response.status === 403) {
        alert("Forbidden! Your account does not have access.");
      } else {
        const err = await response.json();
        alert("Failed: " + err.message);
      }
    } catch (error) {
      console.error("Error:", error);
      alert("Something went wrong!");
    }
  };

  return (
    <div className="pickup-container">
      <form className="pickup-form" onSubmit={handleSubmit}>
        <h2>Schedule a Pickup</h2>

        <label>Mobile Number</label>
        <input
          type="text"
          name="mobileNo"
          value={formData.mobileNo}
          onChange={handleChange}
          required
        />

        <label>Pickup Address</label>
        <textarea
          name="pickupAddress"
          value={formData.pickupAddress}
          onChange={handleChange}
          required
        />

        <label>Device Type</label>
        <select name="deviceType" value={formData.deviceType} onChange={handleChange}>
          <option value="MOBILE">Mobile</option>
          <option value="LAPTOP">Laptop</option>
          <option value="TABLET">Tablet</option>
          <option value="PRINTER">Printer</option>
          <option value="TV">TV</option>
          <option value="OTHER">Other</option>
        </select>

        <label>Model</label>
        <input
          type="text"
          name="model"
          value={formData.model}
          onChange={handleChange}
          required
        />

        <label>Pickup Date</label>
        <input
          type="date"
          name="pickupDate"
          value={formData.pickupDate}
          onChange={handleChange}
          required
        />

        <label>Pickup Time</label>
        <input
          type="time"
          name="pickupTime"
          value={formData.pickupTime}
          onChange={handleChange}
          required
        />

        <label>Condition</label>
        <select name="condition" value={formData.condition} onChange={handleChange}>
          <option value="WORKING">Working</option>
          <option value="DAMAGED">Damaged</option>
          <option value="DEAD">Dead</option>
        </select>

        <label>Quantity</label>
        <input
          type="number"
          name="quantity"
          value={formData.quantity}
          min="1"
          onChange={handleChange}
          required
        />

        <label>Upload Photo</label>
        <input type="file" name="photo" onChange={handleChange} />

        <button type="submit" className="submit-btn">
          Submit Request
        </button>
      </form>
    </div>
  );
}