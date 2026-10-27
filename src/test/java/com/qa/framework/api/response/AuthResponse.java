package com.qa.framework.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** POST /auth returns {"token": "..."} on success and {"reason": "Bad credentials"} (still HTTP 200) on failure. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthResponse(String token, String reason) {

    @Override
    public String toString() {
        return "AuthResponse[token=" + (token == null ? "null" : "****") + ", reason=" + reason + "]";
    }
}
