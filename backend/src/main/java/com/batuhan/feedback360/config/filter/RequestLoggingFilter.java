package com.batuhan.feedback360.config.filter;

import com.batuhan.feedback360.model.entitiy.Log;
import com.batuhan.feedback360.service.LogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final LogService logService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        LocalDateTime requestTime = LocalDateTime.now();

        filterChain.doFilter(requestWrapper, responseWrapper);

        long duration = System.currentTimeMillis() - startTime;
        LocalDateTime responseTime = LocalDateTime.now();

        String requestBody = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
        String responseBody = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);

        Log log = Log.builder()
            .httpMethod(requestWrapper.getMethod())
            .uri(requestWrapper.getRequestURI())
            .clientIp(requestWrapper.getRemoteAddr())
            .requestHeaders(getHeadersAsString(requestWrapper))
            .requestBody(requestBody)
            .responseStatus(responseWrapper.getStatus())
            .responseBody(responseBody)
            .requestTime(requestTime)
            .responseTime(responseTime)
            .durationMs(duration)
            .build();

        logService.saveLog(log);

        responseWrapper.copyBodyToResponse();
    }

    private String getHeadersAsString(HttpServletRequest request) {
        Map<String, String> headersMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            headersMap.put(key, value);
        }
        return headersMap.toString();
    }
}
