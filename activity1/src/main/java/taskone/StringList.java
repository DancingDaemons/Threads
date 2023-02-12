package taskone;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class StringList {
    
    List<String> strings = new ArrayList<>();

    synchronized public void add(String str) {
        int pos = strings.indexOf(str);
        if (pos < 0) {
            strings.add(str);
        }
    }

    synchronized public void sort(){
        strings = strings.stream().sorted().collect(Collectors.toList());
    }

    synchronized public boolean contains(String str) {
        return strings.contains(str);
    }

    synchronized public void clear(){
        strings.clear();
    }

    synchronized public int find(String str){
        return strings.indexOf(str);
    }

    synchronized public void prepend(int index, String str){
        strings.add(index, str);
    }

    public int size() {
        return strings.size();
    }

    public String toString() {
        return strings.toString();
    }
}