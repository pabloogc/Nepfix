package com.nepfix.sim.elements.util;


import org.python.apache.xerces.impl.dv.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public abstract class Misc {

    private static final MessageDigest md;

    static {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);//Should never happen
        }
    }

    public static <K, V> void putInListHashMap(K key, V element, HashMap<K, List<V>> map) {
        List<V> list = map.get(key);
        if (list != null) {
            list.add(element);
        } else {
            list = new ArrayList<>();
            list.add(element);
            map.put(key, list);
        }
    }

    public static <K, V> void putInSetHashMap(K key, V element, HashMap<K, Set<V>> map) {
        Set<V> set = map.get(key);
        if (set != null) {
            set.add(element);
        } else {
            set = new HashSet<>();
            set.add(element);
            map.put(key, set);
        }
    }

    public static String getNepMd5(String nepId, List<String> nodesIds) {
        StringBuilder sb = new StringBuilder();
        sb.append(nepId);
        nodesIds.forEach(sb::append);
        byte[] bytes = sb.toString().getBytes();
        byte[] digest = md.digest(bytes);
        return Base64.encode(digest);
    }
}
