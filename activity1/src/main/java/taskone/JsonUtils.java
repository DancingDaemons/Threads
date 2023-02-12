/**
  File: JsonUtils.java
  Author: Student in Fall 2020B
  Description: JsonUtils class in package taskone.
*/

package taskone;

import org.json.JSONObject;

/**
 * Class: JsonUtils
 * Description: Json Utilities.
 */
public class JsonUtils {
    public static JSONObject fromByteArray(byte[] bytes) {
        try {
            String jsonString = new String(bytes);
            System.out.println("JSON String: " + jsonString);
            return new JSONObject(jsonString);
        }catch (Exception e){
            System.out.println("Parse exception");
        }
        return new JSONObject().put("selected", 0);
    }

    public static byte[] toByteArray(JSONObject object) {
        return object.toString().getBytes();
    }
}
