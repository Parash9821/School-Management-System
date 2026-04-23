import { useEffect, useMemo, useState } from "react";
import api from "../../api/api";
import Navbar from "../../components/Navbar";
import "../../components/Footer";
import "../../styles/PrincipalStyle/PrincipalAssignment.css";

export default function PrincipalAssignments() {
  const [teachers, setTeachers] = useState([]);
  const [classrooms, setClassrooms] = useState([]);
  const [subjects, setSubjects] = useState([]);

  const [filterClassroomId, setFilterClassroomId] = useState("");
  const [rows, setRows] = useState([]);

  const [teacherId, setTeacherId] = useState("");
  const [classroomId, setClassroomId] = useState("");
  const [subjectId, setSubjectId] = useState("");

  const [loadingList, setLoadingList] = useState(false);
  const [creating, setCreating] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [err, setErr] = useState("");

  // ---------- labels (DTO or nested objects)
  const teacherLabel = (a) =>
    a.teacher?.username
      ? `${a.teacher.username} (#${a.teacher.id})`
      : a.teacherUsername
      ? `${a.teacherUsername} (#${a.teacherId})`
      : a.teacherId
      ? `#${a.teacherId}`
      : "—";

  const classroomLabel = (a) =>
    a.classroom?.name
      ? `${a.classroom.name} (#${a.classroom.id})`
      : a.classroomName
      ? `${a.classroomName} (#${a.classroomId})`
      : a.classroomId
      ? `#${a.classroomId}`
      : "—";

  const subjectLabel = (a) =>
    a.subject?.name
      ? `${a.subject.name} (#${a.subject.id})`
      : a.subjectName
      ? `${a.subjectName} (#${a.subjectId})`
      : a.subjectId
      ? `#${a.subjectId}`
      : "—";

  // ---------- fetch dropdown data
  const fetchTeachers = async () => {
    const res = await api.get("/api/principal/users", { params: { role: "TEACHER" } });
    setTeachers(Array.isArray(res.data) ? res.data : []);
  };

  const fetchClassrooms = async () => {
    const res = await api.get("/api/principal/classrooms");
    const data = Array.isArray(res.data) ? res.data : [];
    setClassrooms(data);

    if (!filterClassroomId && data.length > 0) {
      setFilterClassroomId(String(data[0].id));
    }
  };

  const fetchSubjects = async () => {
    const res = await api.get("/api/principal/subjects");
    setSubjects(Array.isArray(res.data) ? res.data : []);
  };

  const fetchAssignments = async (cidParam) => {
    const cid = cidParam ?? filterClassroomId;
    if (!cid) {
      setRows([]);
      return;
    }

    setLoadingList(true);
    setErr("");
    try {
      const res = await api.get("/api/principal/assignments", {
        params: { classroomId: Number(cid) },
      });
      setRows(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      setErr(e?.response?.data?.message || "Failed to load assignments");
      setRows([]);
    } finally {
      setLoadingList(false);
    }
  };

  useEffect(() => {
    (async () => {
      try {
        await Promise.all([fetchTeachers(), fetchSubjects(), fetchClassrooms()]);
      } catch (e) {
        setErr(e?.response?.data?.message || "Failed to initialize data");
      }
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (filterClassroomId) fetchAssignments(filterClassroomId);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterClassroomId]);

  const canSubmit = useMemo(
    () => Boolean(teacherId && classroomId && subjectId),
    [teacherId, classroomId, subjectId]
  );

  const resetForm = () => {
    setTeacherId("");
    setClassroomId("");
    setSubjectId("");
  };

  const create = async (e) => {
    e.preventDefault();
    setErr("");

    if (!canSubmit) {
      setErr("Select teacher, classroom and subject.");
      return;
    }

    setCreating(true);
    try {
      await api.post("/api/principal/assignments", {
        teacherId: Number(teacherId),
        classroomId: Number(classroomId),
        subjectId: Number(subjectId),
      });

      resetForm();
      if (filterClassroomId) await fetchAssignments(filterClassroomId);
    } catch (e2) {
      setErr(e2?.response?.data?.message || "Create assignment failed");
    } finally {
      setCreating(false);
    }
  };

  const remove = async (id) => {
    setErr("");
    const ok = window.confirm("Delete this teaching assignment?");
    if (!ok) return;

    setDeletingId(id);
    try {
      await api.delete(`/api/principal/assignments/${id}`);
      if (filterClassroomId) await fetchAssignments(filterClassroomId);
    } catch (e) {
      setErr(e?.response?.data?.message || "Delete failed");
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div className="dashboard-container">
      <Navbar title="Principal Assignments" />

      <div className="pa-container">
        <div className="pa-head">
          <div>
            <div className="pa-title">Teaching Assignments</div>
            <div className="pa-subtitle">
              Assign teachers to classroom + subject. This decides who can create assessments & enter marks.
            </div>
          </div>
        </div>

        {err && <div className="pa-alert">{err}</div>}

        <div className="pa-grid">
          {/* Create card */}
          <div className="pa-card">
            <div className="pa-card-title">Create Assignment</div>

            <form className="pa-form" onSubmit={create}>
              <div className="pa-fields">
                <div className="pa-field">
                  <label className="pa-label">Teacher</label>
                  <select className="pa-input" value={teacherId} onChange={(e) => setTeacherId(e.target.value)}>
                    <option value="">Select teacher</option>
                    {teachers.map((t) => (
                      <option key={t.id} value={t.id}>
                        {t.id} • {t.username}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="pa-field">
                  <label className="pa-label">Classroom</label>
                  <select className="pa-input" value={classroomId} onChange={(e) => setClassroomId(e.target.value)}>
                    <option value="">Select classroom</option>
                    {classrooms.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.id} • {c.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="pa-field">
                  <label className="pa-label">Subject</label>
                  <select className="pa-input" value={subjectId} onChange={(e) => setSubjectId(e.target.value)}>
                    <option value="">Select subject</option>
                    {subjects.map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.id} • {s.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <button className="pa-btn pa-primary" type="submit" disabled={!canSubmit || creating}>
                {creating ? "Creating..." : "Create Assignment"}
              </button>
            </form>
          </div>

          {/* List card */}
          <div className="pa-card">
            <div className="pa-card-title">Assignments List</div>

            <div className="pa-toolbar">
              <div className="pa-toolbar-left">
                <span className="pa-muted">Classroom</span>
                <select
                  className="pa-input pa-filter"
                  value={filterClassroomId}
                  onChange={(e) => setFilterClassroomId(e.target.value)}
                >
                  {classrooms.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.id} • {c.name}
                    </option>
                  ))}
                </select>
              </div>

              <button
                className="pa-btn"
                onClick={() => fetchAssignments()}
                disabled={loadingList || !filterClassroomId}
              >
                {loadingList ? "Refreshing..." : "Refresh"}
              </button>
            </div>

            {loadingList ? (
              <div className="pa-muted" style={{ marginTop: 10 }}>
                Loading…
              </div>
            ) : rows.length === 0 ? (
              <div className="pa-muted" style={{ marginTop: 10 }}>
                No assignments found for this classroom.
              </div>
            ) : (
              <div className="pa-table-wrap">
                <table className="pa-table">
                  <thead>
                    <tr>
                      <th style={{ width: 80 }}>ID</th>
                      <th>Teacher</th>
                      <th>Classroom</th>
                      <th>Subject</th>
                      <th style={{ width: 130, textAlign: "right" }}>Actions</th>
                    </tr>
                  </thead>

                  <tbody>
                    {rows.map((a) => (
                      <tr key={a.id}>
                        <td className="pa-mono">{a.id}</td>
                        <td className="pa-trunc" title={teacherLabel(a)}>{teacherLabel(a)}</td>
                        <td className="pa-trunc" title={classroomLabel(a)}>{classroomLabel(a)}</td>
                        <td className="pa-trunc" title={subjectLabel(a)}>{subjectLabel(a)}</td>
                        <td style={{ textAlign: "right" }}>
                          <button
                            className="pa-btn pa-danger pa-tiny"
                            onClick={() => remove(a.id)}
                            disabled={deletingId === a.id}
                          >
                            {deletingId === a.id ? "Deleting..." : "Delete"}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
