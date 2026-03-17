import { Navigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function ProtectedRoute({ allowedRoles, children }) {
  const { token, user } = useAuth();

  // Not logged in
  if (!token) return <Navigate to="/login" replace />;

  // Wait until user is ready (prevents redirect race)
  if (!user) return null;

  // Role check
  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/login" replace />;
  }

  return children;
}
