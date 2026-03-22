import React, { useState } from "react";
import "../styles/Chatbot.css";

export default function Chatbot() {
  const [messages, setMessages] = useState([
    { sender: "bot", text: "Hello! 👋 I am your E-Waste Assistant. How can I help you today?" }
  ]);
  const [input, setInput] = useState("");

  // Predefined suggestions
  const suggestions = [
    "pickup",
    "status 5",
    "what items do you accept?",
    "what time do you work?",
    "contact details",
    "why recycle?",
    "create_pickup"
  ];

  // Handle sending message to backend
  const sendMessage = async (customMessage = null) => {
    const messageToSend = customMessage || input;
    if (!messageToSend.trim()) return;

    // Add user message
    const newMessages = [...messages, { sender: "user", text: messageToSend }];
    setMessages(newMessages);

    try {
      const response = await fetch("http://localhost:8084/api/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: messageToSend })
      });

      const data = await response.json();

      setMessages([...newMessages, { sender: "bot", text: data.response }]);
    } catch (error) {
      setMessages([
        ...newMessages,
        { sender: "bot", text: "⚠️ Error: Could not connect to server." }
      ]);
    }

    setInput("");
  };

  // Handle Enter key
  const handleKeyDown = (e) => {
    if (e.key === "Enter") {
      sendMessage();
    }
  };

  return (
    <div className="chatbot-container">
      <div className="chatbot-header">💻 E-Waste Chatbot</div>

      {/* Chat messages */}
      <div className="chatbot-messages">
        {messages.map((msg, index) => (
          <div
            key={index}
            className={`chat-message ${
              msg.sender === "user" ? "user-message" : "bot-message"
            }`}
          >
            {msg.text}
          </div>
        ))}
      </div>

      {/* Suggestions row */}
      <div className="chatbot-suggestions">
        {suggestions.map((s, i) => (
          <button key={i} onClick={() => sendMessage(s)} className="suggestion-btn">
            {s}
          </button>
        ))}
      </div>

      {/* Input box */}
      <div className="chatbot-input">
        <input
          type="text"
          placeholder="Ask me something..."
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
        />
        <button onClick={() => sendMessage()}>Send</button>
      </div>
    </div>
  );
}
