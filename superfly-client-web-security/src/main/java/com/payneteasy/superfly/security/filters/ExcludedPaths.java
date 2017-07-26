package com.payneteasy.superfly.security.filters;

import java.util.Arrays;
import java.util.List;

public class ExcludedPaths {

    private final List<String> paths;

    public ExcludedPaths(String ... aPaths) {
        paths = Arrays.asList(aPaths);
    }


    public boolean isExcluded(String aUrl) {
        for (String path : paths) {
            if(aUrl.startsWith(path)) {
                return true;
            }
        }
        return false;
    }
}
