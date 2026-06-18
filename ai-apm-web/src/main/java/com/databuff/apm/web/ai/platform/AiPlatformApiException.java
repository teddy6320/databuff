package com.databuff.apm.web.ai.platform;

public class AiPlatformApiException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public AiPlatformApiException(String errorCode, int httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String errorCode() {
        return errorCode;
    }

    public int httpStatus() {
        return httpStatus;
    }

    public static AiPlatformApiException notFound(String resource, String id) {
        return new AiPlatformApiException(resource + "_not_found", 404, resource + " not found: " + id);
    }

    public static AiPlatformApiException conflict(String errorCode, String message) {
        return new AiPlatformApiException(errorCode, 409, message);
    }

    public static AiPlatformApiException badRequest(String message) {
        return new AiPlatformApiException("bad_request", 400, message);
    }

    public static AiPlatformApiException forbidden(String message) {
        return new AiPlatformApiException("forbidden", 403, message);
    }
}
