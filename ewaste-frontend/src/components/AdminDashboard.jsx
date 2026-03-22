import React, { useEffect, useState } from "react";
import api from "../services/api";
import RequestDetails from "./RequestDetails";
import PickupPersonForm from "./PickupPersonForm";
import "../styles/AdminDashboard.css";
import jsPDF from "jspdf";
import "jspdf-autotable";

export default function AdminDashboard() {
  const [requests, setRequests] = useState([]);
  const [filter, setFilter] = useState("ALL");
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [showAddPerson, setShowAddPerson] = useState(false);

  // Reports
  const [reportData, setReportData] = useState([]);
  const [reportMode, setReportMode] = useState("NONE"); // DAILY, WEEKLY, CUSTOM
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  // ------------------- LOGOUT -------------------
  const handleLogout = () => {
    // Example: clear localStorage/session and redirect
    localStorage.clear();
    window.location.href = "/login";
  };

  // ------------------- FETCH REQUESTS -------------------
  const fetchRequests = async () => {
    try {
      let url = "/admin/requests";
      if (filter !== "ALL") url += `/status/${filter}`;
      const res = await api.get(url);
      setRequests(res.data);
    } catch (err) {
      console.error("Error fetching requests:", err);
    }
  };

  // ------------------- FETCH COMPLETED REPORTS -------------------
  const fetchReports = async (download = false) => {
    try {
      if (reportMode === "WEEKLY") {
        const res = await api.get("/admin/reports/weekly", { responseType: "blob" });
        if (download) {
          const url = window.URL.createObjectURL(new Blob([res.data]));
          const link = document.createElement("a");
          link.href = url;
          link.setAttribute("download", "weekly_report.pdf");
          document.body.appendChild(link);
          link.click();
          link.remove();
        }
        return;
      }

      let res;
      if (reportMode === "DAILY") res = await api.get("/admin/reports/daily");
      else if (reportMode === "CUSTOM" && startDate && endDate)
        res = await api.get(`/admin/reports/custom?start=${startDate}&end=${endDate}`);
      else return;

      setReportData(res.data);
      if (download) downloadPDF(res.data);
    } catch (err) {
      console.error("Error fetching report:", err);
      setReportData([]);
    }
  };

  const downloadPDF = (data) => {
    const doc = new jsPDF();
    doc.text("Completed Requests Report", 14, 16);
    const tableColumn = [
      "Sr. No.",
      "User Name",
      "Email",
      "Mobile No",
      "Pickup Person",
      "Device Type",
      "Model Name"
    ];

    const tableRows = data.map((req, index) => [
      index + 1,
      req.user?.username || "",
      req.user?.email || "",
      req.mobileNo || "",
      req.assignedPickupPerson?.name || "",
      req.deviceType || "",
      req.model || "",
      `${req.pickupDate || ""} ${req.pickupTime || ""}`,
    ]);

    doc.autoTable({ head: [tableColumn], body: tableRows, startY: 20, theme: "grid", headStyles: { fillColor: [22, 160, 133] } });
    doc.save(`completed_requests_${reportMode.toLowerCase()}.pdf`);
  };

  useEffect(() => {
    fetchRequests();
  }, [filter]);

  return (
    <div className="admin-dashboard">
      <div className="dashboard-header">
        <h1>Admin Dashboard</h1>
        <button className="logout-btn" onClick={handleLogout}>Logout</button>
      </div>

      {/* Filters + Add Pickup Person */}
      <div className="filters">
        {["ALL", "PENDING", "APPROVED", "REJECTED", "SCHEDULED"].map((f) => (
          <button key={f} className={filter === f ? "active" : ""} onClick={() => setFilter(f)}>
            {f}
          </button>
        ))}
        <button onClick={() => setShowAddPerson(true)}>Add Pickup Person</button>
      </div>

      {/* Requests Table */}
      <table className="requests-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>User</th>
            <th>Device</th>
            <th>Model</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {requests.map((req) => (
            <tr key={req.id}>
              <td>{req.id}</td>
              <td>{req.user?.username}</td>
              <td>{req.deviceType}</td>
              <td>{req.model}</td>
              <td>{req.status}</td>
              <td><button onClick={() => setSelectedRequest(req)}>View</button></td>
            </tr>
          ))}
        </tbody>
      </table>

      {selectedRequest && <RequestDetails request={selectedRequest} onClose={() => { setSelectedRequest(null); fetchRequests(); }} />}
      {showAddPerson && <PickupPersonForm onClose={() => { setShowAddPerson(false); fetchRequests(); }} />}

      {/* Reports Section */}
      <div className="reports-section">
        <h2>Completed Requests Reports</h2>
        <div className="report-filters">
          <button className={reportMode === "DAILY" ? "active" : ""} onClick={() => { setReportMode("DAILY"); fetchReports(true); }}>Daily</button>
          <button className={reportMode === "WEEKLY" ? "active" : ""} onClick={() => { setReportMode("WEEKLY"); fetchReports(true); }}>Weekly</button>
          <button className={reportMode === "CUSTOM" ? "active" : ""} onClick={() => setReportMode("CUSTOM")}>Custom</button>
        </div>

        {reportMode === "CUSTOM" && (
          <div className="custom-range">
            <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
            <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
            <button onClick={() => fetchReports(true)}>Generate PDF</button>
          </div>
        )}

        {reportMode !== "WEEKLY" && reportData.length > 0 ? (
          <table className="report-table">
            <thead>
              <tr>
                <th>Sr. No.</th>
                <th>User Name</th>
                <th>Email</th>
                <th>Mobile No</th>
                <th>Pickup Person</th>
                <th>Device Type</th>
                <th>Model Name</th>
                <th>Completed Date & Time</th>
              </tr>
            </thead>
            <tbody>
              {reportData.map((req, index) => (
                <tr key={req.id || index}>
                  <td>{index + 1}</td>
                  <td>{req.user?.username}</td>
                  <td>{req.user?.email}</td>
                  <td>{req.mobileNo}</td>
                  <td>{req.assignedPickupPerson?.name}</td>
                  <td>{req.deviceType}</td>
                  <td>{req.model}</td>
                  <td>{`${req.pickupDate} ${req.pickupTime}`}</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : reportMode !== "NONE" && reportMode !== "WEEKLY" ? (
          <p>No completed requests found for this period.</p>
        ) : null}
      </div>
    </div>
  );
}
