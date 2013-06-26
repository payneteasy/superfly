package com.payneteasy.superfly.web.wicket.utils;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Map;

/**
 * @author rpuch
 */
public class PageParametersBuilder {
    public static final String ID = "id";

    public static PageParameters createId(long id) {
        PageParameters params = new PageParameters();
        params.set(ID, String.valueOf(id));
        return params;
    }

    public static long getId(PageParameters params) {
        return params.get(ID).toLong();
    }

    public static PageParameters fromMap(Map<String, Object> map) {
        PageParameters params = new PageParameters();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            params.set(entry.getKey(), entry.getValue());
        }
        return params;
    }

    public static PageParameters fromPair(String key, Object value) {
        PageParameters params = new PageParameters();
        params.set(key, value);
        return params;
    }
}
