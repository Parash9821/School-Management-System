import { useEffect, useMemo, useState } from "react";
import Navbar from "../components/Navbar";
import api from "../api/api";
import ".././styles/StudentStyle/StudentDashboard.css";
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

export default function StudentDashboard() {
  const academicYear = "2025-2026"; // later make dynamic

  const [summary, setSummary] = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      setLoading(true);
      setErr("");

      try {
        const [sRes, aRes] = await Promise.all([
          api.get(`/api/student/result-summary?academicYear=${academicYear}`),
          api.get(`/api/student/result-analytics?academicYear=${academicYear}`),
        ]);

        if (!mounted) return;

        setSummary(sRes.data);
        setAnalytics(aRes.data);
      } catch (e) {
        if (!mounted) return;
        setErr(e?.response?.data?.message || "Failed to load dashboard data");
      } finally {
        if (!mounted) return;
        setLoading(false);
      }
    };

    load();
    return () => {
      mounted = false;
    };
  }, [academicYear]);

  const subjects = analytics?.subjects || [];

  const percent = Number(summary?.percentage ?? 0);
  const totalObtained = Number(summary?.totalObtained ?? 0);
  const totalMax = Number(summary?.totalMax ?? 0);

  // ✅ PASS only if ALL subjects pass
  const overallPass = useMemo(() => {
    if (!subjects.length) return false;
    return subjects.every((s) => s.pass === true);
  }, [subjects]);

  // ✅ Overall grade becomes "NG" if failed in any subject
  const overallGrade = useMemo(() => {
    return overallPass ? (analytics?.overallGrade || "-") : "NG";
  }, [overallPass, analytics?.overallGrade]);

  // Pie 1: obtained vs remaining
  const pieData = useMemo(
    () => [
      { name: "Obtained", value: totalObtained },
      { name: "Remaining", value: Math.max(0, totalMax - totalObtained) },
    ],
    [totalObtained, totalMax]
  );

  // Pie 2: overall result (based on subject pass/fail)
  const passFail = useMemo(
    () => [
      { name: "Pass", value: overallPass ? 1 : 0 },
      { name: "Fail", value: overallPass ? 0 : 1 },
    ],
    [overallPass]
  );

  // Bar: subject percentage
  const subjectBars = useMemo(
    () =>
      subjects.map((s) => ({
        subject: s.subjectName,
        percent: Number(s.percentage ?? 0),
      })),
    [subjects]
  );

  const PIE_COLORS_1 = ["#2563eb", "#93c5fd"]; // obtained / remaining
  const PIE_COLORS_2 = ["#22c55e", "#ef4444"]; // pass / fail

  return (
    <div className="dashboard-container">
      <Navbar title="Student Dashboard" />

      <div className="dash-container">
        {err && <div className="card error-card">{err}</div>}

        {loading ? (
          <div className="card">Loading...</div>
        ) : (
          <>
            {/* TOP SUMMARY CARDS */}
            <div className="grid-3">
              <div className="card stat">
                <div className="stat-label">Academic Year</div>
                <div className="stat-value">{academicYear}</div>
              </div>

              <div className="card stat">
                <div className="stat-label">Overall Percentage</div>
                <div className="stat-value">{percent.toFixed(2)}%</div>
              </div>

              <div className="card stat">
                <div className="stat-label">Overall Grade</div>
                <div className="stat-value">{overallGrade}</div>

                <div className={`pill ${overallPass ? "pill-pass" : "pill-fail"}`}>
                  {overallPass ? "PASS" : "FAIL"}
                </div>
              </div>
            </div>

            {/* CHARTS */}
            <div className="grid-2">
              <div className="card">
                <div className="card-title">Marks Distribution</div>
                <div className="chart-box">
                  <ResponsiveContainer width="100%" height={260}>
                    <PieChart>
                      <Pie
                        data={pieData}
                        dataKey="value"
                        nameKey="name"
                        outerRadius={90}
                        label
                      >
                        {pieData.map((_, idx) => (
                          <Cell key={idx} fill={PIE_COLORS_1[idx % PIE_COLORS_1.length]} />
                        ))}
                      </Pie>
                      <Tooltip />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </div>

              <div className="card">
                <div className="card-title">Overall Result</div>
                <div className="chart-box">
                  <ResponsiveContainer width="100%" height={260}>
                    <PieChart>
                      <Pie
                        data={passFail}
                        dataKey="value"
                        nameKey="name"
                        outerRadius={90}
                        label
                      >
                        {passFail.map((_, idx) => (
                          <Cell key={idx} fill={PIE_COLORS_2[idx % PIE_COLORS_2.length]} />
                        ))}
                      </Pie>
                      <Tooltip />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>

            {/* SUBJECT BARS */}
            <div className="card">
              <div className="card-title">Subject Performance</div>
              <div className="chart-box">
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={subjectBars}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="subject" />
                    <YAxis domain={[0, 100]} />
                    <Tooltip />
                    <Legend />
                    <Bar dataKey="percent" radius={[10, 10, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* TABLE */}
            <div className="card">
              <div className="card-title">Subject Details</div>

              <div className="table-wrap">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Subject</th>
                      <th>Obtained</th>
                      <th>Max</th>
                      <th>%</th>
                      <th>Grade</th>
                      <th>Pass</th>
                    </tr>
                  </thead>
                  <tbody>
                    {subjects.map((s) => (
                      <tr key={s.subjectId}>
                        <td>{s.subjectName}</td>
                        <td>{s.obtained}</td>
                        <td>{s.max}</td>
                        <td>{Number(s.percentage ?? 0).toFixed(2)}%</td>
                        <td>{s.grade}</td>
                        <td>
                          <span className={`pill ${s.pass ? "pill-pass" : "pill-fail"}`}>
                            {s.pass ? "PASS" : "FAIL"}
                          </span>
                        </td>
                      </tr>
                    ))}

                    {!subjects.length && (
                      <tr>
                        <td colSpan="6" style={{ textAlign: "center", padding: "16px" }}>
                          No marks found yet
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
