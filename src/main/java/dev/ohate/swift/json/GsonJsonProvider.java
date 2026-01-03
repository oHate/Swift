package dev.ohate.swift.json;

import com.google.gson.Gson;

public class GsonJsonProvider implements JsonProvider {

    private final Gson gson;

    public GsonJsonProvider(Gson gson) {
        this.gson = gson;
    }

    public GsonJsonProvider() {
        this(new Gson());
    }

    @Override
    public String toJson(Object value) {
        return this.gson.toJson(value);
    }

    @Override
    public <T> T fromJson(String json, Class<T> type) {
        return this.gson.fromJson(json, type);
    }

}
