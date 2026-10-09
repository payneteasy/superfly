package com.payneteasy.superfly.web.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class RequestBodyLimitFilterTest {

    private static final int LIMIT = 10;

    private static RequestBodyLimitFilter filter() throws ServletException {
        RequestBodyLimitFilter filter = new RequestBodyLimitFilter();
        filter.init(new MockFilterConfig() {{
            addInitParameter("maxBytes", String.valueOf(LIMIT));
        }});
        return filter;
    }

    private static MockHttpServletRequest chunked(byte[] body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/x") {
            @Override
            public long getContentLengthLong() {
                return -1;
            }
        };
        request.setContent(body);
        return request;
    }

    private static MockHttpServletRequest withLength(byte[] body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/x");
        request.setContent(body);
        return request;
    }

    @Test
    public void declaredLengthOverLimitIsRejectedWithoutReadingBody() throws Exception {
        AtomicBoolean chainCalled = new AtomicBoolean();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter().doFilter(withLength(new byte[LIMIT + 1]), response, (rq, rs) -> chainCalled.set(true));

        assertEquals(413, response.getStatus());
        assertFalse(chainCalled.get());
    }

    @Test
    public void bodyWithinLimitReachesChainUnchanged() throws Exception {
        byte[] body = "0123456789".getBytes(StandardCharsets.UTF_8);
        List<byte[]> seen = new ArrayList<>();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter().doFilter(withLength(body), response, (rq, rs) -> seen.add(rq.getInputStream().readAllBytes()));
        filter().doFilter(chunked(body), response, (rq, rs) -> seen.add(rq.getInputStream().readAllBytes()));

        assertEquals(200, response.getStatus());
        assertArrayEquals(body, seen.get(0));
        assertArrayEquals(body, seen.get(1));
    }

    @Test
    public void readerWithinLimitReturnsSameText() throws Exception {
        MockHttpServletRequest request = chunked("абвгд".getBytes(StandardCharsets.UTF_8));
        request.setCharacterEncoding("UTF-8");
        List<String> seen = new ArrayList<>();

        filter().doFilter(request, new MockHttpServletResponse(), (rq, rs) -> {
            try (var reader = rq.getReader()) {
                seen.add(reader.readLine());
            }
        });

        assertEquals("абвгд", seen.get(0));
    }

    @Test
    public void chunkedBodyOverLimitFailsReadAndAnswers413() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean failed = new AtomicBoolean();

        filter().doFilter(chunked(new byte[LIMIT + 1]), response, (rq, rs) -> {
            try {
                rq.getInputStream().readAllBytes();
            } catch (IOException e) {
                failed.set(true);
                throw e;
            }
        });

        assertTrue(failed.get());
        assertEquals(413, response.getStatus());
    }

    @Test
    public void chunkedBodyOverLimitFailsReaderToo() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean failed = new AtomicBoolean();

        filter().doFilter(chunked(new byte[LIMIT + 1]), response, (rq, rs) -> {
            try {
                rq.getReader().readLine();
            } catch (IOException e) {
                failed.set(true);
                throw new ServletException("wrapped", e);
            }
        });

        assertTrue(failed.get());
        assertEquals(413, response.getStatus());
    }

    @Test
    public void committedResponseKeepsExceptionAndStatus() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (rq, rs) -> {
            rs.getWriter().write("partial");
            rs.flushBuffer();
            rq.getInputStream().readAllBytes();
        };

        try {
            filter().doFilter(chunked(new byte[LIMIT + 1]), response, chain);
            fail();
        } catch (IOException expected) {
            assertEquals(200, response.getStatus());
        }
    }

    @Test
    public void otherExceptionsPropagate() throws Exception {
        try {
            filter().doFilter(withLength(new byte[1]), new MockHttpServletResponse(), (rq, rs) -> {
                throw new IllegalStateException("boom");
            });
            fail();
        } catch (IllegalStateException expected) {
            assertEquals("boom", expected.getMessage());
        }
    }

    @Test(expected = ServletException.class)
    public void missingLimitFailsInit() throws Exception {
        new RequestBodyLimitFilter().init(new MockFilterConfig());
    }

    @Test
    public void webXmlPutsLimitsBeforeSecurityChain() throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new File("src/main/webapp/WEB-INF/web.xml"));
        NodeList all = doc.getDocumentElement().getChildNodes();
        List<String> mappingOrder = new ArrayList<>();
        java.util.Map<String, String> limits = new java.util.HashMap<>();
        for (int i = 0; i < all.getLength(); i++) {
            if (!(all.item(i) instanceof Element e)) {
                continue;
            }
            if (e.getTagName().equals("filter-mapping")) {
                String name = text(e, "filter-name");
                String pattern = text(e, "url-pattern");
                mappingOrder.add(name + " " + pattern);
            } else if (e.getTagName().equals("filter")
                    && text(e, "filter-class").equals(RequestBodyLimitFilter.class.getName())) {
                limits.put(text(e, "filter-name"), text(e, "param-value"));
            }
        }
        int security = mappingOrder.indexOf("springSecurityFilterChain /*");
        assertTrue(security >= 0);
        assertLimit(mappingOrder, limits, "/sso/check/*", "65536", security);
        assertLimit(mappingOrder, limits, "/remoting/*", "1048576", security);
    }

    private static void assertLimit(List<String> order, java.util.Map<String, String> limits, String pattern,
                                    String expectedBytes, int securityIndex) {
        for (int i = 0; i < order.size(); i++) {
            String[] parts = order.get(i).split(" ");
            if (parts[1].equals(pattern) && expectedBytes.equals(limits.get(parts[0]))) {
                assertTrue("limit for " + pattern + " must precede the security chain", i < securityIndex);
                return;
            }
        }
        fail("no body limit filter mapped to " + pattern);
    }

    private static String text(Element parent, String tag) {
        return parent.getElementsByTagName(tag).item(0).getTextContent().trim();
    }
}
