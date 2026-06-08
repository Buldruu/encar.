package com.autoapi.client;

import com.autoapi.client.exception.ApiException;
import com.autoapi.client.exception.AuthException;
import com.autoapi.client.firebase.FirebaseCache;
import com.autoapi.client.model.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * Java client for auto-api.com.
 * Supports optional Firebase Realtime Database caching.
 *
 * <pre>{@code
 * // Without cache
 * AutoApiClient client = new AutoApiClient("your-api-key");
 *
 * // With Firebase cache
 * FirebaseCache cache = new FirebaseCache("https://your-project.firebaseio.com", "your-firebase-secret");
 * AutoApiClient client = new AutoApiClient("your-api-key", cache);
 * }</pre>
 */
public class AutoApiClient {

    private static final String BASE_URL = "https://auto-api.com/api/v1";

    private final String apiKey;
    private final HttpClient http;
    private final Gson gson;
    private final FirebaseCache firebaseCache;

    // ------------------------------------------------------------------ constructors

    public AutoApiClient(String apiKey) {
        this(apiKey, null);
    }

    public AutoApiClient(String apiKey, FirebaseCache firebaseCache) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        this.apiKey      = apiKey;
        this.firebaseCache = firebaseCache;
        this.http        = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson        = new Gson();
    }

    // ------------------------------------------------------------------ public API

    /** Returns available filters (brands, models, etc.) for a given source. */
    public Map<String, Object> getFilters(String source) throws IOException, InterruptedException {
        String path = "/filters/" + encode(source);
        String cacheKey = "filters_" + source;

        // Firebase cache-аас уншина
        if (firebaseCache != null) {
            String cached = firebaseCache.get(cacheKey);
            if (cached != null) {
                Type type = new TypeToken<Map<String, Object>>(){}.getType();
                return gson.fromJson(cached, type);
            }
        }

        String json = get(path);

        // Firebase cache-д хадгална
        if (firebaseCache != null) {
            firebaseCache.set(cacheKey, json);
        }

        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        return gson.fromJson(json, type);
    }

    /** Search car listings. */
    public OffersResponse getOffers(String source, OffersParams params)
            throws IOException, InterruptedException {

        String path = "/offers/" + encode(source) + "?" + params.toQueryString();
        String cacheKey = "offers_" + source + "_" + params.cacheKey();

        if (firebaseCache != null) {
            String cached = firebaseCache.get(cacheKey);
            if (cached != null) {
                return gson.fromJson(cached, OffersResponse.class);
            }
        }

        String json = get(path);

        if (firebaseCache != null) {
            firebaseCache.set(cacheKey, json);
        }

        return gson.fromJson(json, OffersResponse.class);
    }

    /** Get a single offer by ID. */
    public OffersResponse getOffer(String source, String offerId)
            throws IOException, InterruptedException {

        String path = "/offers/" + encode(source) + "/" + encode(offerId);
        String cacheKey = "offer_" + source + "_" + offerId;

        if (firebaseCache != null) {
            String cached = firebaseCache.get(cacheKey);
            if (cached != null) {
                return gson.fromJson(cached, OffersResponse.class);
            }
        }

        String json = get(path);

        if (firebaseCache != null) {
            firebaseCache.set(cacheKey, json);
        }

        return gson.fromJson(json, OffersResponse.class);
    }

    /** Get the change ID for a specific date (used to start change tracking). */
    public int getChangeId(String source, String date) throws IOException, InterruptedException {
        String path = "/changes/" + encode(source) + "/id?date=" + encode(date);
        String json = get(path);
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(json, type);
        return ((Number) map.get("change_id")).intValue();
    }

    /** Get a batch of changes starting from the given change ID. */
    public ChangesResponse getChanges(String source, int changeId)
            throws IOException, InterruptedException {
        String path = "/changes/" + encode(source) + "?change_id=" + changeId;
        String json = get(path);
        return gson.fromJson(json, ChangesResponse.class);
    }

    /** Fetch offer details by its marketplace URL. */
    public Map<String, Object> getOfferByUrl(String url) throws IOException, InterruptedException {
        String path = "/offer-by-url?url=" + encode(url);
        String json = get(path);
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        return gson.fromJson(json, type);
    }

    // ------------------------------------------------------------------ internal

    private String get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("X-Api-Key", apiKey)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        if (status == 401 || status == 403) {
            throw new AuthException(status, response.body());
        }
        if (status < 200 || status >= 300) {
            throw new ApiException(status, "HTTP " + status, response.body());
        }

        return response.body();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
