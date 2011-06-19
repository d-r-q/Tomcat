package lxx.office;

import java.util.HashMap;
import java.util.Map;

/**
 * User: jdev
 * Date: 19.06.11
 */
public class PropertiesManager {

    private static final Map<String, String> properties = new HashMap<String, String>();

    public static void setDebugProperty(String name, String value) {
        properties.put(name, value);
    }

    public static String getDebugProperty(String name) {
        return properties.get(name);
    }

}
