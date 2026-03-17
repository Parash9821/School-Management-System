import { useEffect, useMemo, useState } from "react";
import Navbar from "../components/Navbar";
import api from "../api/api";
import { useAuth } from "../auth/AuthContext";
import ".././styles/PrincipalStyle/principaldashboard.css";

import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
} from "recharts";

export default function PrincipalDashboard() {
  const { user } = useAuth();

  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");

  // counts
  const [classroomsCount, setClassroomsCount] = useState(0);
  const [subjectsCount, setSubjectsCount] = useState(0);
  const [studentsCount, setStudentsCount] = useState(0);
  const [teachersCount, setTeachersCount] = useState(0);

  // endpoints
  const endpoints = {
    classrooms: "/api/principal/classrooms",
    subjects: "/api/principal/subjects",
    usersByRole: (role) => `/api/principal/users?role=${role}`,
  };

  useEffect(() => {
    let isMounted = true;

    const load = async () => {
      setLoading(true);
      setErr("");

      try {
        const results = await Promise.allSettled([
          api.get(endpoints.classrooms),
          api.get(endpoints.subjects),
          api.get(endpoints.usersByRole("STUDENT")),
          api.get(endpoints.usersByRole("TEACHER")),
        ]);

        const getLen = (res) =>
          res.status === "fulfilled" && Array.isArray(res.value.data)
            ? res.value.data.length
            : 0;

        if (!isMounted) return;

        setClassroomsCount(getLen(results[0]));
        setSubjectsCount(getLen(results[1]));
        setStudentsCount(getLen(results[2]));
        setTeachersCount(getLen(results[3]));
      } catch (e) {
        if (!isMounted) return;
        setErr(e?.response?.data?.message || "Failed to load principal dashboard");
      } finally {
        if (!isMounted) return;
        setLoading(false);
      }
    };

    load();
    return () => {
      isMounted = false;
    };
  }, []);

  const totalUsers = studentsCount + teachersCount + 1; // + principal

  // Pie data (Dept Heads removed)
  const usersPie = useMemo(() => {
    const data = [
      { name: "Students", value: studentsCount },
      { name: "Teachers", value: teachersCount },
      { name: "Principal", value: 1 },
    ];
    // remove 0 values so legend doesn't show empty slices
    return data.filter((d) => d.value > 0);
  }, [studentsCount, teachersCount]);

  // Bar data
  const overviewBars = useMemo(
    () => [
      { name: "Classrooms", value: classroomsCount },
      { name: "Subjects", value: subjectsCount },
      { name: "Students", value: studentsCount },
      { name: "Teachers", value: teachersCount },
    ],
    [classroomsCount, subjectsCount, studentsCount, teachersCount]
  );

  // colors
  const PIE_COLORS = ["#2563eb", "#60a5fa", "#93c5fd"];

  // Pie label (percentage)
  const renderPieLabel = ({ percent }) => {
    if (percent <= 0) return "";
    const p = Math.round(percent * 100);
    // hide tiny labels for clean look
    return p >= 5 ? `${p}%` : "";
  };

  return (
    <div className="dashboard-container">
      <Navbar title="Principal Dashboard" />

      <div className="dashboard-header">
        <div>
          <div className="dashboard-title">School Overview</div>
          <div className="dashboard-subtitle">
            Logged in as <b>{user?.username}</b> ({user?.role})
          </div>
        </div>
      </div>

      {err && <div className="alert-error">{err}</div>}

      {/* Top stats */}
      <div className="grid-stats">
        <StatCard
          title="Total Users"
          value={totalUsers}
          hint="Students + Teachers + Principal"
          accent="#2563eb"
        />

        <StatCard
          title="Students"
          value={studentsCount}
          hint="Role = STUDENT"
          accent="#60a5fa"
        />

        <StatCard
          title="Teachers"
          value={teachersCount}
          hint="Role = TEACHER"
          accent="#93c5fd"
        />

        <StatCard
          title="Classrooms"
          value={classroomsCount}
          hint="Classrooms created in this school"
          accent="#1e3a8a"
        />
      </div>


      <div className="grid-charts">
        {/* Pie */}
        <div className="card chart-card">
          <div className="card-title">User Distribution</div>
          <div className="card-subtitle">Students vs Teachers vs Principal</div>

          <div className="chart-wrap chart-wrap--pie">
            {loading ? (
              <div className="skeleton">Loading chart...</div>
            ) : usersPie.length === 0 ? (
              <div className="chart-empty">No user data available.</div>
            ) : (
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={usersPie}
                    dataKey="value"
                    nameKey="name"
                    cx="50%"
                    cy="45%"
                    innerRadius={60}
                    outerRadius={95}
                    paddingAngle={2}
                    labelLine={false}
                    label={renderPieLabel}
                    isAnimationActive={true}
                  >
                    {usersPie.map((_, idx) => (
                      <Cell key={idx} fill={PIE_COLORS[idx % PIE_COLORS.length]} />
                    ))}
                  </Pie>

                  <Tooltip
                    formatter={(value, name) => [value, name]}
                    contentStyle={{ borderRadius: 12, border: "1px solid #e2e8f0" }}
                  />

                  {/* Put legend at bottom so it never overlaps the pie */}
                  <Legend
                    verticalAlign="bottom"
                    align="center"
                    height={50}
                    wrapperStyle={{ paddingTop: 8 }}
                  />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>

        {/* Bar */}
        <div className="card chart-card">
          <div className="card-title">School Entities</div>
          <div className="card-subtitle">Quick count summary</div>

          <div className="chart-wrap">
            {loading ? (
              <div className="skeleton">Loading chart...</div>
            ) : (
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={overviewBars} margin={{ top: 10, right: 18, left: 0, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" tickMargin={8} />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="value" radius={[10, 10, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>
      </div>

      {/* Footer section */}

    </div>
  );
}

function StatCard({ title, value, hint }) {
  return (
    <div className="card stat-card">
      <div className="stat-top">
        <div className="stat-title">{title}</div>
        <div className="stat-value">{value}</div>
      </div>
      <div className="stat-hint">{hint}</div>
    </div>
  );
}
