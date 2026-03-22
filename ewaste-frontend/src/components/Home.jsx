import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/Home.css";
import Chatbot from "../components/Chatbot";

const Home = () => {
  const navigate = useNavigate();
  const [chatOpen, setChatOpen] = useState(false); // toggle chatbot visibility

  const toggleChat = () => setChatOpen(!chatOpen);

  return (
    <div className="home-container">
      <header className="navbar">
        <h1 className="logo">E-Waste</h1>
        <nav>
          <button onClick={() => navigate("/login")}>Login</button>
          <button onClick={() => navigate("/register")}>Register</button>
        </nav>
      </header>

      <section className="hero">
        <h2>Transform E-Waste into Environmental Hope</h2>
        <p>
          Join the movement for responsible e-waste disposal and create a sustainable future.
        </p>
        <div className="hero-buttons">
          <button onClick={() => navigate("/register")}>Start Your Journey</button>
          <button onClick={() => navigate("/login")}>Sign In</button>
        </div>
      </section>

      <section className="features">
        <h3>Why Choose E-Waste?</h3>
        <div className="cards">
          <div className="card">
            <h4>Instant Scheduling</h4>
            <p>Book pickup in seconds with our intuitive platform.</p>
          </div>
          <div className="card">
            <h4>Data Security</h4>
            <p>Military-grade data wiping and secure processing.</p>
          </div>
          <div className="card">
            <h4>Eco-Friendly</h4>
            <p>100% environmentally compliant recycling process.</p>
          </div>
        </div>
      </section>

      {/* Floating WhatsApp-style chatbot button */}
      <div className="chatbot-float">
        {chatOpen && <Chatbot />}  {/* show chatbot window only when open */}
        <button className="chatbot-toggle" onClick={toggleChat}>
          {chatOpen ? "✖" : "💬"} {/* X to close, chat bubble to open */}
        </button>
      </div>
    </div>
  );
};

export default Home;
