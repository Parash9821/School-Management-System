import { createContext, useContext, useEffect, useMemo, useState } from "react";

const AuthContext = createContext(null);

function parseJwt(token) {
  try {
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem("token") || "");
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem("user");
    return raw ? JSON.parse(raw) : null;
  });

  // Keep state synced if token exists but user missing (first time)
  useEffect(() => {
    if (token && !user) {
      const payload = parseJwt(token);
      if (payload) {
        const u = {
          username: payload.sub,
          role: payload.role,     // STUDENT / TEACHER / PRINCIPAL
          userId: payload.userId,
        };
        setUser(u);
        localStorage.setItem("user", JSON.stringify(u));
      }
    }
  }, [token, user]);

  const loginWithToken = (newToken) => {
    localStorage.setItem("token", newToken);
    setToken(newToken);

    const payload = parseJwt(newToken);
    const u = payload
      ? { username: payload.sub, role: payload.role, userId: payload.userId }
      : null;

    setUser(u);
    if (u) localStorage.setItem("user", JSON.stringify(u));
    else localStorage.removeItem("user");

    return u;
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setToken("");
    setUser(null);
  };

  const value = useMemo(() => ({ token, user, loginWithToken, logout }), [token, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
