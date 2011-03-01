/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.office;

import java.util.HashMap;
import java.util.Map;

/**
 * User: jdev
 * Date: 14.09.2010
 */
public class StaticDataManager {

    private static final Map<String, Object> data = new HashMap<String, Object>();

    public void add(String key, Object data) {
        if (StaticDataManager.data.containsKey(key)) {
            throw new IllegalArgumentException("There's already defined data for key: " + key);
        }

        StaticDataManager.data.put(key, data);
    }

    public boolean isDefined(String key) {
        return StaticDataManager.data.containsKey(key);
    }

    public Object getData(String key) {
        return StaticDataManager.data.get(key);
    }
}
