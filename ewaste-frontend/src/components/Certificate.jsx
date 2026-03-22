import React from "react";
import html2canvas from "html2canvas";
import jsPDF from "jspdf";
import "../styles/Certificate.css";

export default function Certificate({ userName }) {
  // function to capture certificate and download as PDF
  const downloadCertificate = () => {
    const input = document.getElementById("certificate");
    html2canvas(input, { scale: 2 }).then((canvas) => {
      const imgData = canvas.toDataURL("image/png");
      const pdf = new jsPDF("landscape", "mm", "a4");
      const imgProps = pdf.getImageProperties(imgData);
      const pdfWidth = pdf.internal.pageSize.getWidth();
      const pdfHeight = (imgProps.height * pdfWidth) / imgProps.width;
      pdf.addImage(imgData, "PNG", 0, 0, pdfWidth, pdfHeight);
      pdf.save("certificate.pdf");
    });
  };

  return (
    <div className="certificate-container">
      {/* Certificate Preview */}
      <div id="certificate" className="certificate">
        <h1 className="title">Certificate of Appreciation</h1>
        <p className="subtitle">This is proudly presented to</p>
        <h2 className="username">{userName}</h2>
        <p className="description">
          For successfully completing 5 pickup requests and contributing to
          sustainable e-waste management.
        </p>
        <p className="footer">Issued on: {new Date().toLocaleDateString()}</p>
      </div>

      {/* Button */}
      <button className="download-btn" onClick={downloadCertificate}>
        Download Certificate
      </button>
    </div>
  );
}
