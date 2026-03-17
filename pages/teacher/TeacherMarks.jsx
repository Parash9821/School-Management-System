import { useEffect, useMemo, useState } from "react";
import api from "../../api/api";
import Navbar from "../../components/Navbar";
import "../../styles/TeacherStyle/TeacherMarks.css";

export default function TeacherMarks() {
  // assignments
  const [assignments, setAssignments] = useState([]);
  const [loadingAssignments, setLoadingAssignments] = useState(true);

  // selected assignment
  const [assignmentId, setAssignmentId] = useState("");
  const selected = useMemo(() => {
    const idNum = Number(assignmentId);
    return assignments.find((a) => a.id === idNum) || null;
  }, [assignmentId, assignments]);

  // assessments
  const [assessments, setAssessments] = useState([]);
  const [loadingAssessments, setLoadingAssessments] = useState(false);

  // students
  const [students, setStudents] = useState([]);
  const [loadingStudents, setLoadingStudents] = useState(false);

  // marks state: { [studentId]: { marks, feedback } }
  const [markMap, setMarkMap] = useState({});

  // create assessment form
  const [type, setType] = useState("UNIT_TEST");
  const [maxMarks, setMaxMarks] = useState(100);
  const [heldOn, setHeldOn] = useState(""); // yyyy-mm-dd

  // submit marks
  const [selectedAssessmentId, setSelectedAssessmentId] = useState("");
  const [saving, setSaving] = useState(false);

  const [err, setErr] = useState("");
  const [msg, setMsg] = useState("");

  // -------------------------
  // Load assignments (once)
  // -------------------------
  const fetchAssignments = async () => {
    setLoadingAssignments(true);
    setErr("");
    try {
      const res = await api.get("/api/teacher/assignments");
      setAssignments(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      setErr(e?.response?.data?.message || "Failed to load assignments");
    } finally {
      setLoadingAssignments(false);
    }
  };

  useEffect(() => {
    fetchAssignments();
  }, []);

  // -------------------------
  // When assignment changes, load:
  // - assessments for (classroomId, subjectId)
  // - students for classroomId
  // -------------------------
  const fetchAssessments = async (classroomId, subjectId) => {
    setLoadingAssessments(true);
    setErr("");
    try {
      const res = await api.get("/api/teacher/assessments", {
        params: { classroomId, subjectId },
      });
      setAssessments(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      setErr(e?.response?.data?.message || "Failed to load assessments");
      setAssessments([]);
    } finally {
      setLoadingAssessments(false);
    }
  };

  const fetchStudents = async (classroomId) => {
    setLoadingStudents(true);
    setErr("");
    try {
      const res = await api.get(`/api/teacher/classrooms/${classroomId}/students`);
      const list = Array.isArray(res.data) ? res.data : [];
      setStudents(list);

      // reset mark map for these students
      const next = {};
      for (const s of list) {
        next[s.id] = { marks: "", feedback: "" };
      }
      setMarkMap(next);
    } catch (e) {
      setErr(e?.response?.data?.message || "Failed to load students");
      setStudents([]);
      setMarkMap({});
    } finally {
      setLoadingStudents(false);
    }
  };

  useEffect(() => {
    setMsg("");
    setErr("");
    setAssessments([]);
    setStudents([]);
    setMarkMap({});
    setSelectedAssessmentId("");

    if (!selected) return;

    fetchAssessments(selected.classroomId, selected.subjectId);
    fetchStudents(selected.classroomId);
  }, [selected?.id]); // important: only when selected assignment changes

  // -------------------------
  // Create assessment
  // -------------------------
  const createAssessment = async (e) => {
    e.preventDefault();
    setErr("");
    setMsg("");

    if (!selected) {
      setErr("Select an assignment first.");
      return;
    }
    if (!heldOn) {
      setErr("Held On date is required.");
      return;
    }
    const mm = Number(maxMarks);
    if (!mm || mm < 1) {
      setErr("Max marks must be a valid number.");
      return;
    }

    try {
      await api.post("/api/teacher/assessments", {
        classroomId: selected.classroomId,
        subjectId: selected.subjectId,
        type,
        maxMarks: mm,
        heldOn, // yyyy-mm-dd (LocalDate)
      });

      setMsg("Assessment created.");
      await fetchAssessments(selected.classroomId, selected.subjectId);
    } catch (e2) {
      setErr(e2?.response?.data?.message || "Failed to create assessment");
    }
  };

  // -------------------------
  // Marks table handlers
  // -------------------------
  const setMarks = (studentId, value) => {
    setMarkMap((prev) => ({
      ...prev,
      [studentId]: { ...prev[studentId], marks: value },
    }));
  };

  const setFeedback = (studentId, value) => {
    setMarkMap((prev) => ({
      ...prev,
      [studentId]: { ...prev[studentId], feedback: value },
    }));
  };

  // -------------------------
  // Submit marks (bulk)
  // -------------------------
  const submitMarks = async () => {
    setErr("");
    setMsg("");

    if (!selected) {
      setErr("Select an assignment first.");
      return;
    }
    const assessmentId = Number(selectedAssessmentId);
    if (!assessmentId) {
      setErr("Select an assessment first.");
      return;
    }
    if (students.length === 0) {
      setErr("No students found in this classroom.");
      return;
    }

    // build payload list
    const payload = [];
    for (const s of students) {
      const row = markMap[s.id] || {};
      const m = row.marks;

      if (m === "" || m === null || m === undefined) continue; // skip empty
      const marksNum = Number(m);

      if (Number.isNaN(marksNum)) {
        setErr(`Invalid marks for student ${s.username}`);
        return;
      }

      payload.push({
        assessmentId,
        studentId: s.id,
        marks: marksNum,
        feedback: row.feedback || "",
      });
    }

    if (payload.length === 0) {
      setErr("Enter marks for at least one student.");
      return;
    }

    setSaving(true);
    try {
      // send sequentially (safe) OR Promise.all (fast). We'll do Promise.all.
      await Promise.all(payload.map((p) => api.post("/api/teacher/marks", p)));

      setMsg("Marks saved successfully.");
    } catch (e) {
      setErr(e?.response?.data?.message || "Failed to save marks");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="dashboard-container">
      <Navbar title="Teacher Marks" />

      <div className="container">
        <div className="page-head">
          <div>
            <h2 className="page-title">Marks & Assessments</h2>
            <p className="page-subtitle">
              Select your classroom+subject assignment, create assessments, and enter marks.
            </p>
          </div>

          <button className="btn" onClick={fetchAssignments} disabled={loadingAssignments}>
            Refresh
          </button>
        </div>

        {err && <div className="error">{err}</div>}
        {msg && <div className="success">{msg}</div>}

        {/* 1) Select Assignment */}
        <div className="card">
          <div className="card-title">1) Select Assignment</div>

          {loadingAssignments ? (
            <div className="muted">Loading assignments…</div>
          ) : assignments.length === 0 ? (
            <div className="muted">No assignments found. Ask principal to create teaching assignments.</div>
          ) : (
            <div className="form">
              <label className="label">Classroom + Subject</label>
              <select
                className="input"
                value={assignmentId}
                onChange={(e) => setAssignmentId(e.target.value)}
              >
                <option value="">— Select —</option>
                {assignments.map((a) => (
                  <option key={a.id} value={a.id}>
                    #{a.id} • {a.classroomName || `Classroom ${a.classroomId}`} • {a.subjectName}
                  </option>
                ))}
              </select>

              {selected && (
                <div className="muted" style={{ marginTop: 8 }}>
                  Selected:{" "}
                  <b>{selected.classroomName || `Classroom ${selected.classroomId}`}</b> •{" "}
                  <b>{selected.subjectName}</b>
                </div>
              )}
            </div>
          )}
        </div>

        {/* 2) Create Assessment */}
        <div className="grid-2">
          <div className="card">
            <div className="card-title">2) Create Assessment</div>

            <form className="form" onSubmit={createAssessment}>
              <label className="label">Type</label>
              <select className="input" value={type} onChange={(e) => setType(e.target.value)}>
                <option value="UNIT_TEST">UNIT_TEST</option>
                <option value="MID_TERM">MID_TERM</option>
                <option value="FINAL">FINAL</option>
                <option value="ASSIGNMENT">ASSIGNMENT</option>
                <option value="PROJECT">PROJECT</option>
              </select>

              <label className="label">Max Marks</label>
              <input
                className="input"
                type="number"
                min="1"
                value={maxMarks}
                onChange={(e) => setMaxMarks(e.target.value)}
              />

              <label className="label">Held On</label>
              <input
                className="input"
                type="date"
                value={heldOn}
                onChange={(e) => setHeldOn(e.target.value)}
              />

              <button className="btn primary" type="submit" disabled={!selected}>
                Create Assessment
              </button>
            </form>
          </div>

          {/* 3) Assessments List */}
          <div className="card">
            <div className="card-title">3) Assessments</div>

            {!selected ? (
              <div className="muted">Select an assignment to load assessments.</div>
            ) : loadingAssessments ? (
              <div className="muted">Loading assessments…</div>
            ) : assessments.length === 0 ? (
              <div className="muted">No assessments found for this classroom + subject.</div>
            ) : (
              <div className="form">
                <label className="label">Select Assessment to Enter Marks</label>
                <select
                  className="input"
                  value={selectedAssessmentId}
                  onChange={(e) => setSelectedAssessmentId(e.target.value)}
                >
                  <option value="">— Select —</option>
                  {assessments.map((a) => (
                    <option key={a.id} value={a.id}>
                      #{a.id} • {a.type} • Max {a.maxMarks} • {a.heldOn}
                    </option>
                  ))}
                </select>

                <div className="table" style={{ marginTop: 12 }}>
                  <div className="table-head">
                    <div>ID</div>
                    <div>Type</div>
                    <div>Max</div>
                    <div>Date</div>
                  </div>
                  {assessments.map((a) => (
                    <div className="table-row" key={a.id}>
                      <div className="mono">#{a.id}</div>
                      <div>{a.type}</div>
                      <div className="mono">{a.maxMarks}</div>
                      <div className="mono">{a.heldOn}</div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* 4) Enter Marks */}
        <div className="card">
          <div className="card-title">4) Enter / Update Marks</div>

          {!selected ? (
            <div className="muted">Select an assignment first.</div>
          ) : loadingStudents ? (
            <div className="muted">Loading students…</div>
          ) : students.length === 0 ? (
            <div className="muted">No students enrolled in this classroom.</div>
          ) : (
            <>
              <div className="muted" style={{ marginBottom: 10 }}>
                Enter marks only for students you want to save. Empty marks will be skipped.
              </div>

              <div className="table">
                <div className="table-head">
                  <div>Student</div>
                  <div>Username</div>
                  <div style={{ width: 140 }}>Marks</div>
                  <div>Feedback</div>
                </div>

                {students.map((s) => (
                  <div className="table-row" key={s.id}>
                    <div>
                      <div style={{ fontWeight: 600 }}>{s.fullName || "—"}</div>
                      <div className="muted">ID: {s.id}</div>
                    </div>

                    <div className="mono">{s.username}</div>

                    <div>
                      <input
                        className="input"
                        type="number"
                        value={markMap?.[s.id]?.marks ?? ""}
                        onChange={(e) => setMarks(s.id, e.target.value)}
                        placeholder="e.g. 85"
                      />
                    </div>

                    <div>
                      <input
                        className="input"
                        value={markMap?.[s.id]?.feedback ?? ""}
                        onChange={(e) => setFeedback(s.id, e.target.value)}
                        placeholder="optional feedback"
                      />
                    </div>
                  </div>
                ))}
              </div>

              <div className="row" style={{ marginTop: 12 }}>
                <button
                  className="btn primary"
                  onClick={submitMarks}
                  disabled={saving || !selectedAssessmentId}
                >
                  {saving ? "Saving…" : "Save Marks"}
                </button>
                {!selectedAssessmentId && (
                  <div className="muted" style={{ marginLeft: 10 }}>
                    Select an assessment first.
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
