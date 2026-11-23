import csv
import os
import sqlite3
from datetime import datetime
from flask import Flask, jsonify, render_template, request, send_file

app = Flask(__name__)


class ScoreManager:
    def __init__(self, db_file="leaderboard.db", csv_file="scores.csv"):
        self.db_file = db_file
        self.csv_file = csv_file
        self.init_storage()

    def init_storage(self):
        conn = sqlite3.connect(self.db_file)
        cursor = conn.cursor()
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS scores_v2 (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_name TEXT,
                score INTEGER,
                wpm INTEGER,
                accuracy INTEGER,
                mistakes INTEGER,
                tier TEXT,
                date TEXT
            )
        """)
        conn.commit()
        conn.close()

        if not os.path.exists(self.csv_file):
            with open(self.csv_file, mode="w", newline="", encoding="utf-8") as f:
                writer = csv.writer(f)
                writer.writerow(["Player Name", "Score", "WPM", "Accuracy %", "Mistakes", "Tier", "Date"])

    def save_record(self, player_name, score, wpm, accuracy, mistakes, tier):
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M")

        conn = sqlite3.connect(self.db_file)
        cursor = conn.cursor()
        cursor.execute(
            """INSERT INTO scores_v2 (player_name, score, wpm, accuracy, mistakes, tier, date)
               VALUES (?, ?, ?, ?, ?, ?, ?)""",
            (player_name, score, wpm, accuracy, mistakes, tier, timestamp),
        )
        conn.commit()
        conn.close()

        with open(self.csv_file, mode="a", newline="", encoding="utf-8") as f:
            writer = csv.writer(f)
            writer.writerow([player_name, score, wpm, accuracy, mistakes, tier, timestamp])

    def get_top_scores(self, limit=5):
        conn = sqlite3.connect(self.db_file)
        cursor = conn.cursor()
        cursor.execute(
            "SELECT player_name, score, wpm, accuracy, tier, date FROM scores_v2 ORDER BY score DESC LIMIT ?;",
            (limit,),
        )
        rows = cursor.fetchall()
        conn.close()
        return [
            {
                "player_name": r[0],
                "score": r[1],
                "wpm": r[2],
                "accuracy": r[3],
                "tier": r[4],
                "date": r[5],
            }
            for r in rows
        ]


score_manager = ScoreManager()


@app.route("/")
def index():
    return render_template("index.html")


@app.route("/save_score", methods=["POST"])
def save_score():
    data = request.get_json()
    score_manager.save_record(
        player_name=data.get("player_name", "Anonymous"),
        score=int(data.get("score", 0)),
        wpm=int(data.get("wpm", 0)),
        accuracy=int(data.get("accuracy", 100)),
        mistakes=int(data.get("mistakes", 0)),
        tier=data.get("tier", "E1"),
    )
    return jsonify({"status": "success"})


@app.route("/leaderboard", methods=["GET"])
def leaderboard():
    return jsonify(score_manager.get_top_scores(5))


@app.route("/download_csv", methods=["GET"])
def download_csv():
    if os.path.exists(score_manager.csv_file):
        return send_file(score_manager.csv_file, as_attachment=True)
    return jsonify({"error": "No CSV file found"}), 404


if __name__ == "__main__":
    app.run(debug=True)
    
