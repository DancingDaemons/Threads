/**
  File: Performer.java
  Author: Student in Fall 2020B
  Description: Performer class in package taskone.
*/

package taskone;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

/**
 * Class: Performer 
 * Description: Threaded Performer for server tasks.
 */
public class Performer {

    private StringList state;
    private Socket conn;

    public Performer(Socket sock, StringList strings) {
        this.conn = sock;
        this.state = strings;
    }

    public JSONObject add(String str) {
        JSONObject json = new JSONObject();
        json.put("datatype", 1);
        json.put("type", "add");
        state.add(str);
        json.put("data", state.toString());
        return json;
    }

    public JSONObject clear(){
        state.clear();
        JSONObject json = new JSONObject();
        json.put("datatype", 1);
        json.put("type", "clear");
        json.put("data", state.toString());
        return json;
    }

    public JSONObject find(String str){
        JSONObject json = new JSONObject();
        json.put("datatype", 1);
        json.put("type", "find");
        if(state.contains(str)) {
            json.put("data", String.valueOf(state.find(str)));
        }else{
            json.put("data", "-1");
        }
        return json;
    }

    public JSONObject display(){
        JSONObject json = new JSONObject();
        json.put("datatype", 1);
        json.put("type", "display");
        return json.put("data", state.toString());
    }

    public JSONObject sort(){
        JSONObject json = new JSONObject();
        json.put("datatype", 1);
        json.put("type", "sort");
        state.sort();
        return json.put("data", state.toString());
    }

    public JSONObject prepend(String str){
        System.out.println(str);
        String[] prepend = str.split(" ");
        String content;
        if(prepend.length > 2){
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < prepend.length; i++) {
                sb.append(prepend[1]).append(" ");
            }
            content = sb.toString().trim();
        }else{
            content = prepend[1];
        }
        int index = Integer.parseInt(prepend[0]);
        JSONObject json = new JSONObject();
        json.put("datatype", 1);
        json.put("type", "prepend");
        state.prepend(index, content);
        System.out.println(state.strings);
        return json.put("data", state.strings.toString());
    }

    public static JSONObject error(String err) {
        JSONObject json = new JSONObject();
        json.put("error", err);
        return json;
    }

    public void doPerform() {
        boolean quit = false;
        OutputStream out = null;
        InputStream in = null;
        try {
            out = conn.getOutputStream();
            in = conn.getInputStream();
            System.out.println("Server connected to client:");
            while (!quit) {
                byte[] messageBytes = NetworkUtils.receive(in);
                JSONObject message = JsonUtils.fromByteArray(messageBytes);
                JSONObject returnMessage = new JSONObject();
   
                int choice = message.getInt("selected");
                    switch (choice) {
                        case (1):
                            String inStr = (String) message.get("data");
                            returnMessage = add(inStr);
                            break;
                        case (2):
                            // clear - remove all data from stings
                            returnMessage = clear();
                            break;
                        case (3):
                            returnMessage = find(message.getString("data"));
                            break;
                        case (4):
                            returnMessage = display();
                            break;
                        case (5):
                            returnMessage = sort();
                            break;
                        case (6):
                            returnMessage = prepend(message.getString("data"));
                            break;
                        case (0):
                            //quit
                            quit = true;
                            break;
                        default:
                            returnMessage = error("Invalid selection: " + choice 
                                    + " is not an option");
                            break;
                    }
                // we are converting the JSON object we have to a byte[]
                byte[] output = JsonUtils.toByteArray(returnMessage);
                NetworkUtils.send(out, output);
            }
            // close the resource
            System.out.println("close the resources of client ");
            out.close();
            in.close();
            conn.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
