package com.autoapi.client.firebase;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Firebase Realtime Database-тай холбогдох кэш давхарга.
 * REST API ашиглан GET / PUT хийдэг — Firebase Admin SDK шаардахгүй.
 *
 * <pre>{@code
 * // Database Secret (legacy) ашиглах үед
 * FirebaseCache cache = new FirebaseCache(
 *     "https://your-project-default-rtdb.firebaseio.com",
 *     "YOUR_DATABASE_SECRET"
 * );
 *
 * // Гараар TTL тохируулах (секундээр, default: 1 цаг)
 * FirebaseCache cache = new FirebaseCache(url, secret, 3600);
 * }</pre>
 *
 * <p>Firebase дээрх өгөгдлийн бүтэц:
 * <pre>
 * auto-api-cache/
 *   filters_encar/
 *     value: "..."
 *     expiresAt: 1234567890000
 * </pre>
 */
public class FirebaseCache {

    private static final String CACHE_ROOT = "auto-api-cache";
    private static final long   DEFAULT_TTL_MS = 60 * 60 * 1000L; // 1 цаг

    private final String    baseUrl;
    private final String    secret;
    private final long      ttlMs;
    private final HttpClient http;

    // ------------------------------------------------------------------ constructors

    public FirebaseCache(String firebaseUrl, String databaseSecret) {
        this(firebaseUrl, databaseSecret, DEFAULT_TTL_MS / 1000);
    }

    /**
     * @param firebaseUrl     Firebase Realtime Database URL, e.g. https://my-project.firebaseio.com
     * @param databaseSecret  Firebase database secret (legacy auth)
     * @param ttlSeconds      Cache TTL (хэр удаан хадгалах), секундээр
     */
    public FirebaseCache(String firebaseUrl, String databaseSecret, long ttlSeconds) {
        if (firebaseUrl == null || firebaseUrl.isBlank()) {
            throw new IllegalArgumentException("firebaseUrl must not be blank");
        }
        this.baseUrl = firebaseUrl.endsWith("/") ? firebaseUrl : firebaseUrl + "/";
        this.secret  = databaseSecret;
        this.ttlMs   = ttlSeconds * 1000L;
        this.http    = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // ------------------------------------------------------------------ public

    /**
     * Кэшийн утгыг уншина. Хугацаа дууссан эсвэл олдохгүй бол null буцаана.
     */
    public String get(String key) {
        try {
            String url = buildUrl(key) + ".json" + authParam();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) return null;

            String body = res.body();
            if (body == null || body.equals("null")) return null;

            // JSON-г задлан expiresAt шалгана
            CacheEntry entry = parseCacheEntry(body);
            if (entry == null) return null;
            if (entry.expiresAt < System.currentTimeMillis()) {
                // Хугацаа дууссан тул устгана (async)
                deleteAsync(key);
                return null;
            }
            return entry.value;

        } catch (Exception e) {
            // Cache алдаа нь үндсэн ажиллагааг зогсоохгүй
            return null;
        }
    }

    /**
     * Кэшэд утга хадгална.
     */
    public void set(String key, String jsonValue) {
        try {
            long expiresAt = System.currentTimeMillis() + ttlMs;
            // Firebase-д хадгалах JSON
            String payload = String.format(
                    "{\"value\":%s,\"expiresAt\":%d}",
                    jsonValue, expiresAt
            );

            String url = buildUrl(key) + ".json" + authParam();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            http.send(req, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            // Cache алдаа дарагдана
        }
    }

    /**
     * Кэш бичлэгийг устгана.
     */
    public void delete(String key) {
        try {
            String url = buildUrl(key) + ".json" + authParam();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .build();
            http.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ignored) {}
    }

    // ------------------------------------------------------------------ private

    private String buildUrl(String key) {
        String safeKey = URLEncoder.encode(key, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace(".", "%2E")
                .replace("#", "%23")
                .replace("$", "%24")
                .replace("[", "%5B")
                .replace("]", "%5D");
        return baseUrl + CACHE_ROOT + "/" + safeKey;
    }

    private String authParam() {
        if (secret == null || secret.isBlank()) return "";
        return "?auth=" + URLEncoder.encode(secret, StandardCharsets.UTF_8);
    }

    private void deleteAsync(String key) {
        Thread.ofVirtual().start(() -> delete(key));
    }

    private CacheEntry parseCacheEntry(String json) {
        try {
            // Хялбар JSON задлалт — gson dependency-г ашиглана
            // {"value":..., "expiresAt":1234}
            int expiresIdx = json.lastIndexOf("\"expiresAt\":");
            if (expiresIdx < 0) return null;

            String expiresStr = json.substring(expiresIdx + 12).replaceAll("[^0-9].*", "").trim();
            long expiresAt = Long.parseLong(expiresStr);

            // value хэсгийг авна
            int valueIdx = json.indexOf("\"value\":");
            if (valueIdx < 0) return null;
            String afterValue = json.substring(valueIdx + 8).trim();

            // Trailing comma эсвэл closing brace-ийн өмнөх хэсэг
            // (value нь nested JSON тул string-гүй шууд оруулсан)
            // expiresAt-ийн өмнөх бүх зүйл бол value
            String valuePart = json.substring(valueIdx + 8, expiresIdx).trim();
            if (valuePart.endsWith(",")) {
                valuePart = valuePart.substring(0, valuePart.length() - 1).trim();
            }

            CacheEntry entry = new CacheEntry();
            entry.value     = valuePart;
            entry.expiresAt = expiresAt;
            return entry;

        } catch (Exception e) {
            return null;
        }
    }

    private static class CacheEntry {
        String value;
        long   expiresAt;
    }
}
