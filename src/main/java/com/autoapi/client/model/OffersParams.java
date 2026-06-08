package com.autoapi.client.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Offers хайлтын параметрүүд.
 *
 * <pre>{@code
 * OffersParams params = new OffersParams()
 *     .page(1)
 *     .brand("BMW")
 *     .model("X5")
 *     .yearFrom(2020)
 *     .yearTo(2023)
 *     .priceFrom(10000)
 *     .priceTo(50000);
 * }</pre>
 */
public class OffersParams {

    private final Map<String, String> params = new LinkedHashMap<>();

    public OffersParams page(int page) {
        return put("page", String.valueOf(page));
    }

    public OffersParams brand(String brand) {
        return put("brand", brand);
    }

    public OffersParams model(String model) {
        return put("model", model);
    }

    public OffersParams yearFrom(int year) {
        return put("year_from", String.valueOf(year));
    }

    public OffersParams yearTo(int year) {
        return put("year_to", String.valueOf(year));
    }

    public OffersParams priceFrom(int price) {
        return put("price_from", String.valueOf(price));
    }

    public OffersParams priceTo(int price) {
        return put("price_to", String.valueOf(price));
    }

    public OffersParams mileageFrom(int km) {
        return put("mileage_from", String.valueOf(km));
    }

    public OffersParams mileageTo(int km) {
        return put("mileage_to", String.valueOf(km));
    }

    public OffersParams fuelType(String fuelType) {
        return put("fuel_type", fuelType);
    }

    public OffersParams transmission(String transmission) {
        return put("transmission", transmission);
    }

    public OffersParams param(String key, String value) {
        return put(key, value);
    }

    public String toQueryString() {
        StringJoiner sj = new StringJoiner("&");
        params.forEach((k, v) ->
                sj.add(enc(k) + "=" + enc(v)));
        return sj.toString();
    }

    /** Firebase cache key үүсгэхэд ашиглана */
    public String cacheKey() {
        StringJoiner sj = new StringJoiner("_");
        params.forEach((k, v) -> sj.add(k + "-" + v));
        return sj.toString();
    }

    private OffersParams put(String key, String value) {
        if (value != null && !value.isBlank()) {
            params.put(key, value);
        }
        return this;
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
