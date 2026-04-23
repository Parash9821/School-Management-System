// 📌 FILE: src/pages/teacher/TeacherAnalytics.jsx

import { useEffect, useMemo, useState } from "react";
import api from "../../api/api";
import Navbar from "../../components/Navbar";
import "../../styles/TeacherStyle/TeacherAnalytics.css"; // ✅ add this

export default function TeacherAnalytics() {
  // -------------------------
  // State
  // -------------------------
  const [assignments, setAssignments] = useState([]);
  const [classroomId, setClassroomId] = useState("");

  const [summary, setSummary] = useState(null);

  const [loadingAssignments, setLoadingAssignments] = useState(true);
  const [loadingSummary, setLoadingSummary] = useState(false);

  const [err, setErr] = useState("");
  const [msg, setMsg] = useState("");

  // -------------------------
  // Derived data
  // -------------------------
  const classrooms = useMemo(() => {
    const map = new Map();
    for (const a of assignments) {
      const cid = a?.classroomId;
      if (!cid) continue;

      if (!map.has(cid)) {
        map.set(cid, {
          id: Number(cid),
          name: a?.classroomName || `Classroom ${cid}`,
        });
      }
    }
    return Array.from(map.values()).sort((x, y) => x.id - y.id);
  }, [assignments]);

  const pass = summary?.passRate || null;
  const topRank = Array.isArray(summary?.topRank) ? summary.topRank : [];
  const subjects = Array.isArray(summary?.subjectAnalytics) ? summary.subjectAnalytics : [];

  const overallAvg = Number(summary?.overallAveragePercent || 0);
  const passPercent = Number(pass?.passPercent || 0);

  const overallBar = clampPercent(overallAvg);
  const passBar = clampPercent(passPercent);

  // -------------------------
  // API calls
  // -------------------------
  const fetchAssignments = async () => {
    setLoadingAssignments(true);
    setErr("");
    try {
      const res = await api.get("/api/teacher/assignments");
      const list = Array.isArray(res.data) ? res.data : [];
      setAssignments(list);

      // auto select first classroom if none selected
      if (!classroomId) {
        const firstCid = list.find((a) => a?.classroomId)?.classroomId;
        if (firstCid) setClassroomId(String(firstCid));
      }
    } catch (e) {
      setErr(e?.response?.data?.message || "Failed to load your assignments");
      setAssignments([]);
    } finally {
      setLoadingAssignments(false);
    }
  };

  const fetchSummary = async (cid) => {
    if (!cid) return;

    setLoadingSummary(true);
    setErr("");
    setSummary(null);

    try {
      const res = await api.get("/api/teacher/dashboard-summary", {
        params: { classroomId: Number(cid), topLimit: 5 },
      });
      setSummary(res.data || null);
    } catch (e) {
      setErr(e?.response?.data?.message || "Failed to load analytics");
      setSummary(null);
    } finally {
      setLoadingSummary(false);
    }
  };

  // -------------------------
  // Effects
  // -------------------------
  useEffect(() => {
    fetchAssignments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (!classroomId) return;
    fetchSummary(classroomId);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [classroomId]);

  // -------------------------
  // Actions
  // -------------------------
  const refresh = async () => {
    setMsg("");
    await fetchAssignments();
    if (classroomId) await fetchSummary(classroomId);

    setMsg("Refreshed ✅");
    window.clearTimeout(window.__ta_to);
    window.__ta_to = setTimeout(() => setMsg(""), 1200);
  };

  const isBusy = loadingAssignments || loadingSummary;

  // -------------------------
  // UI
  // -------------------------
  return (
    <div className="dashboard-container">
      <Navbar title="Teacher Analytics" />

      <div className="container">
        <div className="page-head">
          <div>
            <h2 className="page-title">Class Analytics</h2>
            <p className="page-subtitle">
              Select a classroom to view overall average, pass rate, ranks, and subject analytics.
            </p>
          </div>

          <button className="btn" onClick={refresh} disabled={isBusy}>
            {isBusy ? "Loading…" : "Refresh"}
          </button>
        </div>

        {err && <div className="error">{err}</div>}
        {msg && <div className="success">{msg}</div>}

        {/* 1) Select Classroom */}
        <div className="card">
          <div className="card-title">1) Select Classroom</div>

          {loadingAssignments ? (
            <div className="muted">Loading your classrooms…</div>
          ) : classrooms.length === 0 ? (
            <div className="muted">
              No classrooms found. Ask principal to create teaching assignments for you.
            </div>
          ) : (
            <div className="form">
              <label className="label">Classroom</label>
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
              <div className="muted" style={{ marginTop: 8 }}>
                Tip: If analytics are empty, make sure students are enrolled and marks exist for this classroom.
              </div>
            </div>
          )}
        </div>

        {/* 2) Summary Cards */}
        <div className="grid-2">
          {/* Overall Avg */}
          <div className="card">
            <div className="card-title">2) Overall Average</div>

            {!classroomId ? (
              <div className="muted">Select a classroom.</div>
            ) : loadingSummary ? (
              <div className="muted">Loading…</div>
            ) : !summary ? (
              <div className="muted">No data.</div>
            ) : (
              <>
                <div style={{ fontSize: 34, fontWeight: 800, marginTop: 4 }}>
                  {overallAvg.toFixed(2)}%
                </div>

                <div className="muted" style={{ marginTop: 6 }}>
                  Requested by: <b>{summary?.requestedBy || "—"}</b>
                </div>

                <ProgressBar value={overallBar} />
              </>
            )}
          </div>

          {/* Pass Rate */}
          <div className="card">
            <div className="card-title">3) Pass Rate</div>

            {!classroomId ? (
              <div className="muted">Select a classroom.</div>
            ) : loadingSummary ? (
              <div className="muted">Loading…</div>
            ) : !pass ? (
              <div className="muted">No data.</div>
            ) : (
              <>
                <div style={{ fontSize: 34, fontWeight: 800, marginTop: 4 }}>
                  {passPercent.toFixed(2)}%
                </div>

                <div className="row" style={{ gap: 14, marginTop: 10, flexWrap: "wrap" }}>
                  <div className="muted">
                    Total: <b>{Number(pass.totalStudents || 0)}</b>
                  </div>
                  <div className="muted">
                    Passed: <b>{Number(pass.passed || 0)}</b>
                  </div>
                  <div className="muted">
                    Failed: <b>{Number(pass.failed || 0)}</b>
                  </div>
                </div>

                <ProgressBar value={passBar} />
              </>
            )}
          </div>
        </div>

        {/* 4) Top Rank */}
        <div className="card">
          <div className="card-title">4) Top Rank</div>

          {!classroomId ? (
            <div className="muted">Select a classroom.</div>
          ) : loadingSummary ? (
            <div className="muted">Loading…</div>
          ) : topRank.length === 0 ? (
            <div className="muted">No rank data found.</div>
          ) : (
            <div className="table">
              <div className="table-head">
                <div>Rank</div>
                <div>Student</div>
                <div>Obtained</div>
                <div>Max</div>
                <div style={{ textAlign: "right" }}>%</div>
              </div>

              {topRank.map((r, idx) => {
                const percent = Number(r?.percentage || 0);
                return (
                  <div className="table-row" key={`${r.studentId}-${idx}`}>
                    <div className="mono">#{r?.rank ?? "—"}</div>
                    <div>
                      <div style={{ fontWeight: 700 }}>{r?.username || "—"}</div>
                      <div className="muted">ID: {r?.studentId ?? "—"}</div>
                    </div>
                    <div className="mono">{Number(r?.totalObtained || 0)}</div>
                    <div className="mono">{Number(r?.totalMax || 0)}</div>
                    <div className="mono" style={{ textAlign: "right" }}>
                      {percent.toFixed(2)}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* 5) Subject Analytics */}
        <div className="card">
          <div className="card-title">5) Subject Analytics</div>

          {!classroomId ? (
            <div className="muted">Select a classroom.</div>
          ) : loadingSummary ? (
            <div className="muted">Loading…</div>
          ) : subjects.length === 0 ? (
            <div className="muted">No subject analytics found.</div>
          ) : (
            <div className="table">
              <div className="table-head">
                <div>Subject</div>
                <div>Students</div>
                <div>Avg %</div>
                <div>High %</div>
                <div>Low %</div>
                <div style={{ textAlign: "right" }}>Visual</div>
              </div>

              {subjects.map((s, idx) => {
                const avg = Number(s?.averagePercent || 0);
                const high = Number(s?.highestPercent || 0);
                const low = Number(s?.lowestPercent || 0);
                const count = Number(s?.studentCount || 0);

                return (
                  <div className="table-row" key={`${s?.subjectId}-${idx}`}>
                    <div>
                      <div style={{ fontWeight: 700 }}>{s?.subjectName || "—"}</div>
                      <div className="muted">ID: {s?.subjectId ?? "—"}</div>
                    </div>

                    <div className="mono">{count}</div>
                    <div className="mono">{avg.toFixed(2)}</div>
                    <div className="mono">{high.toFixed(2)}</div>
                    <div className="mono">{low.toFixed(2)}</div>

                    <div style={{ textAlign: "right" }}>
                      <MiniBar value={avg} label={`${avg.toFixed(2)}%`} />
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

/* -------------------------
   Small UI helpers
------------------------- */

function clampPercent(v) {
  const n = Number(v || 0);
  if (Number.isNaN(n)) return 0;
  return Math.max(0, Math.min(100, n));
}

function ProgressBar({ value }) {
  const w = clampPercent(value);
  return (
    <div style={{ marginTop: 12 }}>
      <div className="muted" style={{ marginBottom: 6 }}>
        Visual
      </div>
      <div
        style={{
          height: 12,
          borderRadius: 10,
          background: "rgba(148,163,184,.35)",
          overflow: "hidden",
        }}
      >
        <div
          style={{
            width: `${w}%`,
            height: "100%",
            background: "rgba(59,130,246,.9)",
          }}
        />
      </div>
    </div>
  );
}

function MiniBar({ value, label }) {
  const w = clampPercent(value);
  return (
    <div
      style={{
        display: "inline-block",
        width: 140,
        height: 10,
        borderRadius: 10,
        background: "rgba(148,163,184,.35)",
        overflow: "hidden",
        verticalAlign: "middle",
      }}
      title={label}
    >
      <div
        style={{
          width: `${w}%`,
          height: "100%",
          background: "rgba(99,102,241,.9)",
        }}
      />
    </div>
  );
}
