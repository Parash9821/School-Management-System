import React from "react";
import "../styles/Footer.css";
export default function Footer() {
  const year = new Date().getFullYear();

  return (
    <footer className="footer">
      <div className="footer-container">

        <div className="footer-left">
          <h3>School Management System</h3>
          <p>Managing students, teachers and results efficiently.</p>
        </div>

        <div className="footer-center">
          <p>© {year} School Management System</p>
          <p>All Rights Reserved</p>
        </div>

        <div className="footer-right">
          <p>Developed by</p>
          <strong>Parash</strong>
        </div>

      </div>
    </footer>
  );
}