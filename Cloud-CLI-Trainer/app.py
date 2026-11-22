import os
import sqlite3
import csv
from datetime import datetime
from flask import Flask, render_template, request, jsonify

app = Flask(__name__)

DB_NAME = "leaderboard.db"
EXCEL_FILE = "scores.csv"

def init_storage():
    # 1. SQLite Database & Table setup
    conn = sqlite3.connect(DB_NAME)
    c = conn.cursor()
    c.execute('''CREATE TABLE IF NOT EXISTS scores (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_name TEXT,
                    score INTEGER,
                    wpm INTEGER,
                    date TEXT
                )''')
    conn.commit()
    conn.close()

    # 2. Excel-compatible CSV file setup
    if not os.path.exists(EXCEL_FILE):
        with open(EXCEL_FILE, mode='w', newline='') as f:
            writer = csv.writer(f)
            writer.writerow(["Player Name", "Score", "WPM", "Date"])

# Render / Gunicorn startup par bhi database aur CSV initialize karega
init_storage()

@app.route('/')
def home():
    return render_template('index.html')

@app.route('/save_score', methods=['POST'])
def save_score():
    data = request.json
    name = data.get('name', 'Anonymous')
    score = int(data.get('score', 0))
    wpm = int(data.get('wpm', 0))
    now = datetime.now().strftime("%Y-%m-%d %H:%M")

    conn = sqlite3.connect(DB_NAME)
    c = conn.cursor()
    c.execute("INSERT INTO scores (player_name, score, wpm, date) VALUES (?, ?, ?, ?)",
              (name, score, wpm, now))
    conn.commit()
    conn.close()

    with open(EXCEL_FILE, mode='a', newline='') as f:
        writer = csv.writer(f)
        writer.writerow([name, score, wpm, now])

    return jsonify({"status": "saved"})

@app.route('/leaderboard', methods=['GET'])
def get_leaderboard():
    conn = sqlite3.connect(DB_NAME)
    c = conn.cursor()
    c.execute("SELECT player_name, score, wpm, date FROM scores ORDER BY score DESC LIMIT 5")
    rows = c.fetchall()
    conn.close()
    return jsonify(rows)

if __name__ == '__main__':
    port = int(os.environ.get("PORT", 5000))
    app.run(host='0.0.0.0', port=port)