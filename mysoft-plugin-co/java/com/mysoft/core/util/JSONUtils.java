package com.mysoft.core.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhouj04 on 2016/7/6.
 */
public class JSONUtils {

    public static JSONObject toJson(Map<String, Object> map) throws JSONException {
        if (map == null || map.size() == 0) {
            return null;
        }
        JSONObject result = new JSONObject();
        Set<String> keySet = map.keySet();
        for (String key : keySet) {
            result.put(key, map.get(key));
        }
        return result;
    }

    public static Map<String, String> toMap(JSONObject obj) throws JSONException {
        if (obj == null) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = obj.optString(key);
            map.put(key, value);
        }
        return map;
    }
}
