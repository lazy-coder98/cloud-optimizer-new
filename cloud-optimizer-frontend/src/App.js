import React, { useEffect, useMemo, useState } from "react";
import axios from "axios";
import "./App.css";

const API_BASE = (process.env.REACT_APP_API_URL || "").replace(/\/$/, "");
const PAGE_SIZE = 6;
const PROVIDERS = ["AWS", "Azure", "GCP"];
const WORKLOAD_TYPES = ["Production web app", "Development/Test", "Batch job", "Database"];

function formatDate(value) {
  if (!value) return "Saved query";
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

function usageLevel(value) {
  if (value >= 80) return "high";
  if (value <= 25) return "low";
  return "steady";
}

function metricTone(value) {
  const numericValue = Number(value);
  if (!Number.isFinite(numericValue)) return "empty";
  return usageLevel(numericValue);
}

function MetricInput({ label, value, onChange }) {
  const barValue = Math.min(Math.max(Number(value) || 0, 0), 100);
  const tone = metricTone(value);

  return (
    <label className={`metric-card ${tone}`}>
      <span>{label}</span>
      <input
        type="number"
        min="0"
        max="100"
        placeholder="0-100"
        value={value}
        onChange={onChange}
        required
      />
      <span className="meter">
        <span style={{ width: `${barValue}%` }} />
      </span>
    </label>
  );
}

function App() {
  const [token, setToken] = useState(localStorage.getItem("token") || "");
  const [authMode, setAuthMode] = useState("login");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [cpu, setCpu] = useState("");
  const [memory, setMemory] = useState("");
  const [storage, setStorage] = useState("");
  const [provider, setProvider] = useState("AWS");
  const [workloadType, setWorkloadType] = useState("Production web app");
  const [monthlyCost, setMonthlyCost] = useState("");
  const [result, setResult] = useState(null);
  const [history, setHistory] = useState([]);
  const [historyPage, setHistoryPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [aiQuestion, setAiQuestion] = useState("");
  const [aiAdvice, setAiAdvice] = useState("");
  const [aiLoading, setAiLoading] = useState(false);
  const [error, setError] = useState("");

  const authHeaders = useMemo(
    () => ({ headers: { Authorization: `Bearer ${token}` } }),
    [token]
  );

  const fetchHistory = async (page = historyPage) => {
    if (!token) return;

    try {
      setHistoryLoading(true);
      setError("");
      const res = await axios.get(
        `${API_BASE}/api/optimize/history?page=${page}&size=${PAGE_SIZE}`,
        authHeaders
      );

      setHistory(res.data.content || []);
      setHistoryPage(res.data.number || 0);
      setTotalPages(res.data.totalPages || 0);
    } catch (err) {
      setError(
        "Could not load previous queries: " +
          (err.response?.data?.message || err.message)
      );
    } finally {
      setHistoryLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  const handleAuth = async () => {
    try {
      setError("");
      const endpoint = authMode === "signup" ? "signup" : "login";
      const res = await axios.post(`${API_BASE}/api/auth/${endpoint}`, {
        username,
        password,
      });
      const nextToken = res.data.token;
      localStorage.setItem("token", nextToken);
      setToken(nextToken);
      setUsername("");
      setPassword("");
    } catch (err) {
      setError(
        `${authMode === "signup" ? "Signup" : "Login"} failed: ` +
          (err.response?.data?.message || err.message)
      );
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    setToken("");
    setResult(null);
    setAiAdvice("");
    setAiQuestion("");
    setHistory([]);
    setHistoryPage(0);
    setTotalPages(0);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      setLoading(true);
      setError("");
      const res = await axios.post(
        `${API_BASE}/api/optimize`,
        {
          cpuUsage: Number(cpu),
          memoryUsage: Number(memory),
          storageUsage: Number(storage),
          provider,
          workloadType,
          monthlyCost: Number(monthlyCost) || 0,
        },
        authHeaders
      );
      setResult(res.data);
      setAiAdvice("");
      setAiQuestion("");
      await fetchHistory(0);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data?.error ||
          err.message ||
          "Optimization failed"
      );
    } finally {
      setLoading(false);
    }
  };

  const loadHistoryItem = (item) => {
    setCpu(item.cpuUsage);
    setMemory(item.memoryUsage);
    setStorage(item.storageUsage);
    setProvider(item.provider || "AWS");
    setWorkloadType(item.workloadType || "Production web app");
    setMonthlyCost(item.monthlyCost || "");
    setResult({
      recommendation: item.recommendation,
      severity: item.severity,
      estimatedCostSaving: item.estimatedCostSaving,
      estimatedMonthlySavingAmount: item.estimatedMonthlySavingAmount,
      rationale: item.rationale,
    });
    setAiAdvice("");
    setAiQuestion("");
  };

  const handleAiAdvice = async () => {
    if (!result) return;

    try {
      setAiLoading(true);
      setError("");
      const res = await axios.post(
        `${API_BASE}/api/ai/advice`,
        {
          cpuUsage: Number(cpu),
          memoryUsage: Number(memory),
          storageUsage: Number(storage),
          provider,
          workloadType,
          monthlyCost: Number(monthlyCost) || 0,
          recommendation: result.recommendation,
          severity: result.severity,
          estimatedCostSaving: result.estimatedCostSaving,
          estimatedMonthlySavingAmount: result.estimatedMonthlySavingAmount,
          rationale: result.rationale,
          question: aiQuestion,
        },
        authHeaders
      );
      setAiAdvice(res.data.advice || "");
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data?.error ||
          err.message ||
          "AI advisor failed"
      );
    } finally {
      setAiLoading(false);
    }
  };

  if (!token) {
    return (
      <main className="auth-shell">
        <section className="auth-panel">
          <div className="auth-copy">
            <p className="eyebrow">Cloud cost workspace</p>
            <h1>Cloud Optimizer</h1>
            <p className="lede">
              Analyze resource pressure, review past runs, and keep cost
              decisions moving from one clean workspace.
            </p>
          </div>

          <div className="auth-card-wrap">
            <div className="login-card">
              <div className="auth-card-header">
                <h2>{authMode === "signup" ? "Create account" : "Sign in"}</h2>
                <button
                  className="text-button"
                  type="button"
                  onClick={() => {
                    setError("");
                    setAuthMode(authMode === "signup" ? "login" : "signup");
                  }}
                >
                  {authMode === "signup" ? "Sign in" : "Sign up"}
                </button>
              </div>
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
              <button onClick={handleAuth}>
                {authMode === "signup" ? "Create account" : "Login"}
              </button>
            </div>
          </div>
        </section>
      </main>
    );
  }

  return (
    <main className="app-shell">
      <header className="hero-bar">
        <div className="topbar">
          <div>
            <p className="eyebrow">Cloud cost workspace</p>
            <h1>Cloud Optimizer</h1>
          </div>
          <button className="ghost-button light" onClick={handleLogout}>
            Logout
          </button>
        </div>

        <div className="summary-strip">
          <div>
            <span>{history.length}</span>
            <p>Loaded queries</p>
          </div>
          <div>
            <span>{result?.severity || "Ready"}</span>
            <p>Current status</p>
          </div>
          <div>
            <span>{totalPages || 1}</span>
            <p>History pages</p>
          </div>
        </div>
      </header>

      <section className="dashboard-grid">
        <form className="optimizer-panel" onSubmit={handleSubmit}>
          <div className="panel-heading">
            <div>
              <p className="eyebrow">New query</p>
              <h2>Resource profile</h2>
            </div>
            <button className="primary-button" disabled={loading}>
              {loading ? "Analyzing..." : "Optimize"}
            </button>
          </div>

          {error && <p className="error banner">{error}</p>}

          <div className="metric-grid">
            <MetricInput
              label="CPU Usage"
              value={cpu}
              onChange={(e) => setCpu(e.target.value)}
            />
            <MetricInput
              label="Memory Usage"
              value={memory}
              onChange={(e) => setMemory(e.target.value)}
            />
            <MetricInput
              label="Storage Usage"
              value={storage}
              onChange={(e) => setStorage(e.target.value)}
            />
          </div>

          <div className="context-grid">
            <label className="context-field">
              <span>Provider</span>
              <select value={provider} onChange={(e) => setProvider(e.target.value)}>
                {PROVIDERS.map((item) => (
                  <option key={item} value={item}>
                    {item}
                  </option>
                ))}
              </select>
            </label>
            <label className="context-field">
              <span>Workload</span>
              <select
                value={workloadType}
                onChange={(e) => setWorkloadType(e.target.value)}
              >
                {WORKLOAD_TYPES.map((item) => (
                  <option key={item} value={item}>
                    {item}
                  </option>
                ))}
              </select>
            </label>
            <label className="context-field">
              <span>Monthly cost</span>
              <input
                type="number"
                min="0"
                placeholder="0"
                value={monthlyCost}
                onChange={(e) => setMonthlyCost(e.target.value)}
              />
            </label>
          </div>

          {result ? (
            <>
            <div className={`result-panel ${result.severity?.toLowerCase()}`}>
              <div>
                <span className={`severity ${result.severity?.toLowerCase()}`}>
                  {result.severity}
                </span>
                <h3>{result.recommendation}</h3>
                {result.rationale && <p className="rationale">{result.rationale}</p>}
              </div>
              <p>
                Estimated monthly saving
                <strong>{result.estimatedCostSaving}%</strong>
                {Number(result.estimatedMonthlySavingAmount) > 0 && (
                  <span>${result.estimatedMonthlySavingAmount}</span>
                )}
              </p>
            </div>
            <div className="ai-panel">
              <div className="panel-heading compact">
                <div>
                  <p className="eyebrow">Gemini advisor</p>
                  <h2>AI Cost Advisor</h2>
                </div>
                <button
                  className="ghost-button"
                  type="button"
                  onClick={handleAiAdvice}
                  disabled={aiLoading}
                >
                  {aiLoading ? "Thinking..." : "Ask Gemini"}
                </button>
              </div>
              <textarea
                value={aiQuestion}
                onChange={(event) => setAiQuestion(event.target.value)}
                placeholder="Ask about this result, or leave blank for an action plan."
              />
              {aiAdvice && <div className="ai-answer">{aiAdvice}</div>}
            </div>
            </>
          ) : (
            <div className="placeholder-panel">
              <p className="eyebrow">Awaiting analysis</p>
              <h3>Enter usage values to generate a recommendation.</h3>
            </div>
          )}
        </form>

        <aside className="history-panel">
          <div className="panel-heading compact">
            <div>
              <p className="eyebrow">MongoDB history</p>
              <h2>Previous queries</h2>
            </div>
            <button
              className="ghost-button"
              type="button"
              onClick={() => fetchHistory(historyPage)}
              disabled={historyLoading}
            >
              {historyLoading ? "Refreshing" : "Refresh"}
            </button>
          </div>

          <div className="history-list">
            {!historyLoading && history.length === 0 && (
              <div className="empty-state">
                <h3>No saved queries yet</h3>
                <p>Run an optimization and it will appear here.</p>
              </div>
            )}

            {history.map((item) => (
              <button
                className="history-item"
                key={item.id || item.createdAt}
                type="button"
                onClick={() => loadHistoryItem(item)}
              >
                <div className="history-item-header">
                  <span className={`severity ${item.severity?.toLowerCase()}`}>
                    {item.severity}
                  </span>
                  <span>{formatDate(item.createdAt)}</span>
                </div>
                <p>{item.recommendation}</p>
                <div className="usage-row">
                  <span>{item.provider || "AWS"}</span>
                  <span>{item.workloadType || "Workload"}</span>
                  {Number(item.monthlyCost) > 0 && (
                    <span>${item.monthlyCost}/mo</span>
                  )}
                  <span className={usageLevel(item.cpuUsage)}>
                    CPU {item.cpuUsage}%
                  </span>
                  <span className={usageLevel(item.memoryUsage)}>
                    Memory {item.memoryUsage}%
                  </span>
                  <span className={usageLevel(item.storageUsage)}>
                    Storage {item.storageUsage}%
                  </span>
                </div>
              </button>
            ))}
          </div>

          {totalPages > 1 && (
            <div className="pagination">
              <button
                type="button"
                disabled={historyPage === 0 || historyLoading}
                onClick={() => fetchHistory(historyPage - 1)}
              >
                Previous
              </button>
              <span>
                Page {historyPage + 1} of {totalPages}
              </span>
              <button
                type="button"
                disabled={historyPage + 1 >= totalPages || historyLoading}
                onClick={() => fetchHistory(historyPage + 1)}
              >
                Next
              </button>
            </div>
          )}
        </aside>
      </section>
    </main>
  );
}

export default App;
