import { useEffect, useMemo, useState } from "react";
import Navbar from "../../components/Navbar";
import api from "../../api/api";
import "../../styles/StudentStyle/StudentMarks.css"; // ✅ add this file

export default function StudentMarks() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");

  const loadResult = async () => {
    setLoading(true);
    setErr("");

    try {
      // If backend requires academicYear later, add ?academicYear=...
      const res = await api.get("/api/student/results");
      setData(res.data);
    } catch (e) {
      setErr(e?.response?.data?.message || "Failed to load result");
      setData(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadResult();
  }, []);

  const subjects = data?.subjects || [];

  // ✅ rule: overall pass only if all subjects pass
  const overallPass = useMemo(() => {
    if (!subjects.length) return false;
    return subjects.every((s) => s.pass === true);
  }, [subjects]);

  // ✅ rule: if failed → grade must be NG
  const overallGrade = useMemo(() => {
    if (!data) return "-";
    return overallPass ? (data.overallGrade || "-") : "NG";
  }, [data, overallPass]);

  const overallPercentage = useMemo(() => {
    if (!data) return 0;
    return Number(data.overallPercentage ?? 0);
  }, [data]);

  return (
    <div className="dashboard-container">
      <Navbar title="Student Result" />

      <div className="sm-container">
        <div className="sm-head">
          <div>
            <div className="sm-title">My Marks</div>
            <div className="sm-subtitle">View your marks and subject-wise performance</div>
          </div>

          <button className="sm-btn" onClick={loadResult} disabled={loading}>
            {loading ? "Refreshing..." : "Refresh"}
          </button>
        </div>

        {err && <div className="sm-error">{err}</div>}

        <div className="sm-card">
          {loading ? (
            <div className="sm-muted">Loading result…</div>
          ) : !data ? (
            <div className="sm-muted">No result available.</div>
          ) : (
            <>
              {/* STUDENT SUMMARY */}
              <div className="sm-summary">
                <div>
                  <span className="sm-label">Name:</span>{" "}
                  <span className="sm-value">
                    {data.fullName} ({data.username})
                  </span>
                </div>
                <div>
                  <span className="sm-label">Classroom:</span>{" "}
                  <span className="sm-value">{data.classroomName}</span>
                </div>
                <div>
                  <span className="sm-label">Academic Year:</span>{" "}
                  <span className="sm-value">{data.academicYear}</span>
                </div>
              </div>

              {/* OVERALL RESULT */}
              <div className="sm-card sm-overall">
                <div className="sm-card-title">Overall Result</div>

                <div className="sm-grid-3">
                  <div className="sm-metric">
                    <div className="sm-muted">Percentage</div>
                    <div className="sm-metric-value">{overallPercentage.toFixed(2)}%</div>
                  </div>

                  <div className="sm-metric">
                    <div className="sm-muted">Grade</div>
                    {/* ✅ NG if failed */}
                    <div className="sm-metric-value">{overallGrade}</div>
                  </div>

                  <div className="sm-metric">
                    <div className="sm-muted">Status</div>
                    <span className={`sm-pill ${overallPass ? "sm-pill-pass" : "sm-pill-fail"}`}>
                      {overallPass ? "PASS" : "FAIL"}
                    </span>
                  </div>
                </div>

                {/* small info note */}
                {!overallPass && subjects.length > 0 && (
                  <div className="sm-note">
                    Note: If you fail in any one subject, overall grade becomes <b>NG</b>.
                  </div>
                )}
              </div>

              {/* SUBJECT-WISE */}
              <div className="sm-card">
                <div className="sm-card-title">Subject-wise Marks</div>

                {!subjects.length ? (
                  <div className="sm-muted">No subject marks found.</div>
                ) : (
                  <div className="sm-table-wrap">
                    <table className="sm-table">
                      <thead>
                        <tr>
                          <th>Subject</th>
                          <th>Obtained</th>
                          <th>Max</th>
                          <th>%</th>
                          <th>Grade</th>
                          <th>Status</th>
                        </tr>
                      </thead>

                      <tbody>
                        {subjects.map((s) => (
                          <tr key={s.subjectId}>
                            <td className="sm-strong">{s.subjectName}</td>
                            <td className="sm-mono">{s.obtained}</td>
                            <td className="sm-mono">{s.max}</td>
                            <td className="sm-mono">{Number(s.percentage ?? 0).toFixed(2)}%</td>
                            <td className="sm-mono">{s.grade}</td>
                            <td>
                              <span className={`sm-pill ${s.pass ? "sm-pill-pass" : "sm-pill-fail"}`}>
                                {s.pass ? "PASS" : "FAIL"}
                              </span>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
