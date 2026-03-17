import { useEffect, useMemo, useState } from "react";
import Navbar from "../../components/Navbar";
import api from "../../api/api";
import "../../styles/StudentStyle/StudentResult.css"; // ✅ add this

export default function StudentResults() {
  const [data, setData] = useState(null);
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(true);

  const academicYear = "2025-2026"; // TEMP (you can make this dynamic later)

  const load = async () => {
    setLoading(true);
    setErr("");

    try {
      const res = await api.get("/api/student/results", {
        params: { academicYear },
      });
      setData(res.data);
    } catch (e) {
      setData(null);
      setErr(e?.response?.data?.message || "Failed to load results");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const subjects = data?.subjects || [];

  // ✅ Overall PASS only if ALL subjects pass
  const overallPass = useMemo(() => {
    if (!subjects.length) return false;
    return subjects.every((s) => s.pass === true);
  }, [subjects]);

  // ✅ Overall Grade becomes NG if any subject failed
  const overallGrade = useMemo(() => {
    if (!data) return "-";
    return overallPass ? (data.overallGrade || "-") : "NG";
  }, [data, overallPass]);

  const overallPercentage = useMemo(() => {
    if (!data) return 0;
    return Number(data.overallPercentage ?? 0);
  }, [data]);

  const publishedText = useMemo(() => {
    if (!data?.publishedAt) return "—";
    return String(data.publishedAt).replace("T", " ");
  }, [data]);

  return (
    <div className="sr-page">
      <Navbar title="Student Results" />

      <div className="sr-container">
        <div className="sr-head">
          <div>
            <div className="sr-title">My Results</div>
            <div className="sr-subtitle">Academic Year: {academicYear}</div>
          </div>

          <button className="sr-btn" onClick={load} disabled={loading}>
            {loading ? "Refreshing..." : "Refresh"}
          </button>
        </div>

        {loading ? (
          <div className="sr-card">Loading…</div>
        ) : err ? (
          <div className="sr-card sr-error">{err}</div>
        ) : !data ? (
          <div className="sr-card">No data</div>
        ) : (
          <>
            {/* Summary */}
            <div className="sr-grid-3">
              <div className="sr-card sr-stat">
                <div className="sr-label">Student</div>
                <div className="sr-value">{data.fullName || data.username}</div>
                <div className="sr-muted">#{data.studentId}</div>
              </div>

              <div className="sr-card sr-stat">
                <div className="sr-label">Overall</div>
                <div className="sr-value">{overallPercentage.toFixed(2)}%</div>
                <div className="sr-muted">
                  Grade: <b>{overallGrade}</b> •{" "}
                  <span className={`sr-pill ${overallPass ? "sr-pass" : "sr-fail"}`}>
                    {overallPass ? "PASS" : "FAIL"}
                  </span>
                </div>

                {!overallPass && subjects.length > 0 && (
                  <div className="sr-note">
                    Note: If you fail in any one subject, overall grade becomes <b>NG</b>.
                  </div>
                )}
              </div>

              <div className="sr-card sr-stat">
                <div className="sr-label">Total</div>
                <div className="sr-value">
                  {data.totalObtained} / {data.totalMax}
                </div>
                <div className="sr-muted">Published: {publishedText}</div>
              </div>
            </div>

            {/* Subject table */}
            <div className="sr-card" style={{ marginTop: 14 }}>
              <div className="sr-card-title">Subject-wise Result</div>

              {!subjects.length ? (
                <div className="sr-muted" style={{ marginTop: 10 }}>
                  No marks found.
                </div>
              ) : (
                <div className="sr-table-wrap">
                  <table className="sr-table">
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
                          <td className="sr-strong">{s.subjectName}</td>
                          <td className="sr-mono">{s.obtained}</td>
                          <td className="sr-mono">{s.max}</td>
                          <td className="sr-mono">
                            {Number(s.percentage ?? 0).toFixed(2)}%
                          </td>
                          <td className="sr-mono">{s.grade}</td>
                          <td>
                            <span className={`sr-pill ${s.pass ? "sr-pass" : "sr-fail"}`}>
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
  );
}
