package dev.ohate.swift.json;

public interface JsonProvider {

    String toJson(Object value);

    <T> T fromJson(String json, Class<T> type);

}
