package com.autoapi.client.model;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OffersResponse {

    @SerializedName("meta")
    private Meta meta;

    @SerializedName("result")
    private List<OfferItem> result;

    public Meta getMeta()            { return meta; }
    public List<OfferItem> getResult() { return result; }

    // ------------------------------------------------------------------ Meta

    public static class Meta {
        @SerializedName("page")
        private int page;

        @SerializedName("next_page")
        private Integer nextPage;

        @SerializedName("total")
        private int total;

        @SerializedName("per_page")
        private int perPage;

        public int     getPage()     { return page; }
        public Integer getNextPage() { return nextPage; }
        public int     getTotal()    { return total; }
        public int     getPerPage()  { return perPage; }
    }

    // ------------------------------------------------------------------ OfferItem

    public static class OfferItem {
        @SerializedName("id")
        private String id;

        @SerializedName("source")
        private String source;

        @SerializedName("url")
        private String url;

        @SerializedName("data")
        private JsonElement data;

        public String      getId()     { return id; }
        public String      getSource() { return source; }
        public String      getUrl()    { return url; }
        public JsonElement getData()   { return data; }
    }
}
