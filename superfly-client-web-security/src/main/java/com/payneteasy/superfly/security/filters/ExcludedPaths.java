package com.payneteasy.superfly.security.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExcludedPaths {

    private final List<String> paths;

    /**
     * @param aPaths application paths (without context path) to exclude, each
     *               one matches itself and everything under it: '/static'
     *               matches '/static' and '/static/x', but not '/staticX'
     */
    public ExcludedPaths(String ... aPaths) {
        paths = new ArrayList<>(aPaths.length);
        for (String path : aPaths) {
            // '/static/' and '/static' mean the same
            paths.add(path.length() > 1 && path.endsWith("/") ? path.substring(0, path.length() - 1) : path);
        }
    }

    /**
     * @param aUrl normalized application path (without context path, see
     *             {@link com.payneteasy.superfly.security.filters.internal.SecurityFilterFlow#getPath()})
     */
    public boolean isExcluded(String aUrl) {
        if (aUrl == null) {
            return false;
        }
        for (String path : paths) {
            if (path.equals("/") || aUrl.equals(path) || aUrl.startsWith(path + "/")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ExcludedPaths" + Arrays.toString(paths.toArray());
    }
}
