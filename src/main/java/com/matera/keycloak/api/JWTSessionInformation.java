package com.matera.keycloak.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JWTSessionInformation {
    private String exp;
    private String iat;
    private String jti;
    private String iss;
    private String sub;
    private String typ;
    @JsonProperty("session_state")
    private String sessionState;
    private String sid;
    @JsonProperty("state_checker")
    private String stateChecker;

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public String getIat() {
        return iat;
    }

    public void setIat(String iat) {
        this.iat = iat;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public String getSessionState() {
        return sessionState;
    }

    public void setSessionState(String sessionState) {
        this.sessionState = sessionState;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getStateChecker() {
        return stateChecker;
    }

    public void setStateChecker(String stateChecker) {
        this.stateChecker = stateChecker;
    }
}