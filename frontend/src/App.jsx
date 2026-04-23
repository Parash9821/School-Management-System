import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./auth/AuthContext";

import Login from "./pages/Login";
import Register from "./pages/Register"; // ✅ Added Register page
import ProtectedRoute from "./components/ProtectedRoute";

// Dashboards (existing)
import StudentDashboard from "./pages/StudentDashboard";
import TeacherDashboard from "./pages/TeacherDashboard";
import PrincipalDashboard from "./pages/PrincipalDashboard";

// New role pages
import StudentMarks from "./pages/student/StudentMarks";
import StudentResults from "./pages/student/StudentResults";

import TeacherAssessments from "./pages/teacher/TeacherAssessments";
import TeacherMarks from "./pages/teacher/TeacherMarks";
import TeacherAnalytics from "./pages/teacher/TeacherAnalytics";

import PrincipalClassrooms from "./pages/principal/PrincipalClassrooms";
import PrincipalSubjects from "./pages/principal/PrincipalSubjects";
import PrincipalEnrollments from "./pages/principal/PrincipalEnrollments";
import PrincipalAssignments from "./pages/principal/PrincipalAssignments";
import PrincipalUsers from "./pages/principal/PrincipalUsers";

function HomeRedirect() {
  const { user, token } = useAuth();

  if (!token) return <Navigate to="/login" replace />;

  if (user?.role === "STUDENT") return <Navigate to="/student" replace />;
  if (user?.role === "TEACHER") return <Navigate to="/teacher" replace />;
  if (user?.role === "DEPARTMENT_HEAD") return <Navigate to="/teacher" replace />;
  if (user?.role === "PRINCIPAL") return <Navigate to="/principal" replace />;

  return <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public */}
          <Route path="/" element={<HomeRedirect />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} /> {/* ✅ Register route */}

          {/* ===================== STUDENT ===================== */}
          <Route
            path="/student"
            element={
              <ProtectedRoute allowedRoles={["STUDENT"]}>
                <StudentDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/student/marks"
            element={
              <ProtectedRoute allowedRoles={["STUDENT"]}>
                <StudentMarks />
              </ProtectedRoute>
            }
          />
          <Route
            path="/student/results"
            element={
              <ProtectedRoute allowedRoles={["STUDENT"]}>
                <StudentResults />
              </ProtectedRoute>
            }
          />

          {/* ===================== TEACHER ===================== */}
          <Route
            path="/teacher"
            element={
              <ProtectedRoute allowedRoles={["TEACHER", "DEPARTMENT_HEAD", "PRINCIPAL"]}>
                <TeacherDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/teacher/assessments"
            element={
              <ProtectedRoute allowedRoles={["TEACHER", "DEPARTMENT_HEAD", "PRINCIPAL"]}>
                <TeacherAssessments />
              </ProtectedRoute>
            }
          />
          <Route
            path="/teacher/marks"
            element={
              <ProtectedRoute allowedRoles={["TEACHER", "DEPARTMENT_HEAD", "PRINCIPAL"]}>
                <TeacherMarks />
              </ProtectedRoute>
            }
          />
          <Route
            path="/teacher/analytics"
            element={
              <ProtectedRoute allowedRoles={["TEACHER", "DEPARTMENT_HEAD", "PRINCIPAL"]}>
                <TeacherAnalytics />
              </ProtectedRoute>
            }
          />

          {/* ===================== PRINCIPAL ===================== */}
          <Route
            path="/principal"
            element={
              <ProtectedRoute allowedRoles={["PRINCIPAL"]}>
                <PrincipalDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/principal/classrooms"
            element={
              <ProtectedRoute allowedRoles={["PRINCIPAL"]}>
                <PrincipalClassrooms />
              </ProtectedRoute>
            }
          />
          <Route
            path="/principal/subjects"
            element={
              <ProtectedRoute allowedRoles={["PRINCIPAL"]}>
                <PrincipalSubjects />
              </ProtectedRoute>
            }
          />
          <Route
            path="/principal/enrollments"
            element={
              <ProtectedRoute allowedRoles={["PRINCIPAL"]}>
                <PrincipalEnrollments />
              </ProtectedRoute>
            }
          />
          <Route
            path="/principal/assignments"
            element={
              <ProtectedRoute allowedRoles={["PRINCIPAL"]}>
                <PrincipalAssignments />
              </ProtectedRoute>
            }
          />
          <Route
            path="/principal/users"
            element={
              <ProtectedRoute allowedRoles={["PRINCIPAL"]}>
                <PrincipalUsers />
              </ProtectedRoute>
            }
          />

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}