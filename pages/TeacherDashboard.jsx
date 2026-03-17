import { useEffect, useMemo, useState } from "react";
import Navbar from "../components/Navbar";
import api from "../api/api";
import ".././styles/TeacherStyle/TeacherDashboard.css";

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

const COLORS = {
  passed: "#22c55e",
  failed: "#ef4444",
  avg: "#3b82f6",
  high: "#22c55e",
  low: "#ef4444",
};

export default function TeacherDashboard() {
  const [assignments, setAssignments] = useState([]);

  const classrooms = useMemo(() => {
    const map = new Map();
    for (const a of assignments) {
      if (!a?.classroomId) continue;
      if (!map.has(a.classroomId)) {
        map.set(a.classroomId, {
          id: a.classroomId,
          name: a.classroomName || `Classroom ${a.classroomId}`,
        });
      }
    }
    return Array.from(map.values()).sort((x, y) => x.id - y.id);
  }, [assignments]);

  const [classroomId, setClassroomId] = useState("");
  const topRankLimit = 3;

  const [data, setData] = useState(null);
  const [err, setErr] = useState("");
  const [loadingAssignments, setLoadingAssignments] = useState(true);
  const [loadingDash, setLoadingDash] = useState(false);

  const fetchAssignments = async () => {
    setLoadingAssignments(true);
    setErr("");
    try {
      const res = await api.get("/api/teacher/assignments");
      const list = Array.isArray(res.data) ? res.data : [];
      setAssignments(list);

      if (!classroomId) {
        const first = list.find((a) => a?.classroomId)?.classroomId;
        if (first) setClassroomId(String(first));
      }
    } catch (e) {
      setErr(e?.response?.data?.message || "Failed to load assignments");
      setAssignments([]);
    } finally {
      setLoadingAssignments(false);
    }
  };

  const fetchDashboard = async (cid) => {
    if (!cid) return;
    setLoadingDash(true);
    setErr("");
    try {
      const res = await api.get("/api/teacher/dashboard-summary", {
        params: { classroomId: Number(cid), topLimit: topRankLimit },
      });
      setData(res.data || null);
    } catch (e) {
      setErr(e?.response?.data?.message || "Failed to load teacher dashboard");
      setData(null);
    } finally {
      setLoadingDash(false);
    }
  };

  useEffect(() => {
    fetchAssignments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (!classroomId) return;
    fetchDashboard(classroomId);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [classroomId]);

  const passed = Number(data?.passRate?.passed || 0);
  const failed = Number(data?.passRate?.failed || 0);

  const passFail = useMemo(
    () => [
      { name: "Passed", value: passed, key: "passed" },
      { name: "Failed", value: failed, key: "failed" },
    ],
    [passed, failed]
  );

  const subjectBars = useMemo(() => {
    const list = Array.isArray(data?.subjectAnalytics) ? data.subjectAnalytics : [];
    return list.map((s) => ({
      subject: s.subjectName,
      avg: Number(s.averagePercent || 0),
      high: Number(s.highestPercent || 0),
      low: Number(s.lowestPercent || 0),
      studentCount: Number(s.studentCount || 0),
    }));
  }, [data]);

  const loading = loadingAssignments || loadingDash;

  if (loading) {
    return (
      <div className="dashboard-page">
        <Navbar title="Teacher Dashboard" />
        <div className="dash-container">
          <div className="card">Loading...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <Navbar title="Teacher Dashboard" />

      <div className="dash-container">
        {err && <div className="card error-card">{err}</div>}

        {/* Classroom Selector */}
        <div className="card">
          <div className="card-head">
            <div>
              <div className="card-title">Select Classroom</div>
              <div className="muted">Choose a classroom to view performance & analytics.</div>
            </div>
          </div>

          {classrooms.length === 0 ? (
            <div className="muted">No classrooms found in your assignments.</div>
          ) : (
            <select
              className="input"
              value={classroomId}
              onChange={(e) => setClassroomId(e.target.value)}
            >
              <option value="">— Select —</option>
              {classrooms.map((c) => (
                <option key={c.id} value={c.id}>
                  #{c.id} • {c.name}
                </option>
              ))}
            </select>
          )}
        </div>

        {/* Summary cards */}
        <div className="grid-3">
          <div className="card stat">
            <div className="stat-label">Classroom</div>
            <div className="stat-value">#{data?.classroomId ?? classroomId}</div>
            <div className="muted">Year: {data?.academicYear ?? "N/A"}</div>
          </div>

          <div className="card stat">
            <div className="stat-label">Overall Average</div>
            <div className="stat-value">
              {Number(data?.overallAveragePercent || 0).toFixed(2)}%
            </div>
          </div>

          <div className="card stat">
            <div className="stat-label">Pass Rate</div>
            <div className="stat-value">
              {Number(data?.passRate?.passPercent || 0).toFixed(2)}%
            </div>
            <div className="muted">
              <span className="pill pass">Passed: {passed}</span>
              <span className="pill fail" style={{ marginLeft: 8 }}>
                Failed: {failed}
              </span>
            </div>
          </div>
        </div>

        {/* Charts + Top rank */}
        <div className="grid-2">
          <div className="card">
            <div className="card-title">Pass vs Fail</div>
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
                    {passFail.map((entry) => (
                      <Cell
                        key={entry.key}
                        fill={entry.key === "passed" ? COLORS.passed : COLORS.failed}
                      />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="legend-hint">
              <span className="dot" style={{ background: COLORS.passed }} /> Passed
              <span className="dot" style={{ background: COLORS.failed, marginLeft: 12 }} /> Failed
            </div>
          </div>

          <div className="card">
            <div className="card-title">Top Rankers (Top {topRankLimit})</div>
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Rank</th>
                    <th>Student</th>
                    <th>%</th>
                    <th>Obtained</th>
                    <th>Max</th>
                  </tr>
                </thead>
                <tbody>
                  {(Array.isArray(data?.topRank) ? data.topRank : []).map((r, idx) => (
                    <tr key={`${r?.studentId ?? "x"}-${idx}`}>
                      <td>
                        <span className="pill pass">#{r?.rank ?? "—"}</span>
                      </td>
                      <td>{r?.username || "—"}</td>
                      <td>{Number(r?.percentage || 0).toFixed(2)}%</td>
                      <td>{Number(r?.totalObtained || 0)}</td>
                      <td>{Number(r?.totalMax || 0)}</td>
                    </tr>
                  ))}

                  {!data?.topRank?.length && (
                    <tr>
                      <td colSpan="5" className="table-empty">
                        No ranking data
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Subject analytics */}
        <div className="card">
          <div className="card-title" >Subject Analytics</div>
          <div className="chart-box">
            <ResponsiveContainer width="100%" height={320}>
              <BarChart data={subjectBars}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="subject" />
                <YAxis domain={[0, 100]} />
                <Tooltip />
                <Legend />
                <Bar dataKey="avg" fill={COLORS.avg} name="Avg %" />
                <Bar dataKey="high" fill={COLORS.high} name="High %" />
                <Bar dataKey="low" fill={COLORS.low} name="Low %" />
              </BarChart>
            </ResponsiveContainer>
          </div>

          {!subjectBars.length && <div className="muted">No subject analytics yet.</div>}
        </div>

        {/* Subject table */}
        <div className="card">
          <div className="card-title">Subject Detail Table</div>
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Subject</th>
                  <th>Students</th>
                  <th>Avg %</th>
                  <th>High %</th>
                  <th>Low %</th>
                </tr>
              </thead>
              <tbody>
                {(Array.isArray(data?.subjectAnalytics) ? data.subjectAnalytics : []).map((s, idx) => (
                  <tr key={`${s?.subjectId ?? "x"}-${idx}`}>
                    <td>{s?.subjectName || "—"}</td>
                    <td>{Number(s?.studentCount || 0)}</td>
                    <td>{Number(s?.averagePercent || 0).toFixed(2)}%</td>
                    <td>{Number(s?.highestPercent || 0).toFixed(2)}%</td>
                    <td>{Number(s?.lowestPercent || 0).toFixed(2)}%</td>
                  </tr>
                ))}

                {!data?.subjectAnalytics?.length && (
                  <tr>
                    <td colSpan="5" className="table-empty">
                      No analytics yet
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

      </div>
    </div>
  );
}
