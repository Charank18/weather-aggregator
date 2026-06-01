import { useState } from "react";
import axios from "axios";

// API base — empty string so the Vite proxy handles /weather/* → localhost:8080
const API_BASE = "";

async function parseErrorMessage(response) {
  const text = await response.text().catch(() => "");
  if (!text) return `Request failed (${response.status})`;
  try {
    const json = JSON.parse(text);
    return json.error || text;
  } catch {
    return text;
  }
}

/* ── Shared style tokens ───────────────────────────────────────────────── */
const YELLOW      = "#f5c518";
const YELLOW_DARK = "#d4a010";
const WHITE       = "#ffffff";

/* White pill with yellow text — used for both the input and button */
const pillBase = {
  padding: "9px 16px",
  borderRadius: "999px",
  border: `2px solid ${YELLOW}`,
  background: WHITE,
  color: YELLOW_DARK,
  fontWeight: 700,
  fontSize: "15px",
  outline: "none",
  boxShadow: "0 2px 8px rgba(0,0,0,0.10)",
  transition: "box-shadow 0.2s, border-color 0.2s",
};

function App() {
  const [city, setCity]       = useState("");
  const [readings, setReadings] = useState([]);
  const [error, setError]     = useState(null);
  const [loading, setLoading] = useState(false);

  async function fetchWeather() {
    const trimmed = city.trim();
    if (!trimmed) {
      setReadings([]);
      setError("Please enter a city name.");
      return;
    }

    setReadings([]);
    setError(null);
    setLoading(true);

    try {
      const encodedCity = encodeURIComponent(trimmed);

      const postRes = await fetch(`${API_BASE}/weather/fetch?city=${encodedCity}`, {
        method: "POST",
      });

      if (!postRes.ok) {
        const message = await parseErrorMessage(postRes);
        if (postRes.status === 404) {
          setError(message || `City not found: ${trimmed}`);
        } else if (postRes.status === 429) {
          setError(message || "Too many requests. Please wait a few seconds and try again.");
        } else if (postRes.status === 400) {
          setError(message || "Invalid request.");
        } else {
          setError(message || `Failed to fetch weather (${postRes.status})`);
        }
        return;
      }

      const response = await axios.get(`${API_BASE}/weather/${encodedCity}`);
      setReadings(response.data);
    } catch (err) {
      console.error(err);
      if (axios.isAxiosError(err) && err.response) {
        const status = err.response.status;
        const message =
          err.response.data?.error || err.message || `Request failed (${status})`;
        setError(message);
      } else {
        setError("Could not reach the server. Is the backend running on port 8080?");
      }
    } finally {
      setLoading(false);
    }
  }

  /* Allow submitting with Enter key in the input */
  function handleKeyDown(e) {
    if (e.key === "Enter") fetchWeather();
  }

  return (
    <div style={{ padding: "24px 20px" }}>

      {/* ── Title ───────────────────────────────────────────────────────── */}
      <h1
        style={{
          fontFamily: "'Dancing Script', cursive",
          fontWeight: 700,
          color: "#c8630a",
          textShadow: "0 1px 4px rgba(200, 99, 10, 0.18)",
          letterSpacing: "0.5px",
          marginBottom: "28px",
        }}
      >
        What a lovely day!
      </h1>

      {/* ── Search row ──────────────────────────────────────────────────── */}
      <div style={{ display: "flex", justifyContent: "center", gap: "10px", flexWrap: "wrap" }}>
        <input
          type="text"
          placeholder="Enter city"
          value={city}
          onChange={(e) => setCity(e.target.value)}
          onKeyDown={handleKeyDown}
          disabled={loading}
          style={{
            ...pillBase,
            minWidth: "220px",
            cursor: "text",
            /* placeholder colour */
            "--placeholder-color": YELLOW_DARK,
          }}
        />

        <button
          onClick={fetchWeather}
          disabled={loading}
          style={{
            ...pillBase,
            cursor: loading ? "not-allowed" : "pointer",
            opacity: loading ? 0.65 : 1,
            paddingInline: "24px",
          }}
        >
          {loading ? "Fetching…" : "Fetch Weather"}
        </button>
      </div>

      <hr style={{ margin: "24px 0", borderColor: "rgba(200,99,10,0.18)" }} />

      {/* ── Result area ─────────────────────────────────────────────────── */}
      <div
        id="solution-box"
        style={{
          minHeight: "52px",
          marginBottom: "16px",
          padding: "16px",
          borderRadius: "10px",
          backgroundColor: error ? "rgba(253,232,232,0.85)" : "rgba(255,255,255,0.55)",
          border: error ? "1px solid #e57373" : "1px solid rgba(245,197,24,0.35)",
          backdropFilter: "blur(4px)",
          WebkitBackdropFilter: "blur(4px)",
        }}
      >
        {/* Error state */}
        {error && (
          <p style={{ margin: 0, color: "#b71c1c" }}>
            <strong>Error:</strong> {error}
          </p>
        )}

        {/* Empty / prompt state */}
        {!error && !loading && readings.length === 0 && (
          <p style={{ margin: 0, color: "#888" }}>
            Enter a city and click Fetch Weather to see stored readings.
          </p>
        )}

        {/* ── Results table ─────────────────────────────────────────────── */}
        {!error && readings.length > 0 && (
          <>
            <h2 style={{ marginTop: 0, color: "#c8630a", fontFamily: "'Dancing Script', cursive" }}>
              Stored Readings
            </h2>

            <div style={{ overflowX: "auto" }}>
              <table
                style={{
                  width: "100%",
                  borderCollapse: "collapse",
                  fontFamily: "system-ui, sans-serif",
                  fontSize: "15px",
                }}
              >
                {/* Table header */}
                <thead>
                  <tr
                    style={{
                      background: "rgba(245,197,24,0.85)",
                      color: "#7a4a00",
                    }}
                  >
                    {["City", "Temperature", "Wind Speed", "Condition", "Time"].map((col) => (
                      <th
                        key={col}
                        style={{
                          padding: "10px 14px",
                          textAlign: "left",
                          fontWeight: 700,
                          whiteSpace: "nowrap",
                          borderBottom: "2px solid rgba(200,150,0,0.4)",
                        }}
                      >
                        {col}
                      </th>
                    ))}
                  </tr>
                </thead>

                {/* Table rows */}
                <tbody>
                  {readings.map((r, idx) => (
                    <tr
                      key={r.id}
                      style={{
                        background:
                          idx % 2 === 0
                            ? "rgba(255,248,220,0.70)"   /* warm cream */
                            : "rgba(255,255,255,0.45)",
                        transition: "background 0.15s",
                      }}
                    >
                      <td style={tdStyle}><strong style={{ color: "#b85c00" }}>{r.city}</strong></td>
                      <td style={tdStyle}>
                        <span style={badgeStyle("#f5c518", "#7a4a00")}>
                          {r.temperature}°C
                        </span>
                      </td>
                      <td style={tdStyle}>
                        <span style={badgeStyle("#ffe07a", "#7a4a00")}>
                          {r.windSpeed} km/h
                        </span>
                      </td>
                      <td style={tdStyle} title={r.weatherDescription}>
                        {r.weatherDescription}
                      </td>
                      <td style={{ ...tdStyle, color: "#888", whiteSpace: "nowrap" }}>
                        {r.fetchedAt
                          ? new Date(r.fetchedAt).toLocaleString()
                          : "—"}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

/* ── Small style helpers ────────────────────────────────────────────────── */

const tdStyle = {
  padding: "9px 14px",
  borderBottom: "1px solid rgba(245,197,24,0.25)",
  verticalAlign: "middle",
};

function badgeStyle(bg, text) {
  return {
    display: "inline-block",
    background: bg,
    color: text,
    borderRadius: "999px",
    padding: "2px 10px",
    fontWeight: 700,
    fontSize: "13px",
  };
}

export default App;
