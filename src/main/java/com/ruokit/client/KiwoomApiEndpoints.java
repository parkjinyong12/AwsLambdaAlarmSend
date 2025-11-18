package com.ruokit.client;

public final class KiwoomApiEndpoints {

    private static final String DEFAULT_BASE_URL = "https://api.kiwoom.com";
    private static final String BASE_URL_ENV = "KIWOOM_API_BASE_URL";

    private KiwoomApiEndpoints() {
    }

    public static String getBaseUrl() {
        String baseUrl = System.getenv(BASE_URL_ENV);
        if (baseUrl == null || baseUrl.isBlank()) {
            return DEFAULT_BASE_URL;
        }
        return trimTrailingSlash(baseUrl.trim());
    }

    public static String resolve(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }

        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return getBaseUrl() + normalizedPath;
    }

    private static String trimTrailingSlash(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
