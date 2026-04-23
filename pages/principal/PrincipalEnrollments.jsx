import { useEffect, useMemo, useState } from "react";
import api from "../../api/api";
import Navbar from "../../components/Navbar";
import "../../styles/PrincipalStyle/PrincipalEnrollments.css";

export default function PrincipalEnrollments() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);

  // form
  const [studentId, setStudentId] = useState("");
  const [classroomId, setClassroomId] = useState("");
  const [academicYear, setAcademicYear] = useState("2025-2026");

  // filters
  const [filterClassroomId, setFilterClassroomId] = useState("");
  const [filterAcademicYear, setFilterAcademicYear] = useState("2025-2026");

  const [err, setErr] = useState("");

  const canSubmit = useMemo(
    () => Boolean(studentId && classroomId && academicYear.trim()),
    [studentId, classroomId, academicYear]
  );

  const fetchEnrollments = async () => {
    setErr("");

    if (!filterClassroomId || !filterAcademicYear.trim()) {
      setRows([]);
      return;
    }

    setLoading(true);
    try {
      const res = await api.get("/api/principal/enrollments", {
        params: {
          classroomId: Number(filterClassroomId),
          academicYear: filterAcademicYear.trim(),
        },
      });

      setRows(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      setErr(e?.response?.data?.message || "Failed to load enrollments");
      setRows([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    // optional: auto load if filter already filled (you can remove this)
    if (filterClassroomId && filterAcademicYear) fetchEnrollments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const submit = async (e) => {
    e.preventDefault();
    setErr("");

    if (!canSubmit) {
      setErr("studentId, classroomId and academicYear are required.");
      return;
    }

    try {
      await api.post("/api/principal/enrollments", {
        studentId: Number(studentId),
        classroomId: Number(classroomId),
        academicYear: academicYear.trim(),
      });

      // refresh list using current filter
      await fetchEnrollments();

      setStudentId("");
    } catch (e2) {
      setErr(e2?.response?.data?.message || "Enrollment failed");
    }
  };

  const remove = async (id) => {
    setErr("");
    const ok = window.confirm("Delete this enrollment?");
    if (!ok) return;

    try {
      await api.delete(`/api/principal/enrollments/${id}`);
      await fetchEnrollments();
    } catch (e) {
      setErr(e?.response?.data?.message || "Delete failed");
    }
  };

  // ✅ Works with both:
  // - DTO response (studentUsername, classroomName)
  // - entity response (student.id, classroom.id)
  const studentDisplay = (r) => {
    const id = r.studentId ?? r.student?.id ?? "—";
    const username = r.studentUsername ?? r.student?.username ?? "";
    const fullName = r.studentFullName ?? r.student?.fullName ?? "";

    if (username || fullName) {
      return (
        <div className="pe-namecell">
          <div className="pe-strong">
            {fullName || username}
          </div>
          <div className="pe-muted">
            @{username || "—"} • #{id}
          </div>
        </div>
      );
    }

    return <span className="pe-mono">#{id}</span>;
  };

  const classroomDisplay = (r) => {
    const id = r.classroomId ?? r.classroom?.id ?? "—";
    const name = r.classroomName ?? r.classroom?.name ?? "";

    if (name) {
      return (
        <div className="pe-namecell">
          <div className="pe-strong">{name}</div>
          <div className="pe-muted">#{id}</div>
        </div>
      );
    }

    return <span className="pe-mono">#{id}</span>;
  };

  return (
    <div className="dashboard-container">
      <Navbar title="Principal Enrollments" />

      <div className="pe-container">
        <div className="pe-head">
          <div>
            <div className="pe-title">Enrollments</div>
            <div className="pe-subtitle">Enroll students into classrooms (per academic year)</div>
          </div>

          <button className="pe-btn" onClick={fetchEnrollments} disabled={loading}>
            {loading ? "Refreshing..." : "Refresh"}
          </button>
        </div>

        {err && <div className="pe-error">{err}</div>}

        <div className="pe-grid-2">
          {/* LEFT: Create */}
          <div className="pe-card">
            <div className="pe-card-title">Create Enrollment</div>

            <form className="pe-form" onSubmit={submit}>
              <label className="pe-label">Student ID</label>
              <input
                className="pe-input"
                value={studentId}
                onChange={(e) => setStudentId(e.target.value)}
                placeholder="e.g. 6"
              />

              <label className="pe-label">Classroom ID</label>
              <input
                className="pe-input"
                value={classroomId}
                onChange={(e) => setClassroomId(e.target.value)}
                placeholder="e.g. 1"
              />

              <label className="pe-label">Academic Year</label>
              <input
                className="pe-input"
                value={academicYear}
                onChange={(e) => setAcademicYear(e.target.value)}
                placeholder="e.g. 2025-2026"
              />

              <button className="pe-btn pe-btn-primary" type="submit" disabled={!canSubmit}>
                Enroll
              </button>
            </form>
          </div>

          {/* RIGHT: List */}
          <div className="pe-card">
            <div className="pe-card-title">List Enrollments</div>

            {/* Filters toolbar */}
            <div className="pe-toolbar">
              <input
                className="pe-input"
                value={filterClassroomId}
                onChange={(e) => setFilterClassroomId(e.target.value)}
                placeholder="Classroom ID (required)"
              />
              <input
                className="pe-input"
                value={filterAcademicYear}
                onChange={(e) => setFilterAcademicYear(e.target.value)}
                placeholder="Academic Year"
              />
              <button className="pe-btn" onClick={fetchEnrollments} disabled={loading}>
                Load
              </button>
            </div>

            {loading ? (
              <div className="pe-muted" style={{ marginTop: 10 }}>Loading…</div>
            ) : rows.length === 0 ? (
              <div className="pe-muted" style={{ marginTop: 10 }}>
                No enrollments found for the filter.
              </div>
            ) : (
              <div className="pe-table-wrap">
                <table className="pe-table">
                  <thead>
                    <tr>
                      <th style={{ width: 80 }}>ID</th>
                      <th>Student</th>
                      <th>Classroom</th>
                      <th style={{ width: 140 }}>Year</th>
                      <th style={{ width: 120, textAlign: "right" }}>Actions</th>
                    </tr>
                  </thead>

                  <tbody>
                    {rows.map((r) => (
                      <tr key={r.id}>
                        <td className="pe-mono">{r.id}</td>
                        <td>{studentDisplay(r)}</td>
                        <td>{classroomDisplay(r)}</td>
                        <td className="pe-mono">{r.academicYear}</td>
                        <td style={{ textAlign: "right" }}>
                          <button
                            className="pe-btn pe-btn-danger pe-btn-tiny"
                            onClick={() => remove(r.id)}
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>

                {/* Hint if backend doesn’t send names */}

              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
