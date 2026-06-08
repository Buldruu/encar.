package com.autoapi.client.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChangesResponse {

    @SerializedName("meta")
    private Meta meta;

    @SerializedName("result")
    private List<ChangeItem> result;

    public Meta             getMeta()   { return meta; }
    public List<ChangeItem> getResult() { return result; }

    // ------------------------------------------------------------------ Meta

    public static class Meta {
        @SerializedName("next_change_id")
        private int nextChangeId;

        @SerializedName("has_more")
        private boolean hasMore;

        public int     getNextChangeId() { return nextChangeId; }
        public boolean isHasMore()       { return hasMore; }
    }

    // ------------------------------------------------------------------ ChangeItem

    public static class ChangeItem {
        @SerializedName("id")
        private String id;

        @SerializedName("source")
        private String source;

        @SerializedName("type")
        private String type; // "add" | "update" | "remove"

        @SerializedName("url")
        private String url;

        public String getId()     { return id; }
        public String getSource() { return source; }
        public String getType()   { return type; }
        public String getUrl()    { return url; }
    }
}
