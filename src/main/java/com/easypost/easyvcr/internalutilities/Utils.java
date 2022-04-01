package com.easypost.easyvcr.internalutilities;

import com.easypost.easyvcr.clients.httpclient.RecordableHttpRequestImpl;

import java.net.http.HttpHeaders;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiPredicate;

import static java.lang.String.format;

public final class Utils {
    private static final boolean[] tchar      = new boolean[256];
    private static final boolean[] fieldvchar = new boolean[256];

    private static final String HEADER_CONNECTION = "Connection";
    private static final String HEADER_UPGRADE    = "Upgrade";

    private static final Set<String> DISALLOWED_HEADERS_SET = getDisallowedHeaders();

    public static final BiPredicate<String, String>
            ALLOWED_HEADERS = (header, unused) -> !DISALLOWED_HEADERS_SET.contains(header);

    public static final BiPredicate<String, String> VALIDATE_USER_HEADER =
            (name, value) -> {
                assert name != null : "null header name";
                assert value != null : "null header value";
                if (!isValidName(name)) {
                    throw newIAE("invalid header name: \"%s\"", name);
                }
                if (!ALLOWED_HEADERS.test(name, null)) {
                    throw newIAE("restricted header name: \"%s\"", name);
                }
                if (!isValidValue(value)) {
                    throw newIAE("invalid header value for %s: \"%s\"", name, value);
                }
                return true;
            };

    private static Set<String> getDisallowedHeaders() {
        Set<String> headers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        headers.addAll(Set.of("connection", "content-length", "expect", "host", "upgrade"));

        // String v = getNetProperty("jdk.httpclient.allowRestrictedHeaders");
        String v = null;
        if (v != null) {
            // any headers found are removed from set.
            String[] tokens = v.trim().split(",");
            for (String token : tokens) {
                headers.remove(token);
            }
            return Collections.unmodifiableSet(headers);
        } else {
            return Collections.unmodifiableSet(headers);
        }
    }

    /*
     * Validates a RFC 7230 field-name.
     */
    public static boolean isValidName(String token) {
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c > 255 || !tchar[c]) {
                return false;
            }
        }
        return !token.isEmpty();
    }

    /*
     * Validates a RFC 7230 field-value.
     *
     * "Obsolete line folding" rule
     *
     *     obs-fold = CRLF 1*( SP / HTAB )
     *
     * is not permitted!
     */
    public static boolean isValidValue(String token) {
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c > 255) {
                return false;
            }
            if (c == ' ' || c == '\t') {
                continue;
            } else if (!fieldvchar[c]) {
                return false; // forbidden byte
            }
        }
        return true;
    }

    public static IllegalArgumentException newIAE(String message, Object... args) {
        return new IllegalArgumentException(format(message, args));
    }

    public static final void setWebSocketUpgradeHeaders(RecordableHttpRequestImpl request) {
        request.setSystemHeader(HEADER_UPGRADE, "websocket");
        request.setSystemHeader(HEADER_CONNECTION, "Upgrade");
    }

    public record ProxyHeaders(HttpHeaders userHeaders, HttpHeaders systemHeaders) {}
}
