import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { useMemo, useState } from "react";

export default function Navbar({ title }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const role = user?.role;

  const [open, setOpen] = useState(false);

  const menus = useMemo(
    () => ({
      STUDENT: [
        { label: "Dashboard", path: "/student", end: true },
        { label: "My Marks", path: "/student/marks" },
        { label: "Results", path: "/student/results" },
      ],
      TEACHER: [
        { label: "Dashboard", path: "/teacher", end: true },
        { label: "Assignments", path: "/teacher/assessments" },
        { label: "Enter Marks", path: "/teacher/marks" },
        { label: "Analytics", path: "/teacher/analytics" },
      ],
      PRINCIPAL: [
        { label: "Dashboard", path: "/principal", end: true },
        { label: "Users", path: "/principal/users" },
        { label: "Classrooms", path: "/principal/classrooms" },
        { label: "Subjects", path: "/principal/subjects" },
        { label: "Enrollments", path: "/principal/enrollments" },
        { label: "Assignments", path: "/principal/assignments" },
      ],
      DEPARTMENT_HEAD: [
        { label: "Dashboard", path: "/department", end: true },
        { label: "Results", path: "/department/results" },
        { label: "Analytics", path: "/department/analytics" },
      ],
    }),
    []
  );

  const items = menus[role] || [];

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  const closeMenu = () => setOpen(false);

  return (
    <header className="navbar">
      <div className="navbar-left">
        <div className="navbar-title">{title}</div>

        {/* Desktop navigation */}
        <nav className="navbar-links">
          {items.map((m) => (
            <NavLink
              key={m.path}
              to={m.path}
              end={m.end || false}
              className={({ isActive }) =>
                isActive ? "nav-link active" : "nav-link"
              }
            >
              {m.label}
            </NavLink>
          ))}
        </nav>
      </div>

      <div className="navbar-right">
        <div className="navbar-user">
          <span>{role}</span>
        </div>

        <button
          type="button"
          className="btn-logout"
          onClick={handleLogout}
        >
          Logout
        </button>

        {/* Mobile menu toggle */}
        <button
          type="button"
          className="btn-menu"
          aria-label="Open menu"
          onClick={() => setOpen((prev) => !prev)}
        >
          ☰
        </button>
      </div>

      {/* Mobile dropdown */}
      {open && (
        <div className="mobile-menu">
          {items.map((m) => (
            <NavLink
              key={m.path}
              to={m.path}
              end={m.end || false}
              onClick={closeMenu}
              className={({ isActive }) =>
                isActive ? "mobile-link active" : "mobile-link"
              }
            >
              {m.label}
            </NavLink>
          ))}
        </div>
      )}
    </header>
  );
}