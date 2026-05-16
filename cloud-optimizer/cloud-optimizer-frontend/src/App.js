import React, { useState } from "react";
import axios from "axios";
import "./App.css";

const API_BASE = (process.env.REACT_APP_API_URL || "").replace(/\/$/, "");

function App() {
  const [token, setToken] = useState(localStorage.getItem("token") || "");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [cpu, setCpu] = useState("");
  const [memory, setMemory] = useState("");
  const [storage, setStorage] = useState("");
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  const handleLogin = async () => {
    try {
      setError("");
      const res = await axios.post(`${API_BASE}/api/auth/login`, {
        username,
        password,
      });
      const t = res.data.token;
      localStorage.setItem("token", t);
      setToken(t);
    } catch (err) {
      setError("Login failed: " + (err.response?.data?.message || err.message));
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    setToken("");
    setResult(null);
  };

  const handleSubmit = async () => {
    try {
      setError("");
      const res = await axios.post(
        `${API_BASE}/api/optimize`,
        {
          cpuUsage: Number(cpu),
          memoryUsage: Number(memory),
          storageUsage: Number(storage),
        },
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      setResult(res.data);
    } catch (err) {
      console.log(err);
      console.log(err.response);
      setError(
        err.response?.status +
          " - " +
          JSON.stringify(err.response?.data) || err.message
      );
    }
  };

  if (!token) {
    return (
      <div className="container">
        <h1>☁️ Cloud Optimizer</h1>
        <div className="card">
          <h2>Login</h2>
          {error && <p className="error">{error}</p>}
          <input
            type="text"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <button onClick={handleLogin}>Login</button>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <h1>☁️ Cloud Optimizer</h1>
      <button className="logout-btn" onClick={handleLogout}>
        Logout
      </button>

      <div className="card">
        {error && <p className="error">{error}</p>}
        <input
          type="number"
          placeholder="CPU Usage (%)"
          value={cpu}
          onChange={(e) => setCpu(e.target.value)}
        />
        <input
          type="number"
          placeholder="Memory Usage (%)"
          value={memory}
          onChange={(e) => setMemory(e.target.value)}
        />
        <input
          type="number"
          placeholder="Storage Usage (%)"
          value={storage}
          onChange={(e) => setStorage(e.target.value)}
        />
        <button onClick={handleSubmit}>Optimize 🚀</button>
      </div>

      {result && (
        <div className="result-card">
          <h2>Result</h2>
          <p>
            <b>Recommendation:</b> {result.recommendation}
          </p>
          <p>
            <b>Severity:</b> {result.severity}
          </p>
          <p>
            <b>Cost Saving:</b> {result.estimatedCostSaving}
          </p>
        </div>
      )}
    </div>
  );
}

export default App;