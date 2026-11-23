# >_ Cloud & Linux CLI Command Trainer

An interactive, gamified web-based Command-Line Interface (CLI) trainer built to help students and cloud aspirants build muscle memory and speed across **Linux, AWS, Docker, Git, and Networking** commands.

🔗 **Live Demo:** [https://cloud-cli-trainer-java.onrender.com](https://cloud-cli-trainer-java.onrender.com)

---

## ✨ Key Features

- **12 Progressive Difficulty Tiers (`E1` – `P3`):** Structured across Easy (`E1–E3`), Medium (`M1–M3`), Hard (`H1–H3`), and Pro (`P1–P3`) levels.
- **Adaptive Auto-Progression Mode:** Automatically upgrades the user's tier (`E1 → P3`) after every 3 consecutive accurate commands.
- **Category Filters:** Practice specific tracks (**Linux, AWS, Docker, Git, Networking**) or all mixed together.
- **Real-Time Metrics & Feedback:** Live Net WPM, Accuracy %, Mistake Counter, and character-by-character green/red syntax highlighting.
- **Custom Timer Modes:** Choose between `30s`, `60s`, and `120s` practice sessions.
- **Top-5 Leaderboard & MS Excel Export:** Persistent score tracking with a one-click **Export CSV** download (`scores.csv`).

---

## 🛠️ Tech Stack & OOPS Architecture

- **Frontend:** HTML5, CSS3, Vanilla JavaScript (ES6+ Fetch API, responsive dark terminal UI)
- **Backend:** Core Java 17 (`com.sun.net.httpserver.HttpServer`)
  - `ScoreRecord`: Encapsulates player metrics, serialization to CSV, and JSON formatting.
  - `JavaScoreManager`: Handles thread-safe (`synchronized`) file I/O persistence and leaderboard sorting.
  - `CLITrainerServer`: Lightweight REST server handling `/`, `/save_score`, `/leaderboard`, and `/download_csv`.
- **Storage:** MS Excel-compatible CSV persistence (`scores.csv`)
- **Containerization & Hosting:** Docker (`eclipse-temurin:17-jdk-alpine`) deployed on Render Cloud via GitHub

---

## 🚀 Run Locally with Java 17

```bash
# Compile the Java server
javac CLITrainerServer.java

# Run the server
java CLITrainerServer
