import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/api";
import { useAuth } from "../auth/AuthContext";

export default function Login() {
  const [username, setUsername] = useState("teacher");
  const [password, setPassword] = useState("123456");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const { loginWithToken } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const res = await api.post("/api/auth/login", { username, password });

      const token = res.data?.token;
      if (!token) {
        setError("Token not received from backend.");
        return;
      }

      const u = loginWithToken(token);

      if (u.role === "STUDENT") navigate("/student", { replace: true });
      else if (u.role === "TEACHER") navigate("/teacher", { replace: true });
      else if (u.role === "PRINCIPAL") navigate("/principal", { replace: true });
      else navigate("/teacher", { replace: true });

    } catch (err) {
      setError(err?.response?.data?.message || "Invalid username or password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">

      <div className="auth-wrapper">

        <div className="auth-info">
          <h1>School Management System</h1>
          <p>
            A modern platform to manage students, teachers, subjects,
            assessments, and results in one place.
          </p>

          <ul>
            <li>📚 Student & Teacher Dashboard</li>
            <li>📊 Result Analytics</li>
            <li>📝 Assessment Management</li>
            <li>🏫 Classroom & Subject Control</li>
          </ul>
        </div>

        <div className="auth-card">
          <h2>Login</h2>
          <p className="sub">Sign in to continue</p>

          <form onSubmit={handleSubmit}>
            <input
              type="text"
              placeholder="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />

            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />

            {error && <p className="error">{error}</p>}

            <button type="submit" disabled={loading}>
              {loading ? "Signing in..." : "Login"}
            </button>
          </form>
        </div>

      </div>

    </div>
  );
}