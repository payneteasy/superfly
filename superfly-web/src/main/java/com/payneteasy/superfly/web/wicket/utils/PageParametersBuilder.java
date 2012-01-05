package com.payneteasy.superfly.web.wicket.utils;

import org.apache.wicket.PageParameters;

/**
 * @author rpuch
 */
public class PageParametersBuilder {
    public static final String ID = "id";

    public static PageParameters createId(long id) {
        PageParameters params = new PageParameters();
        params.put(ID, String.valueOf(id));
        return params;
    }

    public static long getId(PageParameters params) {
        return params.getAsLong(ID);
    }
}
