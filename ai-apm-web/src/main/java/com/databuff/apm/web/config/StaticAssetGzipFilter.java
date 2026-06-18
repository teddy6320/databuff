package com.databuff.apm.web.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * Tomcat {@code server.compression} does not gzip classpath static resources; compress /assets
 * responses when the client accepts gzip.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class StaticAssetGzipFilter extends OncePerRequestFilter {

    private static final int MIN_BYTES = 1024;
    private static final Set<String> COMPRESSIBLE = Set.of(
            "application/javascript",
            "text/javascript",
            "text/css",
            "application/json",
            "image/svg+xml",
            "text/plain",
            "text/html");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/assets/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!acceptsGzip(request) || response.isCommitted()) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingResponseWrapper cached = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(request, cached);
        } finally {
            writeCachedResponse(request, cached);
        }
    }

    private static boolean acceptsGzip(HttpServletRequest request) {
        String accept = request.getHeader("Accept-Encoding");
        return accept != null && accept.contains("gzip");
    }

    private static void writeCachedResponse(HttpServletRequest request, ContentCachingResponseWrapper cached)
            throws IOException {
        byte[] body = cached.getContentAsByteArray();
        if (body.length == 0 || cached.getStatus() != HttpServletResponse.SC_OK) {
            cached.copyBodyToResponse();
            return;
        }
        String contentType = cached.getContentType();
        if (contentType == null || !isCompressible(contentType)) {
            cached.copyBodyToResponse();
            return;
        }
        if (body.length < MIN_BYTES) {
            cached.copyBodyToResponse();
            return;
        }
        if (cached.containsHeader("Content-Encoding")) {
            cached.copyBodyToResponse();
            return;
        }

        HttpServletResponse response = (HttpServletResponse) cached.getResponse();
        response.resetBuffer();
        response.setHeader("Content-Encoding", "gzip");
        response.setHeader("Vary", "Accept-Encoding");
        response.setContentLength(-1);
        try (GZIPOutputStream gzip = new GZIPOutputStream(response.getOutputStream())) {
            gzip.write(body);
        }
    }

    private static boolean isCompressible(String contentType) {
        int semi = contentType.indexOf(';');
        String mime = semi >= 0 ? contentType.substring(0, semi).trim() : contentType.trim();
        return COMPRESSIBLE.contains(mime);
    }
}
