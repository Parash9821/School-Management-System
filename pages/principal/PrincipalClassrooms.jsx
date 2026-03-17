import { useEffect, useState } from "react";
import api from "../../api/api";
import Navbar from "../../components/Navbar";
import "../../styles/PrincipalStyle/PrincipalClassroom.css";

export default function PrincipalClassrooms() {
  const [loading, setLoading] = useState(true);
  const [classrooms, setClassrooms] = useState([]);
  const [name, setName] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState(null);

  const fetchClassrooms = async () => {
    setError("");
    setLoading(true);
    try {
      const res = await api.get("/api/principal/classrooms");
      setClassrooms(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      setError(e?.response?.data?.message || "Failed to load classrooms");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchClassrooms();
  }, []);

  const createClassroom = async (e) => {
    e.preventDefault();
    setError("");

    if (!name.trim()) {
      setError("Classroom name is required");
      return;
    }

    setSaving(true);
    try {
      await api.post("/api/principal/classrooms", { name: name.trim() });
      setName("");
      await fetchClassrooms();
    } catch (e) {
      setError(e?.response?.data?.message || "Failed to create classroom");
    } finally {
      setSaving(false);
    }
  };

  const deleteClassroom = async (id) => {
    setError("");
    const ok = window.confirm("Delete this classroom?");
    if (!ok) return;

    setDeletingId(id);
    try {
      try {
        await api.delete(`/api/principal/classrooms/${id}`);
      } catch (e1) {
        await api.delete(`/api/principal/classrooms`, { params: { id } });
      }

      setClassrooms((prev) => prev.filter((c) => c.id !== id));
    } catch (e2) {
      setError(e2?.response?.data?.message || "Failed to delete classroom");
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div className="dashboard-container">
      <Navbar title="Principal Classrooms" />

      <div className="pc-container">
        <div className="pc-head">
          <div>
            <div className="pc-title">Classrooms</div>
            <div className="pc-subtitle">Create and manage classrooms for your school.</div>
          </div>
        </div>

        {error && <div className="pc-alert">{error}</div>}

        <div className="pc-grid">
          {/* Create */}
          <div className="pc-card">
            <div className="pc-card-title">Create Classroom</div>

            <form onSubmit={createClassroom} className="pc-form">
              <label className="pc-label">Classroom name</label>
              <input
                className="pc-input"
                placeholder="e.g. Grade 10 A"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />

              <button className="pc-btn pc-primary" type="submit" disabled={saving}>
                {saving ? "Creating..." : "Create"}
              </button>
            </form>
          </div>

          {/* List */}
          <div className="pc-card">
            <div className="pc-card-title">All Classrooms</div>

            <div className="pc-toolbar">
              <div className="pc-muted">
                Total: <b>{classrooms.length}</b>
              </div>

              <button className="pc-btn" onClick={fetchClassrooms} disabled={loading}>
                {loading ? "Refreshing..." : "Refresh"}
              </button>
            </div>

            {loading ? (
              <div className="pc-muted" style={{ marginTop: 10 }}>
                Loading...
              </div>
            ) : classrooms.length === 0 ? (
              <div className="pc-muted" style={{ marginTop: 10 }}>
                No classrooms yet.
              </div>
            ) : (
              <div className="pc-table-wrap">
                <table className="pc-table">
                  <thead>
                    <tr>
                      <th style={{ width: 90 }}>ID</th>
                      <th>Name</th>
                      <th style={{ width: 140, textAlign: "right" }}>Action</th>
                    </tr>
                  </thead>

                  <tbody>
                    {classrooms.map((c) => (
                      <tr key={c.id}>
                        <td className="pc-mono">{c.id}</td>
                        <td className="pc-trunc" title={c.name}>
                          {c.name}
                        </td>
                        <td style={{ textAlign: "right" }}>
                          <button
                            className="pc-btn pc-danger pc-tiny"
                            onClick={() => deleteClassroom(c.id)}
                            disabled={deletingId === c.id}
                          >
                            {deletingId === c.id ? "Deleting..." : "Delete"}
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
