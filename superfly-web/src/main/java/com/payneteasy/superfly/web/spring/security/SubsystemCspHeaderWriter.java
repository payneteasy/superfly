package com.payneteasy.superfly.web.spring.security;

import java.util.function.Supplier;

import org.springframework.security.web.header.HeaderWriter;

import com.payneteasy.superfly.service.impl.SubsystemOriginCache;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Writes Content-Security-Policy on every response; subsystem origins come from a cache, not from the request.
 */
class SubsystemCspHeaderWriter implements HeaderWriter {

    static final String HEADER = "Content-Security-Policy";

    private record Built(SubsystemOriginCache.Urls source, String policy) {
    }

    private final Supplier<SubsystemOriginCache.Urls> urls;
    // The cache hands out the same Urls instance until it expires, so the policy (and the WARNs
    // about rejected URLs) is built once per reload rather than once per response.
    private volatile Built last;

    SubsystemCspHeaderWriter(Supplier<SubsystemOriginCache.Urls> urls) {
        this.urls = urls;
    }

    @Override
    public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
        if (response.containsHeader(HEADER)) {
            return;
        }
        SubsystemOriginCache.Urls current = urls.get();
        Built built = last;
        if (built == null || built.source() != current) {
            built = new Built(current, CspPolicyBuilder.build(current.formActionUrls(), current.styleUrls()));
            last = built;
        }
        response.setHeader(HEADER, built.policy());
    }
}
