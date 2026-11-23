# 💻 Cloud & Linux CLI Command Trainer

An interactive, terminal-themed web application designed to improve command-line syntax accuracy and typing speed across **Linux, AWS, Docker, Git, and Networking** commands.

🔗 **Live Demo:** [https://cloud-cli-trainer.onrender.com](https://cloud-cli-trainer.onrender.com)

---

## 📌 Overview
Beginners often memorize CLI commands but make syntax or flag errors under time pressure (e.g., `chmod 400 key.pem`, `netstat -tulnp`). Unlike static cheat sheets, this application provides a timed terminal environment with real-time character validation, Net WPM calculation, and persistent leaderboard tracking.

---

## ✨ Key Features
- **Timed Practice Sessions:** 60-second live production timer (45-second local build) with instant prompt switching.
- **Exact-Match Validation & Scoring:** Awards `+10` points only for exact command matches and calculates **Net Words Per Minute (Net WPM)**.
- **Dual Storage Persistence:**
  - **SQLite Database (`leaderboard.db`):** Powers the real-time Top-5 leaderboard using parameterized SQL queries.
  - **CSV Audit Log (`scores.csv`):** Automatically logs every session in an MS Excel-compatible format.
- **Mobile-Responsive UI:** Configured with `autocapitalize="off"`, `autocorrect="off"`, real-time input auto-validation, and a dedicated `RUN` button for seamless smartphone usage.

---

## 🛠️ Tech Stack
- **Frontend:** HTML5, CSS3, Vanilla JavaScript
- **Backend:** Python 3, Flask
- **Database & Storage:** SQLite 3 (`leaderboard.db`), Python `csv` module (`scores.csv`)
- **Deployment:** GitHub, Render Cloud, Gunicorn WSGI Server

---

## 🏗️ Architecture & API Routes
- `GET /` — Renders the terminal-themed frontend (`templates/index.html`)
