package com.payneteasy.superfly.web.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Rejects requests whose body is larger than the {@code maxBytes} init-param with 413.
 * The declared Content-Length is checked before anything is read; for chunked bodies the bytes are
 * counted while they are read.
 */
public class RequestBodyLimitFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestBodyLimitFilter.class);

    private long maxBytes;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String value = filterConfig.getInitParameter("maxBytes");
        try {
            maxBytes = Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new ServletException("Init-param maxBytes must be a number, got: " + value, e);
        }
        if (maxBytes < 0) {
            throw new ServletException("Init-param maxBytes must not be negative: " + maxBytes);
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        if (!(req instanceof HttpServletRequest request) || !(resp instanceof HttpServletResponse response)) {
            chain.doFilter(req, resp);
            return;
        }
        if (request.getContentLengthLong() > maxBytes) {
            logger.warn("Request body of {} bytes exceeds the limit of {} bytes", request.getContentLengthLong(), maxBytes);
            response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            return;
        }
        try {
            chain.doFilter(new LimitedRequest(request, maxBytes), response);
        } catch (IOException | ServletException | RuntimeException e) {
            if (!isBodyTooLarge(e)) {
                throw e;
            }
            logger.warn("Request body exceeds the limit of {} bytes", maxBytes);
            if (response.isCommitted()) {
                throw e;
            }
            response.resetBuffer();
            response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        }
    }

    private static boolean isBodyTooLarge(Throwable e) {
        for (int depth = 0; e != null && depth < 10; depth++, e = e.getCause()) {
            if (e instanceof BodyTooLargeException) {
                return true;
            }
        }
        return false;
    }

    static class BodyTooLargeException extends IOException {
        BodyTooLargeException(long maxBytes) {
            super("Request body exceeds " + maxBytes + " bytes");
        }
    }

    private static class LimitedRequest extends HttpServletRequestWrapper {
        private final long maxBytes;
        private ServletInputStream stream;
        private BufferedReader reader;

        LimitedRequest(HttpServletRequest request, long maxBytes) {
            super(request);
            this.maxBytes = maxBytes;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (reader != null) {
                throw new IllegalStateException("getReader() has already been called");
            }
            if (stream == null) {
                stream = new CountingStream(super.getInputStream(), maxBytes);
            }
            return stream;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            if (reader == null) {
                String encoding = getCharacterEncoding();
                Charset charset = encoding == null ? StandardCharsets.ISO_8859_1 : Charset.forName(encoding);
                reader = new BufferedReader(new InputStreamReader(getInputStream(), charset));
            }
            return reader;
        }
    }

    private static class CountingStream extends ServletInputStream {
        private final ServletInputStream delegate;
        private final long maxBytes;
        private long count;

        CountingStream(ServletInputStream delegate, long maxBytes) {
            this.delegate = delegate;
            this.maxBytes = maxBytes;
        }

        private void add(long n) throws IOException {
            if (n > 0) {
                count += n;
                if (count > maxBytes) {
                    throw new BodyTooLargeException(maxBytes);
                }
            }
        }

        @Override
        public int read() throws IOException {
            int b = delegate.read();
            if (b >= 0) {
                add(1);
            }
            return b;
        }

        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            int n = delegate.read(buf, off, len);
            add(n);
            return n;
        }

        @Override
        public boolean isFinished() {
            return delegate.isFinished();
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setReadListener(ReadListener listener) {
            delegate.setReadListener(listener);
        }
    }
}
