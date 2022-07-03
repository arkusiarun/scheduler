package com.airtel.scheduler.execution.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * @author Arun Singh
 */

public class CommonUtils {

    private CommonUtils() {
    }

    private static Gson gson = new Gson();

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static Gson getGson() {
        return gson;
    }

    public static String getJson(Object obj) {
        return gson.toJson(obj);
    }

    public static String getJsonFromMapper(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public static boolean isNullOrEmptyMap(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    public static boolean isEmptyList(List<?> list) {
        return (list == null || list.isEmpty());
    }

    public static LocalDateTime convertEpochToLocalDateTime(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }

    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
    }

    public static String getSHA256Hex(Map<String, Object> referenceKeysObject) {
        try {
            return DigestUtils.sha256Hex(String.valueOf(referenceKeysObject));
        } catch (Exception ex) {
            throw new SecurityException(String.format("Could not generate Hash Key : %s", referenceKeysObject));
        }
    }
}