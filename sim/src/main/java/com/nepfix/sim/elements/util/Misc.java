package com.nepfix.sim.elements.util;


import org.python.apache.xerces.impl.dv.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class Misc {

    private static final MessageDigest md;

    static {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);//Should never happen
        }
    }

    @SuppressWarnings("unchecked")
    public static <K, V> void putInListHashMap(K key, V element, HashMap<K, List<V>> map) {
        List tList = map.get(key);
        if (tList != null) {
            tList.add(element);
        } else {
            tList = new ArrayList<>();
            tList.add(element);
            map.put(key, tList);
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
