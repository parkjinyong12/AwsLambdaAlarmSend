package com.ruokit.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessTokenResponse {

    @JsonProperty("expires_dt")
    private String expiresDt;

    @JsonProperty("token_type")
    private String tokenType;

    private String token;

    public String getExpiresDt() {
        return expiresDt;
    }

    public void setExpiresDt(String expiresDt) {
        this.expiresDt = expiresDt;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
