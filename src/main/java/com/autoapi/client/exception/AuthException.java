package com.autoapi.client.exception;

public class AuthException extends ApiException {

    public AuthException(int statusCode, String responseBody) {
        super(statusCode,
              statusCode == 401 ? "Unauthorized — invalid API key"
                                : "Forbidden — access denied",
              responseBody);
    }
}
