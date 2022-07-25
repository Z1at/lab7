package NikandrovLab5.utility;

import NikandrovLab5.data.*;

import java.util.*;
import java.util.LinkedHashMap;

public class Collection {
    public final LinkedHashMap<String, City> collection;
    public final TreeMap<String, Vector<City>> creators;
    public final String initTime;
    public int id = 0;

    public Collection() {
        collection = new LinkedHashMap<>();
        creators = new TreeMap<>();
        initTime = new Date().toString();
    }
}
