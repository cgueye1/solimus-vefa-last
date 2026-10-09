package com.wakana.vefa.config;
import com.wakana.vefa.util.XssSanitizer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Component
public class XssFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

        if (isMultipartRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpServletRequest sanitizedRequest = new XssRequestWrapper(request);
        filterChain.doFilter(sanitizedRequest, response);
    }

    private boolean isMultipartRequest(HttpServletRequest request) {
        return request.getContentType() != null 
                && request.getContentType().startsWith("multipart/form-data");
    }

    private static class XssRequestWrapper extends HttpServletRequestWrapper {

        private final String sanitizedBody;

        public XssRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            this.sanitizedBody = sanitizeBody(request);
        }

        private String sanitizeBody(HttpServletRequest request) throws IOException {
            String body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);

            if (body == null || body.isEmpty()) {
                return body;
            }

            Pattern pattern = Pattern.compile("\"(\\w+)\":\\s*\"(.*?)\"");
            Matcher matcher = pattern.matcher(body);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String key = matcher.group(1);
                String value = matcher.group(2);
                String cleanValue = XssSanitizer.clean(value);
                matcher.appendReplacement(sb, "\"" + key + "\":\"" + cleanValue + "\"");
            }
            matcher.appendTail(sb);

            return sb.toString();
        }

        @Override
        public ServletInputStream getInputStream() {
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                    sanitizedBody.getBytes(StandardCharsets.UTF_8));

            return new ServletInputStream() {
                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }

                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // Non implémenté pour ce cas
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return value != null ? XssSanitizer.clean(value) : null;
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) {
                return null;
            }
            return Arrays.stream(values)
                    .map(XssSanitizer::clean)
                    .toArray(String[]::new);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            Map<String, String[]> parameterMap = super.getParameterMap();
            Map<String, String[]> cleanMap = new HashMap<>();

            parameterMap.forEach((key, values) -> {
                String[] cleanValues = Arrays.stream(values)
                        .map(XssSanitizer::clean)
                        .toArray(String[]::new);
                cleanMap.put(key, cleanValues);
            });

            return cleanMap;
        }
    }
}