package com.payneteasy.superfly.security.filters.internal;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class MustachePrinter {

    Mustache mustache;
    Map<String, Object> scope = new HashMap<>();

    public MustachePrinter(String aTemplate) {
        MustacheFactory mf = new DefaultMustacheFactory();
        mustache = mf.compile(aTemplate);
    }

    public void add(String aName, Object aValue) {
        scope.put(aName, aValue);
    }

    public void write(PrintWriter aWriter) throws IOException {
        mustache.execute(aWriter, scope).flush();
    }

}
