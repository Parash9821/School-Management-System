import { useEffect, useMemo, useState, Fragment } from "react";
import api from "../../api/api";
import Navbar from "../../components/Navbar";
import "../../styles/PrincipalStyle/PrincipalUsers.css";

const ROLES = ["STUDENT", "TEACHER", "DEPARTMENT_HEAD"];

export default function PrincipalUsers() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);

  // filters
  const [roleFilter, setRoleFilter] = useState("");

  // create form
  const [username, setUsername] = useState("");
  const [fullName, setFullName] = useState("");
  const [role, setRole] = useState("STUDENT");
  const [password, setPassword] = useState("123456");

  // password reset
  const [pwUserId, setPwUserId] = useState(null);
  const [newPassword, setNewPassword] = useState("");

  const [err, setErr] = useState("");
  const [ok, setOk] = useState("");

  const canCreate = useMemo(() => {
    return (
      username.trim() &&
      fullName.trim() &&
      role &&
      password.trim().length >= 6
    );
  }, [username, fullName, role, password]);

  const fetchUsers = async () => {
    setLoading(true);
    setErr("");
    setOk("");
    try {
      const res = await api.get("/api/principal/users", {
        params: roleFilter ? { role: roleFilter } : {},
      });
      setRows(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      setErr(e?.response?.data?.message || "Failed to load users");
      setRows([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [roleFilter]);

  const resetCreateForm = () => {
    setUsername("");
    setFullName("");
    setRole("STUDENT");
    setPassword("123456");
  };

  const createUser = async (e) => {
    e.preventDefault();
    setErr("");
    setOk("");

    if (!canCreate) {
      setErr("Fill username, full name and password (min 6 chars).");
      return;
    }

    try {
      await api.post("/api/principal/users", {
        username: username.trim(),
        fullName: fullName.trim(),
        role,
        password: password.trim(),
      });

      setOk("User created successfully ✅");
      resetCreateForm();
      fetchUsers();
    } catch (e) {
      setErr(e?.response?.data?.message || "Create failed");
    }
  };

  const toggleEnabled = async (u) => {
    setErr("");
    setOk("");

    const confirmIt = window.confirm(
      `${u.enabled ? "Disable" : "Enable"} user "${u.username}"?`
    );
    if (!confirmIt) return;

    try {
      await api.patch(`/api/principal/users/${u.id}/enabled`, null, {
        params: { enabled: !u.enabled },
      });
      setOk("Updated user status ✅");
      fetchUsers();
    } catch (e) {
      setErr(e?.response?.data?.message || "Failed to update status");
    }
  };

  const startResetPassword = (u) => {
    setPwUserId(u.id);
    setNewPassword("");
    setErr("");
    setOk("");
  };

  const cancelResetPassword = () => {
    setPwUserId(null);
    setNewPassword("");
  };

  const submitResetPassword = async () => {
    setErr("");
    setOk("");

    if (!pwUserId) return;

    if (newPassword.trim().length < 6) {
      setErr("Password must be at least 6 characters.");
      return;
    }

    try {
      await api.put(`/api/principal/users/${pwUserId}/password`, {
        newPassword: newPassword.trim(),
      });

      setOk("Password updated ✅");
      cancelResetPassword();
      fetchUsers();
    } catch (e) {
      setErr(e?.response?.data?.message || "Password update failed");
    }
  };

  return (
    <div className="dashboard-container">
      <Navbar title="Principal Users" />

      <div className="pu-container">
        <div className="pu-head">
          <div>
            <div className="pu-title">Users</div>
            <div className="pu-subtitle">
              Create users, enable/disable accounts, and reset passwords.
            </div>
          </div>

          <div className="pu-head-actions">
            <select
              className="pu-input pu-compact"
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value)}
            >
              <option value="">All roles</option>
              {ROLES.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
              <option value="PRINCIPAL">PRINCIPAL</option>
            </select>

            <button
              className="pu-btn"
              onClick={fetchUsers}
              disabled={loading}
            >
              {loading ? "Refreshing..." : "Refresh"}
            </button>
          </div>
        </div>

        {(err || ok) && (
          <div className={`pu-alert ${err ? "danger" : "success"}`}>
            {err || ok}
          </div>
        )}

        <div className="pu-grid">
          {/* LEFT: Create user */}
          <div className="pu-card">
            <div className="pu-card-title">Create User</div>

            <form className="pu-form" onSubmit={createUser}>
              <div className="pu-field">
                <label className="pu-label">Username</label>
                <input
                  className="pu-input"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                />
              </div>

              <div className="pu-field">
                <label className="pu-label">Full Name</label>
                <input
                  className="pu-input"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                />
              </div>

              <div className="pu-field">
                <label className="pu-label">Role</label>
                <select
                  className="pu-input"
                  value={role}
                  onChange={(e) => setRole(e.target.value)}
                >
                  {ROLES.map((r) => (
                    <option key={r} value={r}>
                      {r}
                    </option>
                  ))}
                </select>
              </div>

              <div className="pu-field">
                <label className="pu-label">Password</label>
                <input
                  className="pu-input"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>

              <div className="pu-row">
                <button
                  className="pu-btn pu-primary"
                  type="submit"
                  disabled={!canCreate}
                >
                  Create
                </button>
                <button
                  className="clear"
                  type="button"
                  onClick={resetCreateForm}
                >
                  Clear
                </button>
              </div>
            </form>
          </div>

          {/* RIGHT: Users list */}
          <div className="pu-card">
            <div className="pu-card-title">All Users</div>

            {loading ? (
              <div className="pu-muted" style={{ marginTop: 10 }}>
                Loading…
              </div>
            ) : rows.length === 0 ? (
              <div className="pu-muted" style={{ marginTop: 10 }}>
                No users found.
              </div>
            ) : (
              <div className="pu-table-wrap">
                <table className="pu-table">
                  <thead>
                    <tr>
                      <th>Username</th>
                      <th style={{ width: 100 }}>User ID</th>
                      <th style={{ width: 160 }}>Role</th>
                      <th style={{ width: 140 }}>Status</th>
                      <th style={{ width: 250, textAlign: "right" }}>
                        Actions
                      </th>
                    </tr>
                  </thead>

                  <tbody>
                    {rows.map((u) => (
                      <Fragment key={u.id}>
                        <tr>
                          <td className="pu-mono">{u.username}</td>
                          <td className="pu-mono">{u.id}</td>

                          <td>
                            <span className="pu-pill pu-role">
                              {u.role}
                            </span>
                          </td>

                          <td>
                            <span
                              className={`pu-pill ${
                                u.enabled ? "enabled" : "disabled"
                              }`}
                            >
                              {u.enabled ? "ENABLED" : "DISABLED"}
                            </span>
                          </td>

                          <td style={{ textAlign: "right" }}>
                            <div className="pu-actions">
                              <button
                                className="pu-btn pu-tiny"
                                onClick={() => toggleEnabled(u)}
                              >
                                {u.enabled ? "Disable" : "Enable"}
                              </button>

                              <button
                                className="pu-btn pu-tiny"
                                onClick={() => startResetPassword(u)}
                              >
                                Reset Password
                              </button>
                            </div>
                          </td>
                        </tr>

                        {pwUserId === u.id && (
                          <tr>
                            <td colSpan={5} className="pu-expand">
                              <div className="pu-expand-inner">
                                <div className="pu-expand-title">
                                  Reset password for <b>{u.username}</b>
                                </div>

                                <div className="pu-expand-row">
                                  <input
                                    className="pu-input"
                                    type="password"
                                    placeholder="New password (min 6 chars)"
                                    value={newPassword}
                                    onChange={(e) =>
                                      setNewPassword(e.target.value)
                                    }
                                  />
                                  <button
                                    className="pu-btn pu-primary"
                                    type="button"
                                    onClick={submitResetPassword}
                                  >
                                    Save
                                  </button>
                                  <button
                                    className="pu-btn pu-ghost"
                                    type="button"
                                    onClick={cancelResetPassword}
                                  >
                                    Cancel
                                  </button>
                                </div>
                              </div>
                            </td>
                          </tr>
                        )}
                      </Fragment>
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