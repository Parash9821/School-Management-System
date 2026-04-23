import { useEffect, useMemo, useState } from "react";
import api from "../../api/api";
import Navbar from "../../components/Navbar";
import "../../styles/PrincipalStyle/PrincipalSubject.css";
import "../../styles/navbar.css";

export default function PrincipalSubject() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);

  const [mode, setMode] = useState("create");
  const [editingId, setEditingId] = useState(null);
  const [code, setCode] = useState("");
  const [name, setName] = useState("");
  const [err, setErr] = useState("");

  const canSubmit = useMemo(
    () => code.trim() && name.trim(),
    [code, name]
  );

  const fetchSubjects = async () => {
    setLoading(true);
    try {
      const res = await api.get("/api/principal/subjects");
      setRows(res.data || []);
    } catch {
      setErr("Failed to load subjects");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSubjects();
  }, []);

  const resetForm = () => {
    setMode("create");
    setEditingId(null);
    setCode("");
    setName("");
    setErr("");
  };

  const submit = async (e) => {
    e.preventDefault();
    if (!canSubmit) return;

    try {
      if (mode === "create") {
        await api.post("/api/principal/subjects", { code, name });
      } else {
        await api.put(`/api/principal/subjects/${editingId}`, { code, name });
      }
      resetForm();
      fetchSubjects();
    } catch {
      setErr("Save failed");
    }
  };

  const startEdit = (s) => {
    setMode("edit");
    setEditingId(s.id);
    setCode(s.code);
    setName(s.name);
  };

  const remove = async (id) => {
    if (!window.confirm("Delete this subject?")) return;
    await api.delete(`/api/principal/subjects/${id}`);
    fetchSubjects();
  };

  return (
    <div className="dashboard-container">
      <Navbar title="Principal Subjects" />

      <div className="ps-container">
        <div className="ps-grid">

          {/* FORM */}
          <div className="ps-card">
            <h3>{mode === "create" ? "Add Subject" : "Edit Subject"}</h3>

            <form onSubmit={submit} className="ps-form">
              <label>Subject Code</label>
              <input
                value={code}
                onChange={(e) => setCode(e.target.value)}
                placeholder="MATH101"
              />

              <label>Subject Name</label>
              <input
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Mathematics"
              />

              {err && <p className="ps-error">{err}</p>}

              <div className="ps-actions">
                <button className="ps-btn" disabled={!canSubmit}>
                  {mode === "create" ? "Create" : "Update"}
                </button>

                {mode === "edit" && (
                  <button
                    type="button"
                    className="ps-btn ps-btn-outline"
                    onClick={resetForm}
                  >
                    Cancel
                  </button>
                )}
              </div>
            </form>
          </div>

          {/* TABLE */}
          <div className="ps-card">
            <h3>All Subjects</h3>

            {loading ? (
              <p>Loading...</p>
            ) : rows.length === 0 ? (
              <p>No subjects found.</p>
            ) : (
              <table className="ps-table">
                <thead>
                  <tr>
                    <th>Code</th>
                    <th>Name</th>
                    <th className="right">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map((s) => (
                    <tr key={s.id}>
                      <td className="mono">{s.code}</td>
                      <td>{s.name}</td>
                      <td className="right">
                        <button
                          className="ps-btn-small"
                          onClick={() => startEdit(s)}
                        >
                          Edit
                        </button>
                        <button
                          className="ps-btn-small danger"
                          onClick={() => remove(s.id)}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

        </div>
      </div>
    </div>
  );
}
