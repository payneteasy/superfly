package com.payneteasy.superfly.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.RandomAccess;

public class ListUtils {
    public static <T> List<T> prependWithNull(List<T> list) {
        List<T> result;
        if (list instanceof RandomAccess) {
            result = new ArrayList<T>(list.size() + 1);
        } else {
            result = new LinkedList<T>();
        }
        result.add(null);
        result.addAll(list);
        return result;
    }
}
