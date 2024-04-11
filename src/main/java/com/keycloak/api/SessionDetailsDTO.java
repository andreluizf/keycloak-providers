package com.keycloak.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class SessionDetailsDTO {

    private String username;
    private String clientId;
    private String state;
}
