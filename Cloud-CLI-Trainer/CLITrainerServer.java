import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class ScoreRecord {
    private final String playerName;
    private final int score;
    private final int wpm;
    private final int accuracy;
    private final int mistakes;
    private final String tier;
    private final String date;

    public ScoreRecord(String playerName, int score, int wpm, int accuracy, int mistakes, String tier, String date) {
        this.playerName = playerName;
        this.score = score;
        this.wpm = wpm;
        this.accuracy = accuracy;
        this.mistakes = mistakes;
        this.tier = tier;
        this.date = date;
    }

    public int getScore() { return score; }

    public String toCsvRow() {
        return String.join(",",
            playerName.replace(",", " "),
            String.valueOf(score),
            String.valueOf(wpm),
            String.valueOf(accuracy),
            String.valueOf(mistakes),
            tier,
            date
        );
    }

    public String toJson() {
        return String.format(
            "{\"player_name\":\"%s\",\"score\":%d,\"wpm\":%d,\"accuracy\":%d,\"tier\":\"%s\",\"date\":\"%s\"}",
            playerName.replace("\"", ""), score, wpm, accuracy, tier, date
        );
    }
}

class JavaScoreManager {
    private final String csvFile = "scores.csv";

    public JavaScoreManager() {
        File file = new File(csvFile);
        if (!file.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
                pw.println("Player Name,Score,WPM,Accuracy %,Mistakes,Tier,Date");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void saveRecord(ScoreRecord record) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile, true))) {
            pw.println(record.toCsvRow());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<ScoreRecord> getTopScores(int limit) {
        List<ScoreRecord> list = new ArrayList<>();
        File file = new File(csvFile);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    list.add(new ScoreRecord(
                        parts[0],
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim()),
                        Integer.parseInt(parts[3].trim()),
                        Integer.parseInt(parts[4].trim()),
                        parts[5].trim(),
                        parts[6].trim()
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        list.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return list.subList(0, Math.min(limit, list.size()));
    }

    public String getCsvFilePath() {
        return csvFile;
    }
}

public class CLITrainerServer {
    public static void main(String[] args) throws IOException {
        JavaScoreManager scoreManager = new JavaScoreManager();
        String portEnv = System.getenv("PORT");
        int port = (portEnv != null && !portEnv.isEmpty()) ? Integer.parseInt(portEnv) : 8080;

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", exchange -> {
            byte[] html = Files.readAllBytes(Paths.get("templates/index.html"));
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, html.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(html);
            }
        });

        server.createContext("/save_score", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String name = extractJsonValue(body, "player_name", "Anonymous");
                int score = Integer.parseInt(extractJsonValue(body, "score", "0"));
                int wpm = Integer.parseInt(extractJsonValue(body, "wpm", "0"));
                int acc = Integer.parseInt(extractJsonValue(body, "accuracy", "100"));
                int mistakes = Integer.parseInt(extractJsonValue(body, "mistakes", "0"));
                String tier = extractJsonValue(body, "tier", "E1");
                String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                scoreManager.saveRecord(new ScoreRecord(name, score, wpm, acc, mistakes, tier, date));

                byte[] resp = "{\"status\":\"success\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(resp);
                }
            }
        });

        server.createContext("/leaderboard", exchange -> {
            List<ScoreRecord> top = scoreManager.getTopScores(5);
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < top.size(); i++) {
                json.append(top.get(i).toJson());
                if (i < top.size() - 1) json.append(",");
            }
            json.append("]");
            byte[] resp = json.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });

        server.createContext("/download_csv", exchange -> {
            File file = new File(scoreManager.getCsvFilePath());
            if (file.exists()) {
                byte[] csvBytes = Files.readAllBytes(file.toPath());
                exchange.getResponseHeaders().set("Content-Type", "text/csv");
                exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"scores.csv\"");
                exchange.sendResponseHeaders(200, csvBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(csvBytes);
                }
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Java CLI Trainer Server running on port " + port);
    }

    private static String extractJsonValue(String json, String key, String defaultVal) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx == -1) return defaultVal;
        int start = idx + search.length();
        while (start < json.length() && json.charAt(start) == ' ') start++;
        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            return json.substring(start + 1, end);
        } else {
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
            return json.substring(start, end);
        }
    }
}
