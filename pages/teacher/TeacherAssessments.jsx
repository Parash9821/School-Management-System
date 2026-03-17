import { useEffect, useMemo, useState } from "react";
import api from "../../api/api";
import Navbar from "../../components/Navbar";

export default function TeacherAssignments() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");

  const fetchData = async () => {
    setLoading(true);
    setErr("");
    try {
      const res = await api.get("/api/teacher/assignments");
      setRows(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      setErr(e?.response?.data?.message || "Failed to load assignments");
      setRows([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // normalize each row so UI never depends on nested entity structure
  const normalized = useMemo(() => {
    return rows.map((a) => {
      const classroomName = a.classroomName ?? a.classroom?.name ?? "-";
      const classroomId = a.classroomId ?? a.classroom?.id ?? null;

      const subjectName = a.subjectName ?? a.subject?.name ?? "-";
      const subjectCode = a.subjectCode ?? a.subject?.code ?? "—";
      const subjectId = a.subjectId ?? a.subject?.id ?? null;

      return {
        id: a.id,
        classroomId,
        classroomName,
        subjectId,
        subjectName,
        subjectCode,
      };
    });
  }, [rows]);

  return (
    <div className="dashboard-container">
      <Navbar title="Teacher Assignments" />

      <div className="container">
        <div className="page-head">
          <div>
            <h2 className="page-title">My Assignments</h2>
            <p className="page-subtitle">
              Assigned classroom + subject list (created by principal).
            </p>
          </div>

          <button className="btn" onClick={fetchData} disabled={loading}>
            {loading ? "Refreshing..." : "Refresh"}
          </button>
        </div>

        {err && <div className="error">{err}</div>}

        <div className="card">
          {loading ? (
            <div className="muted">Loading…</div>
          ) : normalized.length === 0 ? (
            <div className="muted">No assignments found.</div>
          ) : (
            <div className="table">
              <div className="table-head">
                <div>Classroom</div>
                <div>Subject</div>
                <div style={{ textAlign: "right" }}>Assignment</div>
              </div>

              {normalized.map((a) => (
                <div className="table-row" key={a.id}>
                  <div>
                    <div className="strong">{a.classroomName}</div>
                    {a.classroomId && (
                      <div className="muted">Classroom ID: {a.classroomId}</div>
                    )}
                  </div>

                  <div>
                    <div className="strong">{a.subjectName}</div>
                    <div className="muted">
                      Code: <span className="mono">{a.subjectCode}</span>
                      {a.subjectCode ? <>  {a.subjectCode}</> : null}
                    </div>
                  </div>

                  <div style={{ textAlign: "right" }} className="mono">
                    #{a.id}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
